import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class EditorServerThread implements Runnable{
	private Socket client;
	private ArrayList<EditorServerThread> clients;
	private PrintWriter out = null;
	private BufferedReader in = null;
	private  ObjectOutputStream outObject = null;
    
	public EditorServerThread(Socket client, ArrayList<EditorServerThread>  clients) {
		this.client = client;
		this.clients = clients;
		
		try {
			this.out = new PrintWriter(client.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			this.outObject = new ObjectOutputStream(client.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public void run() {
		
	}
	
	
}
