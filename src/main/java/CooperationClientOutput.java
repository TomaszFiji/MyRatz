import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class CooperationClientOutput implements Runnable{

	private Socket server;
	private PrintWriter out;
	private CooperationClient cooperationClient;

	public CooperationClientOutput(CooperationClient cooperationClient, Socket server) throws IOException {
		this.server = server;
		this.out = new PrintWriter(server.getOutputStream(), true);
		this.cooperationClient = cooperationClient;
	}

	@Override
	public void run() {		
	}
	
	public void placePower(int type, int x, int y) {
		System.out.print("Sent: power " + type + " " + x + " " + y + "\n");
		out.println("power " + type + " " + x + " " + y);
	}

}
