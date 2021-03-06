import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

/**
 * Class that implements a cooperation client.
 *
 * @author Tomasz Fijalkowski
 * @version 1.0
 */
public class CooperationClient implements Controller {

	private static final int ITEM_NUM = 8;
	private static final int TILE_SIZE = 64;
	private int[] counters = new int[ITEM_NUM];

	private static final String defaultLevelRegex = "level-[1-5]";
	
	private boolean gameStarted = false;

	// For sounds
	private static final String DEATH_RAT_SOUND_1_PATH = "deathRatSound1.mp3";
	private static final String DEATH_RAT_SOUND_2_PATH = "deathRatSound2.mp3";
	private static final String DEATH_RAT_SOUND_3_PATH = "deathRatSound3.mp3";
	private static final double SOUND_VOLUME_RAT = 0.1f;
	private static final String SERVER_IP = null;

	// Game map
	private Tile[][] tileMap = new Tile[0][0];

	private int score;
	private int currentTimeLeft;

	// Rat counters
	private int femaleRatCounter;
	private int maleRatCounter;
	private int otherRatCounter;

	// For loading inventory from save
	private int[] savedPowers;
	private Boolean powersImportedFromSave = true;

	// Images for different game items
	private final List<Image> itemImages = Arrays.asList((new Bomb(this, 0, 0)).getImg(),
			(new Gas(this, 0, 0, true)).getImg(), (new Sterilisation(this, 0, 0)).getImg(),
			(new Poison(this, 0, 0)).getImg(), (new MaleSwapper(this, 0, 0)).getImg(),
			(new FemaleSwapper(this, 0, 0)).getImg(), (new StopSign(this, 0, 0)).getImg(),
			(new DeathRat(this, 0, Rat.Direction.WEST, 0, 0, 0, 0)).getImg());

	// Size of game map
	private int WIDTH;
	private int HEIGHT;

	// Game losing conditions (maximum number of rats, time taken for a level)
	private int MAX_RATS;
	private int PAR_TIME;

	private final MenuController MAIN_MENU;
	private final String LEVEL_NAME;

	// Item toolbars
	private List<HBox> toolbars;

	// Game timeline
	// private Timeline tickTimeline;

	@FXML
	public Canvas levelCanvas; // Game map canvas

	public Button saveLevelStateButton;

	// Toolbars for different game items
	public HBox bombToolbar;
	public HBox gasToolbar;
	public HBox sterilisationToolbar;
	public HBox poisonToolbar;
	public HBox maleSwapToolbar;
	public HBox femaleSwapToolbar;
	public HBox stopSignToolbar;
	public HBox deathRatToolbar;
	public VBox waitingRoot;
	public BorderPane gameplayRoot;

	public Label timerLabel;
	public Label femaleRatCounterLabel;
	public Label maleRatCounterLabel;
	public Label ratCounterLabel;

	public Pane gameEndPane;
	public TextFlow gamePaneText;
	public TextFlow gamePaneScore;
	public TextFlow gamePaneLeaderboard;

	public Pane saveLevelPane;
	public TextField levelNameTextField;
	public Text savingErrorText;
	public Button saveAndExitButton;

	private Socket server;
	private CooperationClientListener clientListener;
	private CooperationClientOutput clientOutput;

	/**
	 * Constructor for LevelController class.
	 *
	 * @param selectedLevelName  Number of level being played.
	 * @param mainMenuController Reference to the main menu controller.
	 */
	public CooperationClient(String selectedLevelName, MenuController mainMenuController, Scene scene, Stage stage) {

		/*
		 * String levelName, MenuController mainMenuController, Scene scene, Stage
		 * stage) { this.isMapLoaded = true; this.scene = scene; this.stage = stage;
		 * this.selectedEditLevelName = levelName; this.levelName = levelName; MAIN_MENU
		 * = mainMenuController;
		 */
		LEVEL_NAME = selectedLevelName;
		MAIN_MENU = mainMenuController;
	}

