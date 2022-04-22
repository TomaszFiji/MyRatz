import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class Server implements ServerInterface {
	private ServerSocket serverSocket;
	private Scene scene;
	private Stage stage;
	private MenuController menu;
	private ArrayList<Socket> clients = new ArrayList<>();
	private ArrayList<EditorServer> defaultEditorServers = new ArrayList<>();
	private ArrayList<EditorServer> createdEditorServers = new ArrayList<>();
	private ArrayList<CooperationServer> defaultCooperationServers = new ArrayList<>();
	private ArrayList<CooperationServer> createdCooperationServers = new ArrayList<>();
	private ArrayList<String> defaultLevelsNames = new ArrayList<>();
	private ArrayList<String> createdLevelsNames = new ArrayList<>();

	public Server(MenuController menu, Scene scene, Stage stage) throws IOException {
		serverSocket = new ServerSocket(0);

		this.setDefaultLevelsNames();
		this.setCreatedLevelsNames();

		this.createDefaultEditorServers();
		this.createCreatedEditorServers();

		this.createDefaultCooperationServers();
		this.createCreatedCooperationServers();

		ServerAcceptances serverAcceptances = new ServerAcceptances(this, serverSocket);
		Thread serverAcceptancesThread = new Thread(serverAcceptances);
		serverAcceptancesThread.start();

	}

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
						es = new CooperationServer(levelName, menu, scene, stage);
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
						es = new CooperationServer(levelName, menu, scene, stage);
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

	public void addNewGameServer(String levelName) {
		
		createdLevelsNames.add(levelName);

		CooperationServer cooperationServer = new CooperationServer(levelName, menu, scene, stage);
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

	private void createCreatedEditorServers() {
		for (String s : createdLevelsNames) {
			EditorServer editorServer = new EditorServer(s, this);
			defaultEditorServers.add(editorServer);
			try {
				editorServer.runServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void createDefaultCooperationServers() {
		for (String s : defaultLevelsNames) {
			CooperationServer cooperationServer = new CooperationServer(s, menu, scene, stage);
			defaultCooperationServers.add(cooperationServer);
			try {
				cooperationServer.runServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void createCreatedCooperationServers() {
		for (String s : createdLevelsNames) {
			CooperationServer cooperationServer = new CooperationServer(s, menu, scene, stage);
			createdCooperationServers.add(cooperationServer);
			try {
				cooperationServer.runServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public ArrayList<String> getCreatedLevelsNames() {
		return createdLevelsNames;

	}

	public ArrayList<String> getDefaultLevelsNames() {
		return defaultLevelsNames;
	}

	public int getPortOfGameServer(String levelName, String levelType, String serverType) {

		if (levelType.equals("default")) {
			if (serverType.equals("editor")) {
				for (EditorServer es : defaultEditorServers) {
					if (es.getLevelName().equals(levelName)) {
						return es.getPort();
					}
				}
			} else if (serverType.equals("cooperation")) {
				for (CooperationServer cs : defaultCooperationServers) {
					if (cs.getLevelName().equals(levelName)) {
						return cs.getPort();
					}
				}
			} else if (serverType.equals("sabotage")) {

			}
		} else if (levelType.equals("created")) {
			if (serverType.equals("editor")) {
				for (EditorServer es : createdEditorServers) {
					if (es.getLevelName().equals(levelName)) {
						return es.getPort();
					}
				}
			} else if (serverType.equals("cooperation")) {
				for (CooperationServer cs : createdCooperationServers) {
					if (cs.getLevelName().equals(levelName)) {
						return cs.getPort();
					}
				}
			} else if (serverType.equals("sabotage")) {

			}
		}
		return 0;

	}

	public int getPort() {
		return serverSocket.getLocalPort();
	}

	public ArrayList<EditorServer> getEditorServers() {
		return defaultEditorServers;
	}

	public synchronized void addClient(Socket client) throws IOException {
		clients.add(client);
		ServerListener sl = new ServerListener(this, client);
		Thread slThread = new Thread(sl);
		slThread.start();
	}

	public void closeServer() throws IOException {
		serverSocket.close();
	}
}
