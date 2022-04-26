import static java.lang.Integer.parseInt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

public class EditorServer implements Controller, ServerInterface {
	private ServerSocket editorServer;
	private int port;
	private int counterOfClients = 0;
	private final String selectedEditLevelName;
	private boolean isDefaultLevel = true;
	private boolean isMapLoaded = true;
	private ArrayList<EditorServerThreadObjectOutput> clientsObjectsOutputs = new ArrayList<>();
	private ArrayList<EditorServerThreadInput> clientsInputs = new ArrayList<>();
	private ArrayList<Thread> clientsInputsThreads = new ArrayList<>();
	private HashMap<Socket, Boolean> clients = new HashMap<>();

	// Size of one tile in pixels
	private static final int TILE_SIZE = 64;
	private static final String defaultLevelRegex = "level-[1-5]";
	private static final int MILLIS_RATIO = 1000;

	// Time between item drops
	private int[] dropRates;

	private String levelName;
	private MenuController MAIN_MENU;

	// Size of game map
	private int width;
	private int height;

	// Game losing conditions (maximum number of rats, time taken for a level)
	private int maxRats;
	private int parTime;

	// Current tile selected to draw
	private Tile selectedTile = new Grass();

	@FXML
	public Canvas levelCanvas;

	public RadioButton rbGrass;
	public RadioButton rbGrassB;
	public RadioButton rbPath;
	public RadioButton rbPathB;
	public RadioButton rbTunnel;
	public RadioButton rbTunnelB;

	public HBox ratSpawnToolbar;

	public TextField widthTextField;
	public TextField heightTextField;
	public Text sizeChangeErrorText;

	public Pane settingsDialoguePane;

	public TextField bombTextField;
	public TextField gasTextField;
	public TextField sterilizationTextField;
	public TextField poisonTextField;
	public TextField maleSwapTextField;
	public TextField femaleSwapTextField;
	public TextField stopSignTextField;
	public TextField deathRatTextField;

	public TextField maxRatTextField;
	public TextField gameTimerTextField;

	public Text settingsErrorText;

	public Pane saveLevelPane;
	public TextField levelNameTextField;
	public Text savingErrorText;

	public Button sizeChangeButton;
	public Button levelSettingsButton;
	public Button saveLevelButton;
	public Button saveAndExitButton;
	public TextField[] powerTextFields;

	// Level map
	private Tile[][] tileMap = new Tile[0][0];
	private Server server;

	public synchronized Tile[][] getTileMap() throws InterruptedException {
		// this.removeControllerFromMap();
		return tileMap;
	}