	/**
	 * Runs a client
	 * @param port	port of a server
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InterruptedException
	 */
	public void runClient(int port) throws IOException, ClassNotFoundException, InterruptedException {
		server = new Socket(SERVER_IP, port);
		clientListener = new CooperationClientListener(this, server);
		clientOutput = new CooperationClientOutput(this, server);
		Thread clientListenerThread = new Thread(clientListener);
		clientListenerThread.start();
	}

	/**
	 * Sets tile map.
	 * @param tileMap	tile map
	 */
	public void setTileMap(Tile[][] tileMap) {
		if(!gameStarted) {
			waitingRoot.setDisable(true);
			waitingRoot.setVisible(false);
			gameplayRoot.setDisable(false);
			gameplayRoot.setVisible(true);
			gameplayRoot.setOpacity(1);
			
		}
		this.tileMap = tileMap;
		this.renderGame();
	}

	/**
	 * Gets tile map.
	 * @return
	 */
	public Tile[][] getTileMap() {
		return tileMap;
	}

	/**
	 * Runs the game
	 * @param selectedEditLevelName	level name
	 * @param isDefaultLevel		if game is default true
	 * @param scene					scene
	 * @param stage					stage
	 * @param menu					main menu
	 * @throws IOException
	 */
	public void runTheGame(String selectedEditLevelName, boolean isDefaultLevel, Scene scene, Stage stage,
			MenuController menu) throws IOException {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("clientLevel.fxml"));

		if (isDefaultLevel) {
			LevelFileReader.loadNormalLevelFile(this,
					"src/main/resources/levels/default_levels/" + selectedEditLevelName, true);
		} else {
			LevelFileReader.loadNormalLevelFile(this,
					"src/main/resources/levels/created_levels/" + selectedEditLevelName, true);
		}

		WIDTH = LevelFileReader.getWidth();
		HEIGHT = LevelFileReader.getHeight();

		MAX_RATS = LevelFileReader.getMaxRats();
		if (LevelFileReader.getInProgTimer() != -1) {
			PAR_TIME = LevelFileReader.getInProgTimer();
		} else {
			PAR_TIME = LevelFileReader.getParTime();
		}

		loader.setController(this);
		Pane root = loader.load();

		scene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Returns current timer.
	 *
	 * @return Time in milliseconds.
	 */
	public int getCurrentTimeLeft() {
		return currentTimeLeft;
	}

	/**
	 * Returns current item counters.
	 *
	 * @return number of each item.
	 */
	public int[] getCounters() {
		return counters;
	}

	/**
	 * Sets items counters.
	 * @param counters counters
	 */
	public void setCounters(int[] counters) {
		this.renderAllItemsSafely(counters);
		setupCanvasDragBehaviour();
	}

	/**
	 * Initializes game.
	 */
	public void initialize() {
		currentTimeLeft = PAR_TIME * 1000;
		// timerLabel.setText(millisToString(currentTimeLeft));

		toolbars = Arrays.asList(bombToolbar, gasToolbar, sterilisationToolbar, poisonToolbar, maleSwapToolbar,
				femaleSwapToolbar, stopSignToolbar, deathRatToolbar);
		Arrays.fill(counters, 2);

		setupCanvasDragBehaviour();

		renderGame();
	}

	/**
	 * Builds new level from level file.
	 */
	private void buildNewLevel() {
		otherRatCounter = 0;
		femaleRatCounter = 0;
		maleRatCounter = 0;
		score = 0;

		tileMap = LevelFileReader.getTileMap();

		for (Rat rat : LevelFileReader.getRatSpawns()) {
			ratAdded(rat);
		}
	}

	/**
	 * Renders female and male counters on the screen.
	 */
	public void renderCounters() {
		String mc = String.valueOf(maleRatCounter);
		String fc = String.valueOf(femaleRatCounter);
		String rc = (femaleRatCounter + maleRatCounter + otherRatCounter) + "/" + (MAX_RATS);

		maleRatCounterLabel.setText(mc);
		femaleRatCounterLabel.setText(fc);
		ratCounterLabel.setText(rc);
	}

