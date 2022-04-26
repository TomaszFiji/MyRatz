import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

public class ClientListener implements Runnable {

	private Socket server;
	// private Tile[][] tileMap;
	private InputStream inputStream;
	private Client client;

	public ClientListener(Client client, Socket server) throws IOException {
		this.server = server;
		inputStream = this.server.getInputStream();
		this.client = client;
	}

	@Override
	public void run() {
		try {
			while (true) {
				BufferedImage img = ImageIO.read(ImageIO.createImageInputStream(server.getInputStream()));
				
				System.out.println("recived img" + img.getWidth());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
