import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class EditorServerThreadInput implements Runnable{
	private EditorServer server;
	private Socket client;
	private BufferedReader in = null;
	private boolean isReady;

	public EditorServerThreadInput(EditorServer server, Socket client) {
		this.server = server;
		this.client = client;
		this.isReady = false;

		try {
			this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			while (true) {
				System.out.println("input new loop");
				String input = in.readLine();
				String[] inputs = input.split(" ");
				System.out.println("recived: " + input);
				
				switch(inputs[0]) {
				case "rat":
					this.server.ratAdded(inputs);
					break;
				case "tile":
					this.server.tileAdded(inputs);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
