import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;

import javafx.application.Platform;

public class EditorClientListener implements Runnable {

	private Socket server;
	// private Tile[][] tileMap;
	private ObjectInputStream inObject;
	private EditorClient editorClient;

	public EditorClientListener(EditorClient editorClient, Socket server) throws IOException {
		this.server = server;
		inObject = new ObjectInputStream(this.server.getInputStream());
		this.editorClient = editorClient;
	}

	@Override
	public void run() {
		try {
			while (true) {

				System.out.println("\nnew listener client loop");
				Object temp = inObject.readObject();
				if (temp instanceof Tile[][] && temp != null) {
					System.out.println("map");
					Platform.runLater(() -> {
						editorClient.setTileMap((Tile[][]) temp);
					});
				} else if (temp instanceof Settings) {
					System.out.println("settings");
					Platform.runLater(() -> {
						editorClient.saveSettings((Settings) temp);
					});
				} else if (temp instanceof Boolean) {
					System.out.println("boolean: " + temp);
					Platform.runLater(() -> {
						editorClient.setReady((Boolean) temp);
					});
				} else if (temp instanceof ReadyStatus) {
					System.out.println("readystatus");
					Platform.runLater(() -> {
						editorClient.setReadyStatus((ReadyStatus) temp);
					});
				} else if (temp instanceof String) {
					System.out.println("string: " + (String) temp);
					Platform.runLater(() -> {
						editorClient.changeLevelName((String) temp);
					});
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			System.err.println(e);
		}

	}
}
