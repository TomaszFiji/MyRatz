import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class ServerListener implements Runnable {
	private Server server;
	private Socket client;
	private BufferedReader in = null;
	private ObjectOutputStream outObject;

	public ServerListener(Server server, Socket client) throws IOException {
		this.server = server;
		this.client = client;
		this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.outObject = new ObjectOutputStream(client.getOutputStream());
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
					outObject.writeObject(new LevelNamesObject(server.getCreatedLevelsNames(), false));
					break;
				case "defaultLevels":
					outObject.writeObject(new LevelNamesObject(server.getDefaultLevelsNames(), true));
					break;
				}
				outObject.reset();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
