import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Client {
	private PrintWriter out;
	private ArrayList<String> createdLevelsNames = new ArrayList<>();
	private ArrayList<String> defaultLevelsNames = new ArrayList<>();

	public Client(String ip, int port) throws UnknownHostException, IOException {
		System.out.println("test1");
		Socket server = new Socket(ip, port);
		System.out.println("test2");
		this.out = new PrintWriter(server.getOutputStream(), true);
		System.out.println("test3");
		defaultLevelsNames.add("level-1");
		System.out.println(out == null);
		System.out.println(server.getLocalPort() + " connected" + server.getPort());
		
	}
	
	private void askForCreatedLevelsNames() {
		out.println("createdLevels");
	}
	
	private void askForDefaultLevelsNames() {
		out.println("defaultLevels");
	}

	public ArrayList<String> getCreatedLevelsNames() {
		return createdLevelsNames;
	}

	public ArrayList<String> getDefaultLevelsNames() {
		return defaultLevelsNames;
	}

}