	/**
	 * Constructor for EditorController when a level is being made from scratch.
	 * 
	 * @param mainMenuController reference to main menu.
	 */
	public EditorServer(MenuController mainMenuController) {

		this.selectedEditLevelName = "";
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
	public EditorServer(String levelName, Server server) {
		this.server = server;
		this.isMapLoaded = true;
		this.isDefaultLevel = levelName.matches(defaultLevelRegex);
//		this.scene = scene;
//		this.stage = stage;
		this.selectedEditLevelName = levelName;
		this.levelName = levelName;
		dropRates = new int[8];
		Arrays.fill(dropRates, 1);
//		MAIN_MENU = mainMenuController;

//		width = LevelFileReader.getWidth();
//		height = LevelFileReader.getHeight();
//
//		tileMap = LevelFileReader.getTileMap();
//		changeToAdultRats();
//
//		maxRats = LevelFileReader.getMaxRats();
//		parTime = LevelFileReader.getParTime();
//		dropRates = LevelFileReader.getDropRates();
//		for (int i = 0; i < dropRates.length; i++) {
//			dropRates[i] = dropRates[i] / MILLIS_RATIO;
//		}
	}

	public String getLevelName() {
		return selectedEditLevelName;
	}

	private void removeControllerFromMap() {
		for (Tile[] tileList : tileMap) {
			for (Tile t : tileList) {
				t.setController(null);

				for (Power p : t.getActivePowers()) {
					p.setController(null);
				}

				for (Rat r : t.getOccupantRats()) {
					r.setController(null);
				}
			}
		}
	}

	public void runTheGame() throws IOException {
		FXMLLoader loader = new FXMLLoader(getClass().getResource("editor.fxml"));
		loader.setController(this);
		loader.load();
		
		if (isDefaultLevel) {
			LevelFileReader.loadNormalLevelFile(this,
					"src/main/resources/server_levels/default_levels/" + selectedEditLevelName, true);
		} else {
			LevelFileReader.loadNormalLevelFile(this,
					"src/main/resources/server_levels/created_levels/" + selectedEditLevelName, true);
		}

		width = LevelFileReader.getWidth();
		height = LevelFileReader.getHeight();

		tileMap = LevelFileReader.getTileMap();
		changeToAdultRats();

		maxRats = LevelFileReader.getMaxRats();
		parTime = LevelFileReader.getParTime();
		dropRates = LevelFileReader.getDropRates();
		for (int i = 0; i < dropRates.length; i++) {
			dropRates[i] = dropRates[i] / MILLIS_RATIO;
		}
		this.removeControllerFromMap();
	}

	public void runServer() throws IOException {
		editorServer = new ServerSocket(0);
		System.out.println(editorServer.getLocalSocketAddress() + " " + InetAddress.getLocalHost().getHostAddress()
				+ " " + editorServer.getLocalPort() + " " + this.levelName);
		this.port = editorServer.getLocalPort();

		System.out.println("initializing");
		ServerAcceptances esa = new ServerAcceptances(this, editorServer);
		Thread saThread = new Thread(esa);
		saThread.start();
	}

	public int getPort() {
		return editorServer.getLocalPort();
	}

	private void setAllNotReady() {
		Set<Socket> temp = clients.keySet();

		for (Socket c : temp) {
			clients.replace(c, false);

		}

		for (EditorServerThreadObjectOutput c : clientsObjectsOutputs) {
			c.setReady(false);
			c.sendReadyStatus(new ReadyStatus(0, clients.size(), levelName));
		}
	}

	public synchronized void setReady(Socket client, String isReady) {

		if (getNumOfRats() <= 1) {
			getClientOutputByClient(client).sendLevelNameMessage("Please add more rat spawns to the level.");
		} else if (maxRats <= getNumOfRats()) {
			getClientOutputByClient(client).sendLevelNameMessage("Please fix level settings before saving.");
		} else if (levelName == null || levelName.equals("")) {
			getClientOutputByClient(client).sendLevelNameMessage("Level name cannot be empty");
		} else if (levelName.matches(defaultLevelRegex)) {
			getClientOutputByClient(client).sendLevelNameMessage("Level name cannot be the same as default level");
		} else if (doesLevelExist(levelName)) {
			getClientOutputByClient(client).sendLevelNameMessage("Level name already exist");
		} else {
			Boolean isReadyBoolean;
			if (isReady.equals("true")) {
				isReadyBoolean = true;
			} else {
				isReadyBoolean = false;
			}
			clients.replace(client, isReadyBoolean);

			int readyPlayers = 0;
			Set<Socket> temp = clients.keySet();

			for (Socket c : temp) {
				if (clients.get(c)) {
					readyPlayers++;
				}
			}

			for (EditorServerThreadObjectOutput c : clientsObjectsOutputs) {
				if (c.getClient().equals(client)) {
					c.setReady(isReadyBoolean);
				}
				c.sendReadyStatus(new ReadyStatus(readyPlayers, clients.size(), levelName));
			}

			if (!clients.containsValue(false)) {
				for (EditorServerThreadObjectOutput c : clientsObjectsOutputs) {
					c.sendReadyStatus(new ReadyStatus(readyPlayers, clients.size(), levelName, true));
				}

				for (Socket c : clients.keySet()) {
					try {
						c.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				this.saveLevel();

			}
		}
	}

	public synchronized void addClient(Socket client) throws IOException {
		clients.put(client, false);
		EditorServerThreadObjectOutput clientOutput = new EditorServerThreadObjectOutput(this, client);
		EditorServerThreadInput clientInput = new EditorServerThreadInput(this, client);

		clientsObjectsOutputs.add(clientOutput);
		clientsInputs.add(clientInput);

		System.out.println("new threads");

		Thread clientInputThread = new Thread(clientInput);

		clientsInputsThreads.add(clientInputThread);

		clientInputThread.start();

		counterOfClients++;
		if (counterOfClients == 1) {
			runTheGame();
		}
		this.setAllNotReady();
		clientOutput.sendLevelNameMessage(levelName);
		clientOutput.sendMap();
		this.setReady(client, "false");
		clientOutput.sendSettings(new Settings(maxRats, parTime, dropRates));
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

		heightTextField.setText(String.valueOf(height));
		widthTextField.setText(String.valueOf(width));

		powerTextFields = new TextField[] { bombTextField, gasTextField, sterilizationTextField, poisonTextField,
				maleSwapTextField, femaleSwapTextField, stopSignTextField, deathRatTextField };
		for (int i = 0; i < dropRates.length; i++) {
			powerTextFields[i].setText(String.valueOf(dropRates[i]));
		}

		maxRatTextField.setText(String.valueOf(maxRats));
		gameTimerTextField.setText(String.valueOf(parTime));
		levelNameTextField.setText(levelName);
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

	public synchronized void ratAdded(String[] inputs) {
		int x = Integer.parseInt(inputs[2]);
		int y = Integer.parseInt(inputs[3]);

		if (tileMap[x][y].getOccupantRats().size() != 0) {
			tileMap[x][y].getOccupantRats().clear();
		}
		switch (inputs[1].charAt(0)) {
		case 'm':
			tileMap[x][y].addOccupantRat(new AdultMale(null, 1, Rat.Direction.NORTH, 0, 0, 0, false));
			System.out.println("male " + x + " " + y + " " + tileMap);
			break;
		case 'f':
			tileMap[x][y].addOccupantRat(new AdultFemale(null, 1, Rat.Direction.NORTH, 0, 0, 0, false, 0, 0));
			System.out.println("female " + x + " " + y);
			break;
		case 'i':
			tileMap[x][y].addOccupantRat(new AdultIntersex(null, 1, Rat.Direction.NORTH, 0, 0, 0, false, 0, 0));
			System.out.println("intersex " + x + " " + y);
			break;
		}

		renderBoard();
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

		if (tileMap[x][y].getOccupantRats().size() != 0) {
			tileMap[x][y].getOccupantRats().clear();
		}
		switch (type) {
		case 'm':
			tileMap[x][y].addOccupantRat(new AdultMale(null, 1, Rat.Direction.NORTH, 0, 0, 0, false));
			System.out.println("male " + x + " " + y + " " + tileMap);
			break;
		case 'f':
			tileMap[x][y].addOccupantRat(new AdultFemale(null, 1, Rat.Direction.NORTH, 0, 0, 0, false, 0, 0));
			System.out.println("female " + x + " " + y);
			break;
		case 'i':
			tileMap[x][y].addOccupantRat(new AdultIntersex(null, 1, Rat.Direction.NORTH, 0, 0, 0, false, 0, 0));
			System.out.println("intersex " + x + " " + y);
			break;
		}

		renderBoard();
	}

	/**
	 * Sets up ability to drag rat spawns onto tilemap.
	 */
	private void setupDraggableSpawns() {
		AdultMale adultMale = new AdultMale(null, 1, Rat.Direction.NORTH, 0, 0, 0, false);
		AdultFemale adultFemale = new AdultFemale(null, 1, Rat.Direction.NORTH, 0, 0, 0, false, 0, 0);
		AdultIntersex adultIntersex = new AdultIntersex(null, 1, Rat.Direction.NORTH, 0, 0, 0, false, 0, 0);

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

	public synchronized void tileAdded(String[] inputs) {
		char type = inputs[1].charAt(0);
		int x = Integer.parseInt(inputs[2]);
		int y = Integer.parseInt(inputs[3]);
		switch (type) {
		case 'g':
			tileMap[x][y] = new Grass();
			break;
		case 'G':
			tileMap[x][y] = new GrassB();
			break;
		case 'p':
			tileMap[x][y] = new Path();
			break;
		case 'P':
			tileMap[x][y] = new PathB();
			break;
		case 't':
			tileMap[x][y] = new Tunnel();
			break;
		case 'T':
			tileMap[x][y] = new TunnelB();
			break;
		}
		renderBoard();
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
						tileMap[x][y] = new Grass();
						System.out.println("grass " + x + " " + y);
					} else if (selectedTile instanceof GrassB) {
						tileMap[x][y] = new GrassB();
						System.out.println("grassB " + x + " " + y);
					} else if (selectedTile instanceof Path) {
						tileMap[x][y] = new Path();
						System.out.println("path " + x + " " + y);
					} else if (selectedTile instanceof PathB) {
						tileMap[x][y] = new PathB();
						System.out.println("pathB " + x + " " + y);
					} else if (selectedTile instanceof Tunnel) {
						tileMap[x][y] = new Tunnel();
						System.out.println("tunnel " + x + " " + y);
					} else if (selectedTile instanceof TunnelB) {
						tileMap[x][y] = new TunnelB();
						System.out.println("tunnelB " + x + " " + y);
					}
					renderBoard();
				}
			}
		});
		levelCanvas.setOnMouseDragged(event -> {
			int x = (int) event.getX() / TILE_SIZE;
			int y = (int) event.getY() / TILE_SIZE;

			if (x < width - 1 && x >= 1 && y >= 1 && y < height - 1) {
				if ((!(tileMap[x][y].getClass() == selectedTile.getClass()))) {
					if (selectedTile instanceof Grass) {
						tileMap[x][y] = new Grass();
						System.out.println("grass " + x + " " + y);
					} else if (selectedTile instanceof GrassB) {
						tileMap[x][y] = new GrassB();
						System.out.println("grassB " + x + " " + y);
					} else if (selectedTile instanceof Path) {
						tileMap[x][y] = new Path();
						System.out.println("path " + x + " " + y);
					} else if (selectedTile instanceof PathB) {
						tileMap[x][y] = new PathB();
						System.out.println("pathB " + x + " " + y);
					} else if (selectedTile instanceof Tunnel) {
						tileMap[x][y] = new Tunnel();
						System.out.println("tunnel " + x + " " + y);
					} else if (selectedTile instanceof TunnelB) {
						tileMap[x][y] = new TunnelB();
						System.out.println("tunnelB " + x + " " + y);
					}
					renderBoard();
				}
			}
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
//		GraphicsContext gc = levelCanvas.getGraphicsContext2D();
//
//		gc.setFill(Color.web("#2d4945"));
//		gc.fillRect(0, 0, levelCanvas.getWidth(), levelCanvas.getHeight());

//		if (tileMap != null) {
//			for (int i = 0; i < tileMap.length; i++) {
//				for (int j = 0; j < tileMap[i].length; j++) {
//					tileMap[i][j].draw(i, j, gc);
//				}
//			}
//		}

		this.setAllNotReady();
		for (EditorServerThreadObjectOutput est : clientsObjectsOutputs) {
			est.sendMap();
		}
	}

	public void changeLevelSize(String[] inputs) {
		try {
			int newWidth = parseInt(inputs[1]);
			int newHeight = parseInt(inputs[2]);
			if (newWidth > (levelCanvas.getWidth() / TILE_SIZE) || newWidth < 3
					|| newHeight > (levelCanvas.getHeight() / TILE_SIZE) || newHeight < 3) {
				sizeChangeErrorText.setText("Maximum map size: 16x14");
			} else {
				sizeChangeErrorText.setText("");
				changeTileMapSize(newWidth, newHeight);
			}
		} catch (NumberFormatException nfe) {
			sizeChangeErrorText.setText("Please enter an integer number");
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
			} else {
				sizeChangeErrorText.setText("");
				changeTileMapSize(newWidth, newHeight);
			}
		} catch (NumberFormatException nfe) {
			sizeChangeErrorText.setText("Please enter an integer number");
		}
	}

	/**
	 * Changes size of tile map. For levels that need to be made bigger, replaces
	 * empty space with grass.
	 * 
	 * @param newWidth  width of new tile map.
	 * @param newHeight height of new tile map.
	 */
	private void changeTileMapSize(int newWidth, int newHeight) {
		Tile[][] newTileMap = new Tile[newWidth][newHeight];

		for (int i = 0; i < newWidth; i++) {
			for (int j = 0; j < newHeight; j++) {
				if ((i >= tileMap.length) || (j >= tileMap[0].length) || (i == newWidth - 1) || (j == newHeight - 1)) {
					newTileMap[i][j] = new Grass();
				} else {
					newTileMap[i][j] = tileMap[i][j];
				}
			}
		}
		tileMap = newTileMap;
		width = newWidth;
		height = newHeight;
		renderBoard();
	}

	/**
	 * Displays level settings box when button is pressed.
	 */
	public void displayLevelSettings() {
		setButtonDisabling(true);
		disableCanvas();
		settingsDialoguePane.setVisible(true);
	}

	public void saveSettings(String[] inputs) {
		maxRatTextField.setText(inputs[1]);
		gameTimerTextField.setText(inputs[2]);
		for (int i = 0; i < dropRates.length; i++) {
			powerTextFields[i].setText(inputs[i + 3]);
		}

		this.saveSettings();
	}

	/**
	 * Saves level settings when button is pressed, unless the user has made invalid
	 * input. If they have, it prompts the user to enter something else
	 */
	public void saveSettings() {
		try {
			maxRats = parseInt(maxRatTextField.getText());
			parTime = parseInt(gameTimerTextField.getText());
			boolean wrongDropRate = false;
			for (int i = 0; i < dropRates.length; i++) {
				dropRates[i] = parseInt(powerTextFields[i].getText());
				if (dropRates[i] < 0) {
					wrongDropRate = true;
				}
			}

			this.setAllNotReady();
			for (EditorServerThreadObjectOutput est : clientsObjectsOutputs) {
				est.sendSettings(new Settings(maxRats, parTime, dropRates));
			}
		} catch (NumberFormatException nfe) {
			settingsErrorText.setText("Please enter integer numbers only.");
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
		saveLevelButton.setDisable(val);
		levelSettingsButton.setDisable(val);
		sizeChangeButton.setDisable(val);
		widthTextField.setDisable(val);
		heightTextField.setDisable(val);
	}

	/**
	 * Opens level saving dialogue box.
	 */
	@FXML
	public void openSavingDialogueBox() {
		saveLevelPane.setVisible(true);
		setButtonDisabling(true);
		disableCanvas();
		if (getNumOfRats() <= 1) {
			savingErrorText.setText("Please add more rat spawns to the level.");
			saveAndExitButton.setDisable(true);
		} else if (maxRats <= getNumOfRats()) {
			savingErrorText.setText("Please fix level settings before saving.");
			saveAndExitButton.setDisable(true);
		} else {
			saveAndExitButton.setDisable(false);
			savingErrorText.setText("");
		}
	}

	/**
	 * Goes back to editor from level saving dialogue box.
	 */
	public void goBackToEditor() {
		saveLevelPane.setVisible(false);
		setupCanvasDrawing();
		setupCanvasDragBehaviour();
		setButtonDisabling(false);
	}

	/**
	 * Changes all baby rats on tile map to adult ones.
	 */
	private void changeToAdultRats() {
		for (int i = 0; i < tileMap.length; i++) {
			for (int j = 0; j < tileMap[i].length; j++) {
				if (tileMap[i][j].getOccupantRats().size() != 0) {
					ChildRat rat = (ChildRat) tileMap[i][j].getOccupantRats().get(0);
					if (rat.getRatSex() == Rat.Sex.MALE) {
						tileMap[i][j].removeOccupantRat(rat);
						tileMap[i][j].addOccupantRat(new AdultMale(null, 6, Rat.Direction.NORTH, 0, i, j, true));
					} else if (rat.getRatSex() == Rat.Sex.FEMALE) {
						tileMap[i][j].removeOccupantRat(rat);
						tileMap[i][j]
								.addOccupantRat(new AdultFemale(null, 6, Rat.Direction.NORTH, 0, i, j, true, 0, 0));
					} else if (rat.getRatSex() == Rat.Sex.INTERSEX) {
						tileMap[i][j].removeOccupantRat(rat);
						tileMap[i][j]
								.addOccupantRat(new AdultIntersex(null, 6, Rat.Direction.NORTH, 0, i, j, true, 0, 0));
					}
				}
			}
		}
	}

	/**
	 * Changes all adult rats on tile map to adult ones.
	 */
	private void changeToBabyRats() {
		for (int i = 0; i < tileMap.length; i++) {
			for (int j = 0; j < tileMap[i].length; j++) {
				if (tileMap[i][j].getOccupantRats().size() != 0) {
					Rat rat = tileMap[i][j].getOccupantRats().get(0);
					if (rat instanceof AdultMale) {
						tileMap[i][j].removeOccupantRat(rat);
						tileMap[i][j].addOccupantRat(
								new ChildRat(null, 4, Rat.Direction.NORTH, 0, i, j, true, 0, Rat.Sex.MALE));
					} else if (rat instanceof AdultFemale) {
						tileMap[i][j].removeOccupantRat(rat);
						tileMap[i][j].addOccupantRat(
								new ChildRat(null, 4, Rat.Direction.NORTH, 0, i, j, true, 0, Rat.Sex.FEMALE));
					} else if (rat instanceof AdultIntersex) {
						tileMap[i][j].removeOccupantRat(rat);
						tileMap[i][j].addOccupantRat(
								new ChildRat(null, 4, Rat.Direction.NORTH, 0, i, j, true, 0, Rat.Sex.INTERSEX));
					}
				}
			}
		}
	}

	/**
	 * Saves level once "Save Level" button is pressed.
	 */
	@FXML
	public void saveLevel() {
//		String newLevelName = levelNameTextField.getText();
//		savingErrorText.setText("");

		changeToBabyRats();
		for (int i = 0; i < dropRates.length; i++) {
			dropRates[i] = dropRates[i] * MILLIS_RATIO;
		}

		SaveCustomLevel save = new SaveCustomLevel("src\\main\\resources\\server_levels\\created_levels\\" + levelName,
				width, height, tileMap, maxRats, parTime, dropRates);

//		makeScreenShot(levelName);
		System.out.println("Screenshot was saved");

		for (EditorServerThreadObjectOutput e : clientsObjectsOutputs) {
			e.closeStream();
		}
		for (Thread e : clientsInputsThreads) {
			e.interrupt();
		}

		try {
			editorServer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean isDefault = selectedEditLevelName.matches(defaultLevelRegex);
		if (isDefault) {
			server.restartGameServer(selectedEditLevelName, "default", "editor");
		} else {
			server.restartGameServer(selectedEditLevelName, "created", "editor");
		}
		server.addNewGameServer(levelName);

	}

	public void changeName(Socket client, String[] inputs) {
		System.out.println("    change menu function is called. : " + inputs);
		if (inputs.length > 2) {
			getClientOutputByClient(client).sendLevelNameMessage("Level name cannot contain spaces");
		} else if (inputs.length == 1) {
			getClientOutputByClient(client).sendLevelNameMessage("Level name cannot be empty");
		} else if (inputs[1].matches(defaultLevelRegex)) {
			getClientOutputByClient(client).sendLevelNameMessage("Level name cannot be the same as default level");
		} else if (doesLevelExist(inputs[1])) {
			getClientOutputByClient(client).sendLevelNameMessage("Level name already exist");
		} else {
			levelName = inputs[1];
			this.setAllNotReady();
			for (EditorServerThreadObjectOutput est : clientsObjectsOutputs) {
				est.sendLevelNameMessage(levelName);
			}
		}
	}

	private boolean doesLevelExist(String levelName) {
		File directoryPath = new File("src/main/resources/server_levels/created_levels");
		// System.out.println(directoryPath.getAbsolutePath());

		// List of all files and directories
		String[] contents = directoryPath.list();

		assert contents != null;
		for (String content : contents) {
			String substring = content.substring(0, content.length() - 4);
			if (substring.equals(levelName)) {
				return true;
			}
		}

		return false;
	}

	private EditorServerThreadObjectOutput getClientOutputByClient(Socket client) {
		for (EditorServerThreadObjectOutput c : clientsObjectsOutputs) {
			if (c.getClient().equals(client)) {
				return c;
			}
		}
		return null;
	}

	private EditorServerThreadInput getClientInputByClient(Socket client) {
		for (EditorServerThreadInput c : clientsInputs) {
			if (c.getClient().equals(client)) {
				return c;
			}
		}
		return null;
	}

	/**
	 * Makes screenshot of current tilemap.
	 * 
	 * @param levelName name of level being screenshot.
	 */
	public void makeScreenShot(String levelName) {
		File file = new File("src\\main\\resources\\levels_images\\" + levelName + ".png");

		WritableImage writableImage = new WritableImage(TILE_SIZE * width, TILE_SIZE * height);
		SnapshotParameters params = new SnapshotParameters();
		levelCanvas.snapshot(params, writableImage);

		try {
			ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
		} catch (Throwable th) {
		}

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

	public void clientLeaving(Socket client) {
		System.out.println("Client leaving");
		clients.remove(client);
		int index = this.clientsInputs.indexOf(this.getClientInputByClient(client));
		this.clientsInputs.remove(index);
		this.clientsObjectsOutputs.remove(index);
		this.clientsInputsThreads.get(index).interrupt();
		this.clientsInputsThreads.remove(index);
		this.setAllNotReady();
	}

}
