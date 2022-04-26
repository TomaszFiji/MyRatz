import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import javax.imageio.ImageIO;

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
		while (true) {
			System.out.println("new client listener loop");
//				Object img = ImageIO.read(ImageIO.createImageInputStream(server.getInputStream()));
			Object temp;
			try {
				temp = inObject.readObject();
				if (temp instanceof LevelNamesObject && temp != null) {
					if (((LevelNamesObject) temp).getIsDefaultNames()) {
						client.updateDefaultLevelsNames(((LevelNamesObject) temp).getLevelNames());
					} else if (!((LevelNamesObject) temp).getIsDefaultNames()) {
						client.updateCreatedLevelsNames(((LevelNamesObject) temp).getLevelNames());
					}
				} else if (temp instanceof Integer) {
					client.updatePort((Integer) temp);
				} else if (temp instanceof Boolean) {
					client.updateDelete((Boolean) temp);
				} else if (temp instanceof String) {
					client.updateMap((String) temp);
				} else{
					System.out.println("\n\nNot recognized object!!! \n\n" + temp.getClass());
				}
			} catch (ClassNotFoundException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
