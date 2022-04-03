import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class ClientObjectListener implements Runnable {

	private Socket server;
	// private Tile[][] tileMap;
	private ObjectInputStream inObject;
	private Client client;

	public ClientObjectListener(Client client, Socket server) throws IOException {
		this.server = server;
		inObject = new ObjectInputStream(this.server.getInputStream());
		this.client = client;
	}

	@Override
	public void run() {
		try {
			while (true) {

				Object temp = inObject.readObject();
				if (temp instanceof LevelNamesObject && temp != null) {
					if (((LevelNamesObject) temp).getIsDefaultNames()) {
						client.updateDefaultLevelsNames(((LevelNamesObject) temp).getLevelNames());
					} else if (!((LevelNamesObject) temp).getIsDefaultNames()) {
						client.updateCreatedLevelsNames(((LevelNamesObject) temp).getLevelNames());
					}
				} else if (temp instanceof Integer) {
					client.updatePort((Integer) temp);
				}
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

}
