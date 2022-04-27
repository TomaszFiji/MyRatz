import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class EditorServerThreadObjectOutput {
	private EditorServer server;
	private Socket client;
	private ObjectOutputStream outObject = null;
	private boolean isReady;

	public EditorServerThreadObjectOutput(EditorServer server, Socket client) throws IOException {
		this.server = server;
		this.client = client;
		this.isReady = false;
		this.outObject = new ObjectOutputStream(client.getOutputStream());
	}
	
	public void closeStream() {
		try {
			this.outObject.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	public Socket getClient() {
		return client;
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

	public void setReady(Boolean isReady) {
		try {
			System.out.println("Sending ready " + isReady);
			outObject.writeObject(isReady);
			outObject.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendReadyStatus(ReadyStatus readyStatus) {
		try {
			System.out.println("Sending ready status");
			outObject.writeObject(readyStatus);
			outObject.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendLevelNameMessage(String levelName) {
		try {
			System.out.println("Sending level name message: " + levelName);
			outObject.writeObject(levelName);
			outObject.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