	/**
	 * Returns tile at position (x,y).
	 *
	 * @param x Horizontal position.
	 * @param y Vertical position.
	 * @return Tile at position.
	 */
	public Tile getTileAt(int x, int y) {
		try {
			return tileMap[x][y];
		} catch (Exception ArrayIndexOutOfBoundsException) {
			return null;
		}
	}

	/**
	 * Returns whether tile at position (x,y) on canvas can be interacted with.
	 *
	 * @param x canvas x position.
	 * @param y canvas y position.
	 * @return interactivity.
	 */
	private boolean tileInteractivityAt(double x, double y) {
		if (x >= (WIDTH * TILE_SIZE) || y >= (HEIGHT * TILE_SIZE)) {
			return false;
		} else {
			int xPos = (int) (Math.floor(x) / TILE_SIZE);
			int yPos = (int) (Math.floor(y) / TILE_SIZE);

			return tileMap[xPos][yPos].isInteractive();
		}
	}

	/**
	 * Sets time and counters
	 * @param temp time and counters object
	 */
	public void setTimeAndCounters(TimeAndRatCounters temp) {
		maleRatCounterLabel.setText(temp.getMaleRatCounter());
		femaleRatCounterLabel.setText(temp.getFemaleRatCounter());
		ratCounterLabel.setText(temp.getRatCounter());
		timerLabel.setText(temp.getTimeLeft());
		if (temp.isGameEnd()) {
			disableToolbars();

			gameEndPane.setVisible(true);

			if (temp.isGameWon()) {
				score += currentTimeLeft / 1000;
				gamePaneText.getChildren().add(new Text("You've won! :)"));
				gamePaneScore.getChildren().add(new Text("Score: " + score));
			} else {
				gamePaneText.getChildren().add(new Text("You've lost! :("));
			}
			
			String[] highScores = HighScores.getTopScores(LEVEL_NAME);
			for (String text : highScores) {
				gamePaneLeaderboard.getChildren().add(new Text(text + "\n"));
			}
			
		}
	}

	/**
	 * Copies power data to current level.
	 * 
	 * @param powers power data.
	 */
	public void addPowersFromSave(int[] powers) {
		savedPowers = powers;
	}

	/**
	 * Grants player items periodically.
	 */
	private void addPowers() {

		// Add powers to inventory from file save
		if (savedPowers != null && powersImportedFromSave) {
			for (int i = 0; i < savedPowers.length; i++) {
				while (savedPowers[i] > 0) {
					counters[i]++;
					renderItem(i);
					savedPowers[i] -= 1;
				}
			}
			powersImportedFromSave = false;
		}

		for (int i = 0; i < counters.length; i++) {
			renderItem(i);
		}

	}


	/**
	 * Exits level and goes back to main menu.
	 */
	@FXML
	private void exitGame() {
		MAIN_MENU.finishLevel();
	}

	/**
	 * Lets interactive tiles on the game screen accept drag and drop events.
	 */
	private void setupCanvasDragBehaviour() {

		levelCanvas.setOnDragOver(event -> {
			// Mark the drag as acceptable if the source is a draggable ImageView
			if (event.getGestureSource() instanceof ImageView) {
				if (tileInteractivityAt(event.getSceneX(), event.getSceneY()))
					event.acceptTransferModes(TransferMode.ANY);
				event.consume();
			}
		});

		// This code allows the canvas to react to a dragged object when it is finally
		// dropped.
		levelCanvas.setOnDragDropped(event -> {
			String dbContent = event.getDragboard().getString();
			try {
				int index = Integer.parseInt(dbContent);
				if (index >= 0 && index < ITEM_NUM) {
					itemDropped(event, index);
				}
			} catch (Exception ignored) {
			}
		});
	}

	/**
	 * Renders current tile map.
	 */
	private void renderGame() {
		GraphicsContext gc = levelCanvas.getGraphicsContext2D();
		if (tileMap != null) {
			for (int i = 0; i < tileMap.length; i++) {
				for (int j = 0; j < tileMap[i].length; j++) {
					tileMap[i][j].draw(i, j, gc);
				}
			}
		}
	}

	/**
	 * Renders all item toolbars.
	 */
	private void renderAllItems() {
		for (int i = 0; i < counters.length; i++) {
			renderItem(i);
		}
	}

