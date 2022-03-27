import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class EditorServer {
	private static final int PORT = 2137;
    private static ArrayList<EditorServerThread>  clients = new ArrayList<>();
    private static ExecutorService pool = Executors.newFixedThreadPool(4);
    
    public EditorServer(String selectedEditLevelName, boolean isDefaultLevel, Scene scene, Stage stage, MenuController menu) throws IOException {
    	FXMLLoader loader = new FXMLLoader(getClass().getResource("editor.fxml"));

		if (isDefaultLevel) {
			LevelFileReader.loadNormalLevelFile("src/main/resources/levels/default_levels/" + selectedEditLevelName,
					true);
		} else {
			LevelFileReader.loadNormalLevelFile("src/main/resources/levels/created_levels/" + selectedEditLevelName,
					true);
		}

		EditorController editorController = new EditorController(selectedEditLevelName, menu);
		loader.setController(editorController);
		Pane root = loader.load();

		scene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
		stage.setScene(scene);
		stage.show();
    }
    
    public void runServer() throws IOException {
    	ServerSocket editorServer = new ServerSocket(PORT);

        System.out.println("initializing");
        while (true) {
            System.out.println("waiting..");
            Socket client = editorServer.accept();
            System.out.println("con");
            EditorServerThread cl = new EditorServerThread(client, clients);
            System.out.println("new thread");

            clients.add(cl);
            System.out.println("connected");
            pool.execute(cl);
        }
    }
}
