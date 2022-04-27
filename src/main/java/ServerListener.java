import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.SerializationUtils;

/**
 * Class that implements a server listener.
 *
 * @author Tomasz Fijalkowski
 * @version 1.0
 */
public class ServerListener implements Runnable {
	private Server server;
	private BufferedReader in = null;
	private ObjectOutputStream outObject;
	private BufferedOutputStream out;

	/**
	 * Server listener constructor.
	 * @param server	server
	 * @param client	client
	 * @throws IOException
	 */
	public ServerListener(Server server, Socket client) throws IOException {
		this.server = server;
		this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		this.outObject = new ObjectOutputStream(client.getOutputStream());
		this.out = new BufferedOutputStream(client.getOutputStream());
	}

	/**
	 * Periodically listen to client requests.
	 */
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
				case "portRequest":
					// Syntax must follow: "portRequest <levelName> <levelType> <serverType>"
					System.out.println(Integer.valueOf(server.getPortOfGameServer(inputs[1], inputs[2], inputs[3])));
					outObject.writeObject(Integer.valueOf(server.getPortOfGameServer(inputs[1], inputs[2], inputs[3])));
					break;
				case "imageRequested":
					BufferedImage image = ImageIO
							.read(new File("src\\main\\resources\\server_levels\\images\\" + inputs[1] + ".png"));

					ImageIO.write(image, "png", out);
					break;
				case "deleteLevel":
					Boolean temp = server.deleteLevel(inputs[1]);
					System.out.println(temp);
					outObject.writeObject(temp);
					break;
				case "downloadLevel":
					String s = server.downloadMap(inputs[1], inputs[2]);
					System.out.println(s);
					outObject.writeObject(s);
					break;
				}
				outObject.reset();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
