import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class Server implements ServerInterface{
	private ServerSocket serverSocket;
	private ArrayList<Socket> clients = new ArrayList<>();
	private ArrayList<EditorServer> editorServers = new ArrayList<>();

	public Server(MenuController menu, Scene scene, Stage stage) throws IOException {
		serverSocket = new ServerSocket(0);

		for (int i = 1; i <= 5; i++) {
			EditorServer editorServer = new EditorServer("level-" + i, menu, scene, stage);
			editorServers.add(editorServer);
			editorServer.runServer();
		}

		ServerAcceptances serverAcceptances = new ServerAcceptances(this, serverSocket);
		Thread serverAcceptancesThread = new Thread(serverAcceptances);
		serverAcceptancesThread.start();

	}

	public ArrayList<String> getCreatedLevelsNames() {
		ArrayList<String> createdLevels = new ArrayList<>();
		createdLevels.add("test1");
		createdLevels.add("test2");
		createdLevels.add("test3");
		return createdLevels;

	}

	public ArrayList<String> getDefaultLevelsNames() {
		ArrayList<String> defaultLevelsNames = new ArrayList<>();
		defaultLevelsNames.add("level-1");
		defaultLevelsNames.add("level-2");
		defaultLevelsNames.add("level-3");
		defaultLevelsNames.add("level-4");
		defaultLevelsNames.add("level-5");
		return defaultLevelsNames;
	}

	public int getPortOfGameServer(String levelName) {
		for (EditorServer es : editorServers) {
			if (es.getLevelName().equals(levelName)) {
				return es.getPort();
			}
		}
		return 0;

	}

	public int getPort() {
		return serverSocket.getLocalPort();
	}

	public ArrayList<EditorServer> getEditorServers() {
		return editorServers;
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
