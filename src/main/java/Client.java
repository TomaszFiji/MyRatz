import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javafx.scene.image.Image;

public class Client {
	private PrintWriter out;
//	private ArrayList<String> createdLevelsNames = new ArrayList<>();
//	private ArrayList<String> defaultLevelsNames = new ArrayList<>();
	private SynchronizedLevelNames defaultLevelsNames = new SynchronizedLevelNames();
	private SynchronizedLevelNames createdLevelsNames = new SynchronizedLevelNames();
	private boolean wiatingForCreatedData = false;
	private boolean wiatingForDefaultData = false;
	private ClientObjectListener clientObjectListener;
	private ClientListener clientListener;
	private int port;
	private Boolean isDeleted = false;
	private String mapString = "";
//	private SerializableImage img;

	public Client(String ip, int port) throws UnknownHostException, IOException {
		Socket server = new Socket(ip, port);
		this.out = new PrintWriter(server.getOutputStream(), true);
		this.clientObjectListener = new ClientObjectListener(this, server);
		this.clientListener = new ClientListener(this, server);
		
		
		Thread clientListenerThread = new Thread(clientListener);
		Thread clientObjectListenerThread = new Thread(clientObjectListener);
		
		clientObjectListenerThread.start();
//		clientListenerThread.start();

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
	
//	public synchronized void updateImg(SerializableImage img) {
//		this.img = img;
//		notifyAll();
//	}
//
	public synchronized Image getLevelImage(String levelName) throws InterruptedException {
		System.out.println("requesting img " + levelName);
//		out.println("imageRequested " + levelName);
//		wait();
		return null;
	}

	public synchronized void updateDelete(Boolean temp) {
		System.out.println("updateDeleteLevel " + temp);
		isDeleted = temp;
		notifyAll();
		System.out.println("updateDeleteLevelAfterNotify " + temp);
	}
	public synchronized boolean deleteLevel(String levelName) throws InterruptedException {
		System.out.println("deleteLevel " + levelName);
		out.println("deleteLevel " + levelName);
		wait();
		System.out.println("deleteLevelAfterWait " + isDeleted);
		return isDeleted;
	}

	public synchronized void updateMap(String temp) {
		mapString = temp;
		notifyAll();
	}
	
	public synchronized String downloadMap(String levelName, String levelType) throws InterruptedException {
		System.out.println("downloadLevel " + levelName + " " + levelType);
		out.println("downloadLevel " + levelName + " " + levelType);
		wait();
		return mapString;
	}

}
