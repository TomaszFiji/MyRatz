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
	}

	public void sendMap() {
		try {
			System.out.println("Sending map");
			Tile[][] temp = server.getTileMap();
			outObject.writeObject(temp);
			outObject.reset();
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}
	}

	public void sendSettings(Settings settings) {
		try {
			System.out.println("Sending settings");
			outObject.writeObject(settings);
			outObject.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
