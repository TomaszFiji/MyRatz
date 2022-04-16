import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

public class CooperationClientListener implements Runnable{

	private Socket server;
	private ObjectInputStream inObject;
	private CooperationClient cooperationClient;

	public CooperationClientListener(CooperationClient cooperationClient, Socket server) throws IOException {
		this.server = server;
		inObject = new ObjectInputStream(this.server.getInputStream());
		this.cooperationClient = cooperationClient;
		}

	@Override
	public void run() {
		try {
			while (true) {
				System.out.println("new listener client loop");
				Object temp = inObject.readObject();
				if (temp instanceof Tile[][] && temp != null) {
					cooperationClient.setTileMap((Tile[][]) temp);
				}
				
				System.gc();
			}
		} catch (IOException | ClassNotFoundException e) {
			//e.printStackTrace();
			System.err.println(e);
		}
	}

}
