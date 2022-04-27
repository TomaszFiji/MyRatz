import java.io.File;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Class that implements a server.
 *
 * @author Tomasz Fijalkowski
 * @version 1.0
 */
public class Server implements ServerInterface {
	private ServerSocket serverSocket;
	private ArrayList<Socket> clients = new ArrayList<>();
	private ArrayList<EditorServer> defaultEditorServers = new ArrayList<>();
	private ArrayList<EditorServer> createdEditorServers = new ArrayList<>();
	private ArrayList<CooperationServer> defaultCooperationServers = new ArrayList<>();
	private ArrayList<CooperationServer> createdCooperationServers = new ArrayList<>();
	private ArrayList<String> defaultLevelsNames = new ArrayList<>();
	private ArrayList<String> createdLevelsNames = new ArrayList<>();

	/**
	 * Constructor for server class.
	 * @param menu			main menu
	 * @param scene			scene from main menu
	 * @param stage			stage form main menu
	 * @throws IOException	
	 */
	public Server(MenuController menu, Scene scene, Stage stage) throws IOException {
		serverSocket = new ServerSocket(0);

		this.setDefaultLevelsNames();
		this.setCreatedLevelsNames();

		this.createDefaultEditorServers();
		this.createCreatedEditorServers();

		this.createDefaultCooperationServers();
		this.createCreatedCooperationServers();

		System.out.println(defaultEditorServers.size() + " " + createdEditorServers.size() + " "
				+ defaultCooperationServers.size() + " " + createdCooperationServers.size() + " "
				+ defaultLevelsNames.size() + " " + createdLevelsNames.size());

		ServerAcceptances serverAcceptances = new ServerAcceptances(this, serverSocket);
		Thread serverAcceptancesThread = new Thread(serverAcceptances);
		serverAcceptancesThread.start();

	}

