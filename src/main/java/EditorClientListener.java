import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.Socket;

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

				Object temp = inObject.readObject();
				if (temp instanceof Tile[][] && temp != null) {
					editorClient.setTileMap((Tile[][]) temp);
				} else if (temp instanceof Settings) {
					editorClient.saveSettings((Settings) temp);
				}
//				for (int i = 0; i < tileMap.length; i++) {
//					for (int j = 0; j < tileMap[i].length; j++) {
//						System.out.print(tileMap[i][j].getClass().getName() + " ");
//					}
//					System.out.println();
//				}
//				System.out.println(editorClient.getTileMap() + "   " + tileMap.equals(editorClient.getTileMap()) + "   "
//						+ tileMap);
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}

	}
}
