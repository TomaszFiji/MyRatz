import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import javafx.scene.Scene;
import javafx.scene.image.Image;
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

		System.out.println(defaultEditorServers.size() + " " + createdEditorServers.size() + " "
				+ defaultCooperationServers.size() + " " + createdCooperationServers.size() + " "
				+ defaultLevelsNames.size() + " " + createdLevelsNames.size());

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
			createdEditorServers.add(editorServer);
			try {
				editorServer.runServer();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

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

	public ArrayList<String> getCreatedLevelsNames() {
		return createdLevelsNames;

	}

	public ArrayList<String> getDefaultLevelsNames() {
		return defaultLevelsNames;
	}

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

	public Image getLevelImg(String levelName) throws IOException {
		File f = new File("src\\main\\resources\\server_levels\\images\\" + levelName + ".png");

		if (f.exists()) {
			FileInputStream tempStream = new FileInputStream(
					"src\\main\\resources\\server_levels\\images\\" + levelName + ".png");
			Image tempImg = new Image(tempStream);

			int width = (int) tempImg.getWidth();
			int height = (int) tempImg.getHeight();
			float widthCompare = (float) 390 / (float) width;
			float heightCompare = (float) 350 / (float) height;

			if (widthCompare < heightCompare) {
				width *= widthCompare;
				height *= widthCompare;
			} else {
				width *= heightCompare;
				height *= heightCompare;
			}
			FileInputStream tempStream2 = new FileInputStream(
					"src\\main\\resources\\server_levels\\images\\" + levelName + ".png");
//			SerializableImage img = new SerializableImage(tempStream2, width, height, false, false);

			tempStream.close();
			tempStream2.close();
//			return img;
		}
		return null;
	}

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

	public String downloadMap(String s, String levelType) throws FileNotFoundException {
		File levelData = new File("src\\main\\resources\\server_levels\\" + levelType + "\\" + s + ".txt");
		Scanner reader = new Scanner(levelData);
		String out = "";
		while (reader.hasNextLine()) {
			out += reader.nextLine() + "\n";
		}
		return out;
	}
}
