import static java.lang.Integer.parseInt;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

import javax.imageio.ImageIO;

import javafx.animation.FadeTransition;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class EditorClient implements Controller {
	private static final String SERVER_IP = "127.0.0.1";
	private EditorClientListener clientListener;
	private EditorClientOutput clientOutput;
	private boolean isReady = false;

	// Size of one tile in pixels
	private static final int TILE_SIZE = 64;
	private static final int MILLIS_RATIO = 1000;

	// Time between item drops
	private final int[] dropRates;

	private final String levelName;
	private final MenuController MAIN_MENU;

	// Size of game map
	private int width;
	private int height;

	// Game losing conditions (maximum number of rats, time taken for a level)
	private int maxRats;
	private int parTime;

	// Current tile selected to draw
	private Tile selectedTile = new Grass();

    @FXML
    private TextField bombTextField;
    private TextField deathRatTextField;
    private TextField femaleSwapTextField;
    private TextField gameTimerTextField;
    private TextField gasTextField;
    private Text heightText;
    private TextField heightTextField;
    private Canvas levelCanvas;
    private Text levelNameText;
    private TextField levelNameTextField;
    private Button levelSettingsButton;
    private TextField maleSwapTextField;
    private TextField maxRatTextField;
    private TextField poisonTextField;
    private HBox ratSpawnToolbar;
    private RadioButton rbGrass;
    private RadioButton rbGrassB;
    private RadioButton rbPath;
    private RadioButton rbPathB;
    private RadioButton rbTunnel;
    private RadioButton rbTunnelB;
    private Button readyButton;
    private Text readyPlayersText;
    private Pane settingsDialoguePane;
    private Text settingsErrorText;
    private Button sizeChangeButton;
    private Text sizeChangeErrorText;
    private TextField sterilizationTextField;
    private TextField stopSignTextField;
    private Text widthText;
    private TextField widthTextField;
	public TextField[] powerTextFields;

	// Level map
	private Tile[][] tileMap = new Tile[0][0];

	public void runTheGame(String selectedEditLevelName, boolean isDefaultLevel, Scene scene, Stage stage,
			MenuController menu) throws IOException {

		FXMLLoader loader = new FXMLLoader(getClass().getResource("editorClient.fxml"));

		if (isDefaultLevel) {
			LevelFileReader.loadNormalLevelFile(this,
					"src/main/resources/levels/default_levels/" + selectedEditLevelName, true);
		} else {
			LevelFileReader.loadNormalLevelFile(this,
					"src/main/resources/levels/created_levels/" + selectedEditLevelName, true);
		}

		loader.setController(this);
		Pane root = loader.load();

		scene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		stage.setScene(scene);
		stage.show();
	}

	public void runClient(int port) throws IOException, ClassNotFoundException, InterruptedException {
		Socket server = new Socket(SERVER_IP, port);
		clientListener = new EditorClientListener(this, server);
		clientOutput = new EditorClientOutput(this, server);
		Thread clientListenerThread = new Thread(clientListener);
		
		clientListenerThread.start();

		System.out.println("runned*******************************");
	}

	public void setTileMap(Tile[][] tileMap) {
		this.tileMap = tileMap.clone();
		this.width = tileMap.length;
		this.height = tileMap[0].length;
		widthText.setText(String.valueOf(width));
		heightText.setText(String.valueOf(height));
		System.out.println("Tile map changed");
		renderBoard();
	}

	public Tile[][] getTileMap() {
		return tileMap;
	}

	/**
	 * Constructor for EditorController when a level is being made from scratch.
	 * 
	 * @param mainMenuController reference to main menu.
	 */
	public EditorClient(MenuController mainMenuController) {
		MAIN_MENU = mainMenuController;
		width = 10;
		height = 7;
		tileMap = new Tile[width][height];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				tileMap[i][j] = new Grass();
			}
		}
		maxRats = 20;
		parTime = 150;
		dropRates = new int[8];
		Arrays.fill(dropRates, 1);
		levelName = "";
	}

	/**
	 * Constructor for EditorController when an existing level is being edited.
	 * 
	 * @param levelName          name of original existing level.
	 * @param mainMenuController reference to main menu.
	 */
	public EditorClient(String levelName, MenuController mainMenuController) {
		this.levelName = levelName;
		MAIN_MENU = mainMenuController;

		width = LevelFileReader.getWidth();
		height = LevelFileReader.getHeight();

		tileMap = LevelFileReader.getTileMap();
		//changeToAdultRats();

		maxRats = LevelFileReader.getMaxRats();
		parTime = LevelFileReader.getParTime();
		dropRates = LevelFileReader.getDropRates();
		for (int i = 0; i < dropRates.length; i++) {
			dropRates[i] = dropRates[i] / MILLIS_RATIO;
		}
	}

	/**
	 * Initializes FXML elements and editor data.
	 */
	public void initialize() {
		renderBoard();
		setupRadioButtons();
		setupDraggableSpawns();
		setupCanvasDrawing();
		setupCanvasDragBehaviour();

		heightText.setText(String.valueOf(height));
		widthText.setText(String.valueOf(width));

		powerTextFields = new TextField[] { bombTextField, gasTextField, sterilizationTextField, poisonTextField,
				maleSwapTextField, femaleSwapTextField, stopSignTextField, deathRatTextField };
		for (int i = 0; i < dropRates.length; i++) {
			powerTextFields[i].setText(String.valueOf(dropRates[i]));
		}

		maxRatTextField.setText(String.valueOf(maxRats));
		gameTimerTextField.setText(String.valueOf(parTime));
		levelNameText.setText(levelName);
	}

	/**
	 * Sets up the ability to drag things onto canvas.
	 */
	private void setupCanvasDragBehaviour() {

		levelCanvas.setOnDragOver(event -> {
			int x = (int) event.getX() / TILE_SIZE;
			int y = (int) event.getY() / TILE_SIZE;

			if (event.getGestureSource() instanceof ImageView) {
				if (x < width && x >= 0 && y >= 0 && y < height) { // if x and y are in the size of the tilemap
					if (tileMap[x][y] instanceof Path || tileMap[x][y] instanceof PathB) { // if the tile at (x,y) is a
																							// path
						event.acceptTransferModes(TransferMode.ANY);
					}
				}
				event.consume();
			}
		});

		// This code allows the canvas to react to a dragged object when it is finally
		// dropped.
		levelCanvas.setOnDragDropped(event -> {
			String dbContent = event.getDragboard().getString();
			char ratType = dbContent.charAt(0);
			spawnDropped(event, ratType);
		});
	}

	/**
	 * Handles dropping things onto canvas.
	 * 
	 * @param event drag event.
	 * @param type  type of item dropped.
	 */
	private void spawnDropped(DragEvent event, char type) {
		int x = (int) event.getX() / TILE_SIZE;
		int y = (int) event.getY() / TILE_SIZE;

		clientOutput.placeRat(type, x, y);

	}

	/**
	 * Sets up ability to drag rat spawns onto tilemap.
	 */
	private void setupDraggableSpawns() {
		AdultMale adultMale = new AdultMale(this, 1, Rat.Direction.NORTH, 0, 0, 0, false);
		AdultFemale adultFemale = new AdultFemale(this, 1, Rat.Direction.NORTH, 0, 0, 0, false, 0, 0);
		AdultIntersex adultIntersex = new AdultIntersex(this, 1, Rat.Direction.NORTH, 0, 0, 0, false, 0, 0);

		ImageView adultMaleImageView = new ImageView(adultMale.getImg());
		ImageView adultFemaleImageView = new ImageView(adultFemale.getImg());
		ImageView adultIntersexImageView = new ImageView(adultIntersex.getImg());
		ImageView deleteImageView = new ImageView(new Image("file:target/classes/delete.png"));

		ratSpawnToolbar.getChildren().add(adultMaleImageView);
		ratSpawnToolbar.getChildren().add(adultFemaleImageView);
		ratSpawnToolbar.getChildren().add(adultIntersexImageView);
		ratSpawnToolbar.getChildren().add(deleteImageView);

		makeDraggable(adultMaleImageView, 'm');
		makeDraggable(adultFemaleImageView, 'f');
		makeDraggable(adultIntersexImageView, 'i');
		makeDraggable(deleteImageView, 'd');
	}

	/**
	 * Makes ImageView draggable onto tilemap.
	 * 
	 * @param item image view to be made draggable.
	 * @param type type of item.
	 */
	private static void makeDraggable(final ImageView item, char type) {
		item.setOnDragDetected(event -> {
			Dragboard dragboard = item.startDragAndDrop(TransferMode.MOVE);
			ClipboardContent clipboardContent = new ClipboardContent();
			clipboardContent.putString(String.valueOf(type));
			dragboard.setContent(clipboardContent);
			event.consume();
		});
	}

	/**
	 * Sets up the ability to draw selected tile onto tilemap.
	 */
	private void setupCanvasDrawing() {
		levelCanvas.setOnMousePressed(event -> {
			int x = (int) event.getX() / TILE_SIZE;
			int y = (int) event.getY() / TILE_SIZE;

			if (x < width - 1 && x >= 1 && y >= 1 && y < height - 1) {
				if ((!(tileMap[x][y].getClass() == selectedTile.getClass()))) {
					if (selectedTile instanceof Grass) {
						System.out.println("grass " + x + " " + y);
						clientOutput.placeTile('g', x, y);
					} else if (selectedTile instanceof GrassB) {
						System.out.println("grassB " + x + " " + y);
						clientOutput.placeTile('G', x, y);
					} else if (selectedTile instanceof Path) {
						System.out.println("path " + x + " " + y);
						clientOutput.placeTile('p', x, y);
					} else if (selectedTile instanceof PathB) {
						System.out.println("pathB " + x + " " + y);
						clientOutput.placeTile('P', x, y);
					} else if (selectedTile instanceof Tunnel) {
						System.out.println("tunnel " + x + " " + y);
						clientOutput.placeTile('t', x, y);
					} else if (selectedTile instanceof TunnelB) {
						System.out.println("tunnelB " + x + " " + y);
						clientOutput.placeTile('T', x, y);
					} else {
						System.out.println("Mouse pressed something wrong");
					}
				} else {
					System.out.println("Mouse pressed2 something wrong");
				}
			} else {
				System.out.println("Mouse pressed3 something wrong");
			}
			System.out.println("Mouse Pressed done " + x + " " + y);
		});
		levelCanvas.setOnMouseDragged(event -> {
			int x = (int) event.getX() / TILE_SIZE;
			int y = (int) event.getY() / TILE_SIZE;

			if (x < width - 1 && x >= 1 && y >= 1 && y < height - 1) {
				if ((!(tileMap[x][y].getClass() == selectedTile.getClass()))) {
					if (selectedTile instanceof Grass) {
						System.out.println("grass " + x + " " + y);
						clientOutput.placeTile('g', x, y);
					} else if (selectedTile instanceof GrassB) {
						System.out.println("grassB " + x + " " + y);
						clientOutput.placeTile('G', x, y);
					} else if (selectedTile instanceof Path) {
						System.out.println("path " + x + " " + y);
						clientOutput.placeTile('p', x, y);
					} else if (selectedTile instanceof PathB) {
						System.out.println("pathB " + x + " " + y);
						clientOutput.placeTile('P', x, y);
					} else if (selectedTile instanceof Tunnel) {
						System.out.println("tunnel " + x + " " + y);
						clientOutput.placeTile('t', x, y);
					} else if (selectedTile instanceof TunnelB) {
						System.out.println("tunnelB " + x + " " + y);
						clientOutput.placeTile('T', x, y);
					} else {
						System.out.println("Mouse dragged something wrong");
					}
				}
			}
			System.out.println("Mouse dragged done");
		});
	}

	/**
	 * Sets up radio buttons for grass/path/tunnel selection.
	 */
	private void setupRadioButtons() {
		final ToggleGroup tileRadioButtons = new ToggleGroup();

		rbGrass.setToggleGroup(tileRadioButtons);
		rbGrassB.setToggleGroup(tileRadioButtons);
		rbPath.setToggleGroup(tileRadioButtons);
		rbPathB.setToggleGroup(tileRadioButtons);
		rbTunnel.setToggleGroup(tileRadioButtons);
		rbTunnelB.setToggleGroup(tileRadioButtons);

		ImageView grassImageView = new ImageView(new Grass().getImg());
		ImageView grassBImageView = new ImageView(new GrassB().getImg());
		ImageView pathImageView = new ImageView(new Path().getImg());
		ImageView pathBImageView = new ImageView(new PathB().getImg());
		ImageView tunnelImageView = new ImageView(new Tunnel().getImg());
		ImageView tunnelBImageView = new ImageView(new TunnelB().getImg());

		ImageView grassImageViewSelected = new ImageView(new Image("file:target/classes/grass_selected.png"));
		ImageView grassBImageViewSelected = new ImageView(new Image("file:target/classes/grassb_selected.png"));
		ImageView pathImageViewSelected = new ImageView(new Image("file:target/classes/path_selected.png"));
		ImageView pathBImageViewSelected = new ImageView(new Image("file:target/classes/pathb_selected.png"));
		ImageView tunnelImageViewSelected = new ImageView(new Image("file:target/classes/tunnel_selected.png"));
		ImageView tunnelBImageViewSelected = new ImageView(new Image("file:target/classes/tunnelb_selected.png"));

		rbGrass.setSelected(true);

		rbGrass.setGraphic(grassImageViewSelected);
		rbGrassB.setGraphic(grassBImageView);
		rbPath.setGraphic(pathImageView);
		rbPathB.setGraphic(pathBImageView);
		rbTunnel.setGraphic(tunnelImageView);
		rbTunnelB.setGraphic(tunnelBImageView);

		tileRadioButtons.selectedToggleProperty().addListener((ob, o, n) -> {
			if (rbGrass.isSelected()) {
				selectedTile = new Grass();
				rbGrass.setGraphic(grassImageViewSelected);
				rbGrassB.setGraphic(grassBImageView);
				rbPath.setGraphic(pathImageView);
				rbPathB.setGraphic(pathBImageView);
				rbTunnel.setGraphic(tunnelImageView);
				rbTunnelB.setGraphic(tunnelBImageView);
			} else if (rbGrassB.isSelected()) {
				selectedTile = new GrassB();
				rbGrass.setGraphic(grassImageView);
				rbGrassB.setGraphic(grassBImageViewSelected);
				rbPath.setGraphic(pathImageView);
				rbPathB.setGraphic(pathBImageView);
				rbTunnel.setGraphic(tunnelImageView);
				rbTunnelB.setGraphic(tunnelBImageView);
			} else if (rbPath.isSelected()) {
				selectedTile = new Path();
				rbGrass.setGraphic(grassImageView);
				rbGrassB.setGraphic(grassBImageView);
				rbPath.setGraphic(pathImageViewSelected);
				rbPathB.setGraphic(pathBImageView);
				rbTunnel.setGraphic(tunnelImageView);
				rbTunnelB.setGraphic(tunnelBImageView);
			} else if (rbPathB.isSelected()) {
				selectedTile = new PathB();
				rbGrass.setGraphic(grassImageView);
				rbGrassB.setGraphic(grassBImageView);
				rbPath.setGraphic(pathImageView);
				rbPathB.setGraphic(pathBImageViewSelected);
				rbTunnel.setGraphic(tunnelImageView);
				rbTunnelB.setGraphic(tunnelBImageView);
			} else if (rbTunnel.isSelected()) {
				selectedTile = new Tunnel();
				rbGrass.setGraphic(grassImageView);
				rbGrassB.setGraphic(grassBImageView);
				rbPath.setGraphic(pathImageView);
				rbPathB.setGraphic(pathBImageView);
				rbTunnel.setGraphic(tunnelImageViewSelected);
				rbTunnelB.setGraphic(tunnelBImageView);
			} else if (rbTunnelB.isSelected()) {
				selectedTile = new TunnelB();
				rbGrass.setGraphic(grassImageView);
				rbGrassB.setGraphic(grassBImageView);
				rbPath.setGraphic(pathImageView);
				rbPathB.setGraphic(pathBImageView);
				rbTunnel.setGraphic(tunnelImageView);
				rbTunnelB.setGraphic(tunnelBImageViewSelected);
			}
		});
	}

	/**
	 * Renders tilemap onto window.
	 */
	private void renderBoard() {
		GraphicsContext gc = levelCanvas.getGraphicsContext2D();

		gc.setFill(Color.web("#2d4945"));
		gc.fillRect(width * TILE_SIZE, 0, levelCanvas.getWidth(), levelCanvas.getHeight());
		gc.fillRect(0, height * TILE_SIZE, levelCanvas.getWidth(), levelCanvas.getHeight());

		if (tileMap != null) {
			// TODO fix magic nums
			for (int i = 0; i < tileMap.length; i++) {
				for (int j = 0; j < tileMap[i].length; j++) {
					tileMap[i][j].draw(i, j, gc);

				}
			}
		}
		System.out.println("Finished rendering \n");
	}

	public void requestChangeLevelSize() {
		int newWidth = parseInt(widthTextField.getText());
		int newHeight = parseInt(heightTextField.getText());

		clientOutput.changeLevelSize(newWidth, newHeight);
	}

	public void requestChangeLevelName() {
		String newName = levelNameTextField.getText();

		clientOutput.changeLevelName(newName);
	}

	public void changeLevelName(String levelNameMessage) {
		if (levelNameMessage.contains(" ")) {
			sizeChangeErrorText.setText(levelNameMessage);
			this.applyFadingEffect(sizeChangeErrorText);
		} else {
			levelNameText.setText(levelNameMessage);
		}
	}

	/**
	 * Changes level size once "Apply Changes" is pressed. Unless invalid input, in
	 * which case it prompts the user to change their input.
	 */
	@FXML
	public void changeLevelSize() {
		try {
			int newWidth = parseInt(widthTextField.getText());
			int newHeight = parseInt(heightTextField.getText());
			if (newWidth > (levelCanvas.getWidth() / TILE_SIZE) || newWidth < 3
					|| newHeight > (levelCanvas.getHeight() / TILE_SIZE) || newHeight < 3) {
				sizeChangeErrorText.setText("Maximum map size: 16x14");
				this.applyFadingEffect(sizeChangeErrorText);
			} else {
				sizeChangeErrorText.setText("");
				requestChangeLevelSize();
			}
		} catch (NumberFormatException nfe) {
			sizeChangeErrorText.setText("Please enter an integer number");
			this.applyFadingEffect(sizeChangeErrorText);
		}
	}

	/**
	 * Changes size of tile map. For levels that need to be made bigger, replaces
	 * empty space with grass.
	 * 
	 * @param newWidth  width of new tile map.
	 * @param newHeight height of new tile map.
	 */
	/*
	 * private void changeTileMapSize(int newWidth, int newHeight) { Tile[][]
	 * newTileMap = new Tile[newWidth][newHeight];
	 * 
	 * for (int i = 0; i < newWidth; i++) { for (int j = 0; j < newHeight; j++) { if
	 * ((i >= tileMap.length) || (j >= tileMap[0].length) || (i == newWidth - 1) ||
	 * (j == newHeight - 1)) { newTileMap[i][j] = new Grass(); } else {
	 * newTileMap[i][j] = tileMap[i][j]; } } } tileMap = newTileMap; width =
	 * newWidth; height = newHeight; renderBoard(); }
	 */

	/**
	 * Displays level settings box when button is pressed.
	 */
	public void displayLevelSettings() {
		setButtonDisabling(true);
		disableCanvas();
		settingsDialoguePane.setVisible(true);
	}

	public void saveSettings(Settings settings) {
		maxRatTextField.setText(String.valueOf(settings.getMaxRats()));
		gameTimerTextField.setText(String.valueOf(settings.getParTime()));
		for (int i = 0; i < settings.getDropRates().length; i++) {
			powerTextFields[i].setText(String.valueOf(settings.getDropRate(i)));
		}

		maxRats = settings.getMaxRats();
		parTime = settings.getParTime();
		for (int i = 0; i < dropRates.length; i++) {
			dropRates[i] = settings.getDropRate(i);
		}
	}

	/**
	 * Saves level settings when button is pressed, unless the user has made invalid
	 * input. If they have, it prompts the user to enter something else
	 */
	public void saveSettings() {
		try {
			int maxRatsLocal = parseInt(maxRatTextField.getText());
			int parTimeLocal = parseInt(gameTimerTextField.getText());
			boolean wrongDropRate = false;
			int[] dropRatesLocal = new int[dropRates.length];
			for (int i = 0; i < dropRatesLocal.length; i++) {
				dropRatesLocal[i] = parseInt(powerTextFields[i].getText());
				if (dropRatesLocal[i] < 0) {
					wrongDropRate = true;
				}
			}
			if (maxRatsLocal <= getNumOfRats()) {
				settingsErrorText.setText("Please enter a valid number of rats.");
			} else if (parTimeLocal < 0) {
				settingsErrorText.setText("Please enter a valid game time.");
			} else if (wrongDropRate) {
				settingsErrorText.setText("Please enter valid drop rates.");
			} else {
				settingsErrorText.setText("");
				settingsDialoguePane.setVisible(false);
				setupCanvasDrawing();
				setupCanvasDragBehaviour();
				setButtonDisabling(false);
				clientOutput.saveSettings(maxRatsLocal, parTimeLocal, dropRatesLocal);
			}
			this.applyFadingEffect(settingsErrorText);
		} catch (NumberFormatException nfe) {
			settingsErrorText.setText("Please enter integer numbers only.");
			this.applyFadingEffect(settingsErrorText);
		}
	}

	public void closeSettings() {
		settingsErrorText.setText("");
		settingsDialoguePane.setVisible(false);
		setupCanvasDrawing();
		setupCanvasDragBehaviour();
		setButtonDisabling(false);

		maxRatTextField.setText(String.valueOf(maxRats));
		gameTimerTextField.setText(String.valueOf(parTime));
		for (int i = 0; i < dropRates.length; i++) {
			powerTextFields[i].setText(String.valueOf(dropRates[i]));
		}
	}

	/**
	 * Gets current number of rats on the board.
	 * 
	 * @return number of rats.
	 */
	private int getNumOfRats() {
		int num = 0;
		for (Tile[] tiles : tileMap) {
			for (Tile tile : tiles) {
				if (tile.getOccupantRats().size() != 0) {
					num++;
				}
			}
		}
		return num;
	}

	/**
	 * Disables all canvas events.
	 */
	private void disableCanvas() {
		levelCanvas.setOnMousePressed(null);
		levelCanvas.setOnMouseDragged(null);
		levelCanvas.setOnDragDropped(null);
		levelCanvas.setOnDragOver(null);
	}

	/**
	 * Sets button disabling for all side panel buttons.
	 * 
	 * @param val whether buttons should be disabled.
	 */
	private void setButtonDisabling(boolean val) {
		rbGrass.setDisable(val);
		rbGrassB.setDisable(val);
		rbTunnel.setDisable(val);
		rbTunnelB.setDisable(val);
		rbPath.setDisable(val);
		rbPathB.setDisable(val);
		levelSettingsButton.setDisable(val);
		sizeChangeButton.setDisable(val);
		widthTextField.setDisable(val);
		heightTextField.setDisable(val);
	}

	@Override
	public Tile getTileAt(int x, int y) {
		return null;
	}

	@Override
	public void ratKilled(Rat rat) {

	}

	@Override
	public void ratAdded(Rat rat) {

	}

	@Override
	public void ratRemoved(Rat rat) {

	}

	@Override
	public int[] getCounters() {
		return null;
	}

	@Override
	public int getCurrentTimeLeft() {
		return 0;
	}

	@Override
	public void addPowersFromSave(int[] inProgInv) {

	}

	@FXML
	void readyButtonAction(ActionEvent event) {
		clientOutput.setReady(!isReady);
	}

	public void setReady(Boolean temp) {
		isReady = temp;
		if (temp) {
			readyButton.setStyle("-fx-background-image: url('gui/red-menu-button1.png')");
			readyButton.setText("Cancel");
		} else {
			readyButton.setStyle("-fx-background-image: url('gui/green-editor-button.png')");
			readyButton.setText("I'm ready");
		}
	}

	public void setReadyStatus(ReadyStatus temp) {
		readyPlayersText.setText(temp.getReadyPlayers() + "/" + temp.getAllPlayers());
		levelNameText.setText(temp.getLevelName());
		if (temp.isGameFinished()) {
			sizeChangeErrorText.setText("Game is saving");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			MAIN_MENU.finishLevel();
		}
	}
	
	private void applyFadingEffect(Node node) {
		FadeTransition ft = new FadeTransition(Duration.millis(3000), node);
		ft.setFromValue(1.0);
		ft.setToValue(0.0);
		ft.play();
	}

	public void stageClosing() {
		clientOutput.stageClosing();
	}
	
	public void leave() {
		clientOutput.stageClosing();
		MAIN_MENU.finishLevel();
	}
}
