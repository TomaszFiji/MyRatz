import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class EditorServerThreadInput implements Runnable {
	private EditorServer server;
	private Socket client;
	private BufferedReader in = null;
	private boolean isReady;

	public EditorServerThreadInput(EditorServer server, Socket client) throws IOException {
		this.server = server;
		this.client = client;
		this.isReady = false;
		this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
	}

	@Override
	public void run() {
		try {
			while (true) {
				System.out.println("input new loop");
				String input = in.readLine();
				String[] inputs = input.split(" ");
				System.out.println("recived: " + input);

				switch (inputs[0]) {
				case "rat":
					this.server.ratAdded(inputs);
					break;
				case "tile":
					this.server.tileAdded(inputs);
					break;
				case "size":
					this.server.changeLevelSize(inputs);
					break;
				case "settings":
					this.server.saveSettings(inputs);
					break;
				case "readyStatus":
					this.server.setReady(client, inputs[1]);
					break;
				case "levelName":
					this.server.changeName(client, inputs);
					break;
				}
				
				Thread.sleep(0);
			}
		} catch (IOException | InterruptedException e) {
			try {
				this.in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

	}
}
