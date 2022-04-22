import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Client {
	private PrintWriter out;
//	private ArrayList<String> createdLevelsNames = new ArrayList<>();
//	private ArrayList<String> defaultLevelsNames = new ArrayList<>();
	private SynchronizedLevelNames defaultLevelsNames = new SynchronizedLevelNames();
	private SynchronizedLevelNames createdLevelsNames = new SynchronizedLevelNames();
	private boolean wiatingForCreatedData = false;
	private boolean wiatingForDefaultData = false;
	private ClientObjectListener clientObjectListener;
	private int port;

	public Client(String ip, int port) throws UnknownHostException, IOException {
		System.out.println("test1");
		Socket server = new Socket(ip, port);
		System.out.println("test2");
		this.out = new PrintWriter(server.getOutputStream(), true);
		this.clientObjectListener = new ClientObjectListener(this, server);
		Thread clientObjectListenerThread = new Thread(clientObjectListener);
		clientObjectListenerThread.start();

		System.out.println("test3");
//		defaultLevelsNames.add("level-1");
		System.out.println(out == null);
		System.out.println(server.getLocalPort() + " connected" + server.getPort());

	}

	public synchronized void updateCreatedLevelsNames(ArrayList<String> createdLevelsNames) {
		this.createdLevelsNames.setLevelsNames(createdLevelsNames);
	}

	public synchronized void updateDefaultLevelsNames(ArrayList<String> defaultLevelsNames) {
		this.defaultLevelsNames.setLevelsNames(defaultLevelsNames);
	}

	private void askForCreatedLevelsNames() {
		out.println("createdLevels");
	}

	private void askForDefaultLevelsNames() {

		out.println("defaultLevels");
	}

	public ArrayList<String> getCreatedLevelsNames() throws InterruptedException {
		this.askForCreatedLevelsNames();
		return createdLevelsNames.getLevelsNames();
	}

	public ArrayList<String> getDefaultLevelsNames() throws InterruptedException {
		this.askForDefaultLevelsNames();
		return defaultLevelsNames.getLevelsNames();
	}

	public synchronized void updatePort(Integer port) {
		this.port = port;
		notifyAll();
	}

	public synchronized int getPort(String clientLevelData) throws InterruptedException {
		out.println("portRequest " + clientLevelData);
		wait();
		return port;
	}

}
