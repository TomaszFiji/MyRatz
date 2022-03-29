import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class EditorServerThreadObjectOutput implements Runnable {
	private EditorServer server;
	private Socket client;
	private ObjectOutputStream outObject = null;
	private boolean isReady;

	public EditorServerThreadObjectOutput(EditorServer server, Socket client) {
		this.server = server;
		this.client = client;
		this.isReady = false;

		try {
			this.outObject = new ObjectOutputStream(client.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			System.out.println("Sending map");
			Tile[][] temp = server.getTileMap();
//			for (int i = 0; i < temp.length; i++) {
//				for (int j = 0; j < temp[i].length; j++) {
//					System.out.print(temp[i][j].getClass().getName() + " ");
//				}
//				System.out.println();
//			}
			outObject.writeObject(temp);
			outObject.reset();
//			System.out.println(server.getTileMap() + "   " + temp.equals(server.getTileMap()) + "   " + temp);
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}

	}

}
