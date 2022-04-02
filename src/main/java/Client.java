import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
	public Client(String ip, int port) throws UnknownHostException, IOException {
		Socket server = new Socket(ip, port);
		System.out.println(server.getLocalPort() + " connected" + server.getPort());
		
	}

}