	/**
	 * Renders all item toolbars without glithes.
	 */
	private void renderAllItemsSafely(int[] newCounters) {

		int[] previousCounters = counters.clone();
		this.counters = newCounters;
		for (int i = 0; i < counters.length; i++) {
			if (counters[i] != previousCounters[i]) {
				renderItem(i);
			}
		}
		
	}

	/**
	 * Adds item dropped by the player onto a Tile.
	 *
	 * @param event Drag and drop event.
	 * @param index Which item has been dragged
	 */
	private void itemDropped(DragEvent event, int index) {
		int x = (int) event.getX() / TILE_SIZE;
		int y = (int) event.getY() / TILE_SIZE;

		System.out.println("Item Dropped : " + index + " at tile: " + x + " : " + y );
		clientOutput.placePower(index, x, y);
		Power power = null;
		boolean addPower = true;
		switch (index) {
		case 0:
			power = new Bomb(this, x, y);
			break;
		case 1:
			power = new Gas(this, x, y, true);
			break;
		case 2:
			power = new Sterilisation(this, x, y);
			break;
		case 3:
			power = new Poison(this, x, y);
			break;
		case 4:
			power = new MaleSwapper(this, x, y);
			break;
		case 5:
			power = new FemaleSwapper(this, x, y);
			break;
		case 6:
			power = new StopSign(this, x, y);
			break;
		case 7:
			SeaShantySimulator seaSim = new SeaShantySimulator();
			int randomNum = ThreadLocalRandom.current().nextInt(1, 3);
			if (randomNum == 1) {
				seaSim.playAudioClip(DEATH_RAT_SOUND_1_PATH, SOUND_VOLUME_RAT);
			} else if (randomNum == 2) {
				seaSim.playAudioClip(DEATH_RAT_SOUND_2_PATH, SOUND_VOLUME_RAT);
			} else {
				seaSim.playAudioClip(DEATH_RAT_SOUND_3_PATH, SOUND_VOLUME_RAT);
			}

			tileMap[x][y].addOccupantRat(new DeathRat(this, Rat.getDEFAULT_SPEED(), Rat.Direction.NORTH, 0, x, y, 0));
			addPower = false;
			break;
		default:
			addPower = false;
		}
		if (addPower) {
			tileMap[x][y].addActivePower(power);
		}

		counters[index]--;
		renderItem(index);
	}

	/**
	 * Saves level state (if a valid level name is entered).
	 */
	@FXML
	public void saveLevel() {
		boolean canBeSaved = false;
		String newLevelName = levelNameTextField.getText();
		if (newLevelName.contains(" ")) {
			savingErrorText.setText("Level name cannot contain spaces");
		} else if (newLevelName.matches(defaultLevelRegex)) {
			savingErrorText.setText("Level name cannot be the same as default level");
		} else if (newLevelName.length() == 0) {
			savingErrorText.setText("Level name cannot be empty");
		} else {
			savingErrorText.setText("");
			canBeSaved = true;
		}

		if (canBeSaved) {
			try {
				LevelFileReader.saveLevel(this, newLevelName);
				makeScreenShot(newLevelName);
				System.out.println("Screenshot was saved");
			} catch (IOException e) {
				e.printStackTrace();
				savingErrorText.setText("An error occurred saving game state.");
			}
			MAIN_MENU.finishLevel();
		}
	}

	/**
	 * Makes screenshot of current tilemap.
	 * 
	 * @param levelName name of level being screenshot.
	 */
	public void makeScreenShot(String levelName) {
		File file = new File("src\\main\\resources\\saved_games_images\\" + ProfileFileReader.getLoggedProfile() + "\\"
				+ levelName + ".png");

		WritableImage writableImage = new WritableImage(TILE_SIZE * WIDTH, TILE_SIZE * HEIGHT);
		SnapshotParameters params = new SnapshotParameters();
		levelCanvas.snapshot(params, writableImage);

		try {
			ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
		} catch (Throwable th) {
			System.out.println("Screenshot wasn' saved :(");

		}

	}

