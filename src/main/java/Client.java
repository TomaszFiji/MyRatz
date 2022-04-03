import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Client {
	private PrintWriter out;
	private ArrayList<String> createdLevelsNames = new ArrayList<>();
	private ArrayList<String> defaultLevelsNames = new ArrayList<>();
	private boolean wiatingForCreatedData = false;
	private boolean wiatingForDefaultData = false;
	private ClientObjectListener clientObjectListener;

	public Client(String ip, int port) throws UnknownHostException, IOException {
		System.out.println("test1");
		Socket server = new Socket(ip, port);
		System.out.println("test2");
		this.out = new PrintWriter(server.getOutputStream(), true);
		this.clientObjectListener = new ClientObjectListener(this, server);
		Thread clientObjectListenerThread = new Thread(clientObjectListener);
		clientObjectListenerThread.start();
		
		System.out.println("test3");
		defaultLevelsNames.add("level-1");
		System.out.println(out == null);
		System.out.println(server.getLocalPort() + " connected" + server.getPort());

	}

	public synchronized void updateCreatedLevelsNames(ArrayList<String> createdLevelsNames) {
		this.createdLevelsNames = createdLevelsNames;
		wiatingForCreatedData = false;
//		notifyAll();
	}

	public synchronized void updateDefaultLevelsNames(ArrayList<String> defaultLevelsNames) {
		this.defaultLevelsNames = defaultLevelsNames;
		wiatingForDefaultData = false;
//		notifyAll();
	}

	private void askForCreatedLevelsNames() {
		out.println("createdLevels");
	}

	private void askForDefaultLevelsNames() {

		out.println("defaultLevels");
	}

	public ArrayList<String> getCreatedLevelsNames() throws InterruptedException {
		wiatingForCreatedData = true;
		askForCreatedLevelsNames();

		Thread.sleep(1000);

		System.out.println("client: asking for crelev finish" + createdLevelsNames.get(0));
		return createdLevelsNames;
	}

	public ArrayList<String> getDefaultLevelsNames() throws InterruptedException {
		wiatingForDefaultData = true;
		System.out.println("client: asking for deflev start");
		askForDefaultLevelsNames();
		Thread.sleep(1000);
		System.out.println("client: asking for deflev finish" + defaultLevelsNames.get(0));
		return defaultLevelsNames;
	}

}
