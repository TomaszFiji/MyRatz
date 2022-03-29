import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * The main class to begin, and the one that handles the main menu
 *
 * @author Tomaszs Fijalkowski
 * @version 1.0
 */
public class Menu extends Application {

	public static final int SERVER_PORT = 2196;
	private static Stage rootStage = null;
	private Scene scene;

	public void run(String[] args) {
		launch(args);
	}

	public void start(Stage primaryStage) throws Exception {
		HighScores.loadData();
		ProfileFileReader.loadData();

		Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("menu2.fxml")));
		if (rootStage == null) {
			rootStage = primaryStage;
		}
		rootStage.setTitle("Ratz");
		rootStage.setResizable(false);
		scene = new Scene(root, 800, 500);
		rootStage.setScene(scene);
		rootStage.show();

		rootStage.setOnCloseRequest(event -> {
			System.out.println("Stage is closing");

			try {
				ProfileFileReader.saveDataToFile();
				HighScores.saveDataToFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public static Stage getStage() {
		return rootStage;
	}

	public void finishLevel() {
		rootStage.setScene(scene);
		rootStage.show();
	}

}
