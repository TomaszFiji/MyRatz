import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class EditorServerAcceptances implements Runnable {
	private EditorServer server;
	private ServerSocket serverSocket;

	public EditorServerAcceptances(EditorServer server, ServerSocket serverSocket) {
		this.server = server;
		this.serverSocket = serverSocket;
	}

	public void run() {
		while (true) {
			try {
				Socket client = serverSocket.accept();
				server.addClient(client);
			} catch (IOException e) {

			}
		}
	}

}
