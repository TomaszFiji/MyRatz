import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javafx.scene.image.Image;

/**
 * Class that implements a client.
 *
 * @author Tomasz Fijalkowski
 * @version 1.0
 */
public class Client {
	private PrintWriter out;
	private SynchronizedLevelNames defaultLevelsNames = new SynchronizedLevelNames();
	private SynchronizedLevelNames createdLevelsNames = new SynchronizedLevelNames();
	private ClientObjectListener clientObjectListener;
	private int port;
	private Boolean isDeleted = false;
	private String mapString = "";

	/**
	 * Client constructor.
	 * @param ip	ip of a server
	 * @param port	port of a server
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Client(String ip, int port) throws UnknownHostException, IOException {
		Socket server = new Socket(ip, port);
		this.out = new PrintWriter(server.getOutputStream(), true);
		this.clientObjectListener = new ClientObjectListener(this, server);

		Thread clientObjectListenerThread = new Thread(clientObjectListener);

		clientObjectListenerThread.start();

		System.out.println(out == null);
		System.out.println(server.getLocalPort() + " connected" + server.getPort());

	}

	/**
	 * Updates stored created level names.
	 * @param createdLevelsNames created level names
	 */
	public synchronized void updateCreatedLevelsNames(ArrayList<String> createdLevelsNames) {
		this.createdLevelsNames.setLevelsNames(createdLevelsNames);
	}

	/**
	 * Updates stored default level names.
	 * @param defaultLevelsNames default level names
	 */
	public synchronized void updateDefaultLevelsNames(ArrayList<String> defaultLevelsNames) {
		this.defaultLevelsNames.setLevelsNames(defaultLevelsNames);
	}

	/**
	 * Asks server for update on created levels names.
	 */
	private void askForCreatedLevelsNames() {
		out.println("createdLevels");
	}

	/**
	 * Asks server for update on default levels names.
	 */
	private void askForDefaultLevelsNames() {

		out.println("defaultLevels");
	}

	/**
	 * Gets stored created levels names.
	 * @return created levels names
	 * @throws InterruptedException
	 */
	public ArrayList<String> getCreatedLevelsNames() throws InterruptedException {
		this.askForCreatedLevelsNames();
		return createdLevelsNames.getLevelsNames();
	}

	/**
	 * Gets stored default levels names.
	 * @return created levels names
	 * @throws InterruptedException
	 */
	public ArrayList<String> getDefaultLevelsNames() throws InterruptedException {
		this.askForDefaultLevelsNames();
		return defaultLevelsNames.getLevelsNames();
	}

	/**
	 * Updates stored port.
	 * @param port port number
	 */
	public synchronized void updatePort(Integer port) {
		this.port = port;
		notifyAll();
	}

	/**
	 * Asks server for update on game server port.
	 * @param clientLevelData	data about game server
	 * @return					port number of game server
	 * @throws InterruptedException
	 */
	public synchronized int getPort(String clientLevelData) throws InterruptedException {
		out.println("portRequest " + clientLevelData);
		wait();
		return port;
	}

	/**
	 * Updated information about deleting the game server.
	 * @param temp
	 */
	public synchronized void updateDelete(Boolean temp) {
		System.out.println("updateDeleteLevel " + temp);
		isDeleted = temp;
		notifyAll();
		System.out.println("updateDeleteLevelAfterNotify " + temp);
	}

	/**
	 * Asks for deleting the game server.
	 * @param temp
	 */
	public synchronized boolean deleteLevel(String levelName) throws InterruptedException {
		System.out.println("deleteLevel " + levelName);
		out.println("deleteLevel " + levelName);
		wait();
		System.out.println("deleteLevelAfterWait " + isDeleted);
		return isDeleted;
	}

	/**
	 * Updates stored map information.
	 * @param temp
	 */
	public synchronized void updateMap(String temp) {
		mapString = temp;
		notifyAll();
	}

	/**
	 * Ask for update on map information.
	 * @param levelName		name of the level
	 * @param levelType		type of the level
	 * @return				map informations
	 * @throws InterruptedException
	 */
	public synchronized String downloadMap(String levelName, String levelType) throws InterruptedException {
		System.out.println("downloadLevel " + levelName + " " + levelType);
		out.println("downloadLevel " + levelName + " " + levelType);
		wait();
		return mapString;
	}

}
