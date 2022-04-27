import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javafx.application.Platform;

public class CooperationServerThreadInput implements Runnable {

	private CooperationServer cooperationServer;
	private Socket client;
	private boolean isReady;
	private BufferedReader in;

	public CooperationServerThreadInput(CooperationServer cooperationServer, Socket client) throws IOException {
		this.cooperationServer = cooperationServer;
		this.client = client;
		this.isReady = false;
		this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
	}

	@Override
	public void run() {
		try {
			while (true) {
				System.out.println("v\n\n\ninput new loop\n\n\n\n^");
				String input = in.readLine();
				String[] inputs = input.split(" ");
				System.out.println("recived: " + input);

				switch (inputs[0]) {
				case "power":
					Platform.runLater(() -> {
						this.cooperationServer.placePower(inputs);
					});
					break;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
