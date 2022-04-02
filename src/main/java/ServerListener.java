import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerListener implements Runnable {
	private Server server;
	private Socket client;
	private BufferedReader in = null;

	public ServerListener(Server server, Socket client) throws IOException {
		this.server = server;
		this.client = client;
		this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
	}

	public void run() {
		try {
			while (true) {
				System.out.println("input new loop");
				String input = in.readLine();
				String[] inputs = input.split(" ");
				System.out.println("recived: " + input);

				switch (inputs[0]) {
				case "createdLevels":
					
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
