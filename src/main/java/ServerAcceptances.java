import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.application.Platform;

public class ServerAcceptances implements Runnable {
	private ServerInterface server;
	private ServerSocket serverSocket;

	public ServerAcceptances(ServerInterface server, ServerSocket serverSocket) {
		this.server = server;
		this.serverSocket = serverSocket;
	}

	public void run() {
		while (true) {
			try {
				Socket client = serverSocket.accept();
				Platform.runLater(() -> {
					try {
						server.addClient(client);
					} catch (IOException e) {
						e.printStackTrace();
					}
				});
				System.out.println("clientAdded");
			} catch (IOException e) {

			}
		}
	}

}