	/**
	 * Actions performed when user decides to save the game and exit to main menu.
	 */
	@FXML
	public void openSaveDialogue() {
		// tickTimeline.stop();
		disableToolbars();
		saveLevelStateButton.setDisable(true);
		savingErrorText.setText("");
		levelNameTextField.setText(LEVEL_NAME + "-inProgress");
		saveLevelPane.setVisible(true);
	}

	/**
	 * Returns user from dialogue box back to level.
	 */
	public void goBackToLevel() {
		saveLevelPane.setVisible(false);
		saveLevelStateButton.setDisable(false);

		renderAllItems();
	}

	/**
	 * Renders toolbar of one specific item type.
	 * 
	 * @param index the number representing an item in the toolbar
	 */
	private void renderItem(int index) {

		toolbars.get(index).getChildren().clear();

		ImageView[] items = new ImageView[counters[index]];
		for (int i = 0; i < counters[index]; i++) {
			ImageView item = new ImageView(itemImages.get(index));
			items[i] = item;
			toolbars.get(index).getChildren().add(items[i]);
			makeDraggable(item, index);
		}
	}

	/**
	 * Disables side menu items (makes them non-draggable)
	 */
	private void disableToolbars() {
		for (int i = 0; i < toolbars.size(); i++) {
			toolbars.get(i).getChildren().clear();

			ImageView[] items = new ImageView[counters[i]];
			for (int j = 0; j < counters[i]; j++) {
				ImageView item = new ImageView(itemImages.get(i));
				items[j] = item;
				toolbars.get(i).getChildren().add(items[j]);
			}
		}
	}

	/**
	 * Makes ImageView draggable.
	 *
	 * @param item      item that is being made draggable.
	 * @param dbContent String used in setupCanvasDragBehaviour.
	 */
	private void makeDraggable(final ImageView item, int dbContent) {
		System.out.println("Attempt  to make draggable");
		item.setOnDragDetected(event -> {
			System.out.println("Drag detected");
			Dragboard dragboard = item.startDragAndDrop(TransferMode.MOVE);
			ClipboardContent clipboardContent = new ClipboardContent();
			clipboardContent.putString(String.valueOf(dbContent));
			dragboard.setContent(clipboardContent);
			event.consume();
		});
	}

	/**
	 * Removes rat from a rat counter.
	 *
	 * @param rat the rat being removed.
	 */
	public void ratRemoved(Rat rat) {
		if (rat instanceof AdultMale) {
			maleRatCounter--;
		} else if (rat instanceof AdultFemale) {
			femaleRatCounter--;
		} else if (rat instanceof ChildRat || rat instanceof AdultIntersex) {
			otherRatCounter--;
		}
	}

	/**
	 * Adds rat to rat counter.
	 *
	 * @param rat the rat being added.
	 */
	public void ratAdded(Rat rat) {
		if (rat instanceof ChildRat || rat instanceof AdultIntersex) {
			otherRatCounter++;
		} else if (rat instanceof AdultMale) {
			maleRatCounter++;
		} else if (rat instanceof AdultFemale) {
			femaleRatCounter++;
		}
	}

	/**
	 * Removes rat that has been killed from rat counter and adds to score.
	 *
	 * @param rat rat that has been killed.
	 */
	public void ratKilled(Rat rat) {
		if (rat instanceof AdultFemale) {
			if (((AdultFemale) rat).getRatFetusCount() > 0) {
				for (int i = 0; i < ((AdultFemale) rat).getRatFetusCount(); i++) {
					score += 10;
				}
			}
			score += 10;
			femaleRatCounter--;
		} else if (rat instanceof AdultMale) {
			score += 10;
			maleRatCounter--;
		} else if (rat instanceof ChildRat) {
			score += 10;
			otherRatCounter--;
		} else if (rat instanceof AdultIntersex) {
			if (((AdultIntersex) rat).getRatFetusCount() > 0) {
				for (int i = 0; i < ((AdultIntersex) rat).getRatFetusCount(); i++) {
					score += 10;
				}
			}
			score += 10;
			otherRatCounter--;
		}
	}
}
