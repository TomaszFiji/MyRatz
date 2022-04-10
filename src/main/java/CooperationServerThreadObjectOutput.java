import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class CooperationServerThreadObjectOutput implements Runnable {

	private CooperationServer server;
	private ObjectOutputStream outObject;
	private Socket client;

	public CooperationServerThreadObjectOutput(CooperationServer cooperationServer, Socket client) throws IOException {
		this.server = server;
		this.client = client;
		this.outObject = new ObjectOutputStream(client.getOutputStream());
	}

	public void run() {}
	
	public void sendMap() {
		try {
			System.out.println("Sending map");
			Tile[][] temp = server.getTileMap();
			outObject.writeObject(temp);
			outObject.reset();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
