import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javafx.scene.Scene;
import javafx.stage.Stage;

public class Server {
	private ServerSocket serverSocket;
	private ArrayList<Socket> clients = new ArrayList<>();
	private ArrayList<EditorServer> editorServers = new ArrayList<>();

	public Server(MenuController menu, Scene scene, Stage stage) throws IOException {
		serverSocket = new ServerSocket(0);
		
		for (int i = 1; i <= 5; i++) {
			EditorServer editorServer = new EditorServer("level-" + i, menu, scene, stage);
			editorServers.add(editorServer);
			//editorServer.runServer();
		}
		
		ServerAcceptances serverAcceptances = new ServerAcceptances(this, serverSocket);
		Thread serverAcceptancesThread = new Thread(serverAcceptances);
		serverAcceptancesThread.start();

	}
	
	public int getPort() {
		return serverSocket.getLocalPort();
	}
	
	public ArrayList<EditorServer> getEditorServers(){
		return editorServers;
	}

	public synchronized void addClient(Socket client) {
		clients.add(client);
	}

	public void closeServer() throws IOException {
		serverSocket.close();
	}
}
