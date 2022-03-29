import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class EditorClientOutput implements Runnable {

	private Socket server;
	private EditorClient editorClient;
	private PrintWriter out;
	private char previousType = 'a';
	private int previousX = -1;
	private int previousY = -1;

	public EditorClientOutput(EditorClient editorClient, Socket server) throws IOException {
		this.server = server;
		this.out = new PrintWriter(server.getOutputStream(), true);
		this.editorClient = editorClient;
	}

	@Override
	public void run() {
	}

	public void placeRat(char type, int x, int y) {
		System.out.print("Sent: rat " + type + " " + x + " " + y + "\n");
		out.println("rat " + type + " " + x + " " + y);
	}

	public void placeTile(char type, int x, int y) {
		if (type != previousType || x != previousX || y != previousY) {
			previousType = type;
			previousX = x;
			previousY = y;
			System.out.print("Sent: tile " + type + " " + x + " " + y + "\n");
			out.println("tile " + type + " " + x + " " + y);
			System.out.println("Sending complete");
		} else {
			System.out.println("Sent: nothing");			
		}
	}
}
