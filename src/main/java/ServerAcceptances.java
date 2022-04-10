import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
				server.addClient(client);
				System.out.println("clientAdded");
			} catch (IOException e) {

			}
		}
	}

}
