import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

public class EditorServerThread implements Runnable {
	private EditorServer server;
	private Socket client;
	private ArrayList<EditorServerThread> clients;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private ObjectOutputStream outObject = null;
	private boolean isReady;

	public EditorServerThread(EditorServer server, Socket client, ArrayList<EditorServerThread> clients) {
		this.server = server;
		this.client = client;
		this.clients = clients;
		this.isReady = false;

		try {
			this.out = new PrintWriter(client.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			this.outObject = new ObjectOutputStream(client.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			while (true) {
				Thread.sleep(1000);
				System.out.println("Sending map");
				Tile[][] temp = server.getTileMap();
				for (int i = 0; i < temp.length; i++) {
					for (int j = 0; j < temp[i].length; j++) {
						System.out.print(temp[i][j].getClass().getName() + " ");
					}
					System.out.println();
				}
				outObject.writeObject(temp);
				outObject.reset();
				//System.out.println(temp);
				System.out.println(server.getTileMap() + "   " + temp.equals(server.getTileMap()) + "   " + temp);
//				Random random = new Random();
//				Integer m = random.nextInt(20);
//				String s = m.toString();
//				outObject.writeObject(new String(s));
//				System.out.println(s);
//				out.println("test");
//				System.out.println("test");
				
				
			}
		} catch (InterruptedException | IOException e) {
			e.printStackTrace();
		}

	}

}
