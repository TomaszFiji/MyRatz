import java.io.IOException;
import java.net.Socket;

public interface ServerInterface {
	public void addClient(Socket client) throws IOException;
}