	/**
	 * Restarts specified game server
	 * @param levelName		name of the level
	 * @param levelType		type of the level
	 * @param serverType	type of the server
	 */
	public void restartGameServer(String levelName, String levelType, String serverType) {

		if (levelType.equals("default")) {
			if (serverType.equals("editor")) {
				EditorServer temp = null;
				for (EditorServer es : defaultEditorServers) {
					if (es.getLevelName().equals(levelName)) {
						es = new EditorServer(levelName, this);
						try {
							es.runServer();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				defaultEditorServers.remove(temp);
			} else if (serverType.equals("cooperation")) {
				CooperationServer temp = null;
				for (CooperationServer es : defaultCooperationServers) {
					if (es.getLevelName().equals(levelName)) {
						es = new CooperationServer(levelName, this);
						try {
							es.runServer();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				defaultCooperationServers.remove(temp);

			} else if (serverType.equals("sabotage")) {

			}
		} else if (levelType.equals("created")) {

			boolean shouldBeAdded = false;
			for (String s : createdLevelsNames) {
				if (s.equals(levelName)) {
					shouldBeAdded = true;
				}
			}
			if (shouldBeAdded) {
				createdLevelsNames.add(levelName);
			}

			if (serverType.equals("editor")) {
				EditorServer temp = null;
				for (EditorServer es : createdEditorServers) {
					if (es.getLevelName().equals(levelName)) {
						es = new EditorServer(levelName, this);
						try {
							es.runServer();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				defaultEditorServers.remove(temp);
			} else if (serverType.equals("cooperation")) {
				for (CooperationServer es : createdCooperationServers) {
					if (es.getLevelName().equals(levelName)) {
						es = new CooperationServer(levelName, this);
						try {
							es.runServer();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			} else if (serverType.equals("sabotage")) {

			}
		}
	}

	/**
	 * Adds new game servers to a server.
	 * @param levelName	name of level
	 */
	public void addNewGameServer(String levelName) {

		createdLevelsNames.add(levelName);

		CooperationServer cooperationServer = new CooperationServer(levelName, this);
		createdCooperationServers.add(cooperationServer);
		try {
			cooperationServer.runServer();
		} catch (IOException e) {
			e.printStackTrace();
		}

		EditorServer editorServer = new EditorServer(levelName, this);
		createdEditorServers.add(editorServer);
		try {
			editorServer.runServer();
		} catch (IOException e) {
			e.printStackTrace();

		}
	}

	/**
	 * Populate default name array list
	 */
	private void setDefaultLevelsNames() {
		File directoryPath = new File("src/main/resources/server_levels/default_levels");
		System.out.println(directoryPath.getAbsolutePath());

		// List of all files and directories
		String[] contents = directoryPath.list();
		defaultLevelsNames = new ArrayList<>();

		assert contents != null;
		for (String content : contents) {
			String substring = content.substring(0, content.length() - 4);
			defaultLevelsNames.add(substring);
		}
	}

	/**
	 * Populate created name array list
	 */
	private void setCreatedLevelsNames() {
		File directoryPath = new File("src/main/resources/server_levels/created_levels");
		System.out.println(directoryPath.getAbsolutePath());

		// List of all files and directories
		String[] contents = directoryPath.list();
		createdLevelsNames = new ArrayList<>();

		assert contents != null;
		for (String content : contents) {
			String substring = content.substring(0, content.length() - 4);
			createdLevelsNames.add(substring);
		}
	}

	/**
	 * Creates default editor game servers 
	 */
	private void createDefaultEditorServers() {
		for (String s : defaultLevelsNames) {
			EditorServer editorServer = new EditorServer(s, this);
			defaultEditorServers.add(editorServer);
			try {
				editorServer.runServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates created editor game servers 
	 */
	private void createCreatedEditorServers() {
		for (String s : createdLevelsNames) {
			EditorServer editorServer = new EditorServer(s, this);
			createdEditorServers.add(editorServer);
			try {
				editorServer.runServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates default cooperation game servers 
	 */
	private void createDefaultCooperationServers() {
		for (String s : defaultLevelsNames) {
			CooperationServer cooperationServer = new CooperationServer(s, this);
			defaultCooperationServers.add(cooperationServer);
			try {
				cooperationServer.runServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates created cooperation game servers 
	 */
	private void createCreatedCooperationServers() {
		for (String s : createdLevelsNames) {
			CooperationServer cooperationServer = new CooperationServer(s, this);
			createdCooperationServers.add(cooperationServer);
			try {
				cooperationServer.runServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets array list of created level names
	 * @return	created level names
	 */
	public ArrayList<String> getCreatedLevelsNames() {
		return createdLevelsNames;

	}

	/**
	 * Gets array list of default level names
	 * @return	default level names
	 */
	public ArrayList<String> getDefaultLevelsNames() {
		return defaultLevelsNames;
	}

	/**
	 * Gets port of specified game server.
	 * @param levelName		name of the level
	 * @param levelType		type of the level
	 * @param serverType	type of the server
	 * @return				port the game server is running on or 0 if server does not exist 
	 */
	public int getPortOfGameServer(String levelName, String levelType, String serverType) {
		if (levelType.equals("default")) {
			if (serverType.equals("editor")) {
				System.out.println("getPort1");
				for (EditorServer es : defaultEditorServers) {
					System.out.println(levelName + " isEqual? " + es.getLevelName());
					if (es.getLevelName().equals(levelName)) {
						return es.getPort();
					}
				}
			} else if (serverType.equals("cooperation")) {
				System.out.println("getPort2");
				for (CooperationServer cs : defaultCooperationServers) {
					System.out.println(levelName + " isEqual? " + cs.getLevelName());
					if (cs.getLevelName().equals(levelName)) {
						return cs.getPort();
					}
				}
			} else if (serverType.equals("sabotage")) {

			}
		} else if (levelType.equals("created")) {
			if (serverType.equals("editor")) {
				System.out.println("getPort3");
				for (EditorServer es : createdEditorServers) {
					System.out.println(levelName + " isEqual? " + es.getLevelName());
					if (es.getLevelName().equals(levelName)) {
						return es.getPort();
					}
				}
			} else if (serverType.equals("cooperation")) {
				System.out.println("getPort4");
				for (CooperationServer cs : createdCooperationServers) {
					System.out.println(levelName + " isEqual? " + cs.getLevelName());
					if (cs.getLevelName().equals(levelName)) {
						return cs.getPort();
					}
				}
			} else if (serverType.equals("sabotage")) {

			}
		}
		System.out.println("getPortEnd");
		return 0;

	}

	/**
	 * Gets server port.
	 * @return	server port
	 */
	public int getPort() {
		return serverSocket.getLocalPort();
	}

	/**
	 * Adds client to a server
	 * @param client socket representing a client
	 */
	public synchronized void addClient(Socket client) throws IOException {
		clients.add(client);
		ServerListener sl = new ServerListener(this, client);
		Thread slThread = new Thread(sl);
		slThread.start();
	}

	/**
	 * Closes a server
	 * @throws IOException
	 */
	public void closeServer() throws IOException {
		serverSocket.close();
	}

	/**
	 * Deletes specified level and every game server of this name.
	 * @param levelName	name of the level
	 * @return			true if success, false if fails
	 */
	public Boolean deleteLevel(String levelName) {
		String temp = null;
		for (String s : createdLevelsNames) {
			if (s.equals(levelName)) {
				temp = s;
			}
		}
		if (temp != null) {
			createdLevelsNames.remove(temp);
		}

		CooperationServer tempC = null;
		for (CooperationServer s : createdCooperationServers) {
			if (s.getLevelName().equals(levelName)) {
				tempC = s;
			}
		}
		if (tempC != null) {
			createdCooperationServers.remove(tempC);
		}

		EditorServer tempE = null;
		for (EditorServer s : createdEditorServers) {
			if (s.getLevelName().equals(levelName)) {
				tempE = s;
			}
		}
		if (tempE != null) {
			createdEditorServers.remove(tempE);
		}

		File fileToDelete = new File("src\\main\\resources\\server_levels\\created_levels\\" + levelName + ".txt");

		return fileToDelete.delete();
	}

	/**
	 * Gets content of map file.
	 * @param levelName	name of a level
	 * @param levelType	type of a level
	 * @return			content of map file
	 * @throws FileNotFoundException
	 */
	public String downloadMap(String levelName, String levelType) throws FileNotFoundException {
		File levelData = new File("src\\main\\resources\\server_levels\\" + levelType + "\\" + levelName + ".txt");
		Scanner reader = new Scanner(levelData);
		String out = "";
		while (reader.hasNextLine()) {
			out += reader.nextLine() + "\n";
		}
		reader.close();
		return out;
	}
}
