import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Class to manage high scores.
 *
 * @author Tomasz Fijalkowski
 * @version 1.0
 */
public class HighScores {
	// Path to a file storing data about scores.
	private static final String FILE_PATH = "src/main/resources/highScores2.txt";

	// Maximum number of saved scores per level.
	// private static final int NUMBER_OF_TOP_SCORES = 10;

	private static final ArrayList<LevelScores> levelsScores = new ArrayList<>();

	/**
	 * Load data from text file to memory. Should be use only once at the start of
	 * the program.
	 * 
	 * @throws FileNotFoundException file not found
	 */
	public static void loadData() throws FileNotFoundException {
		File file = new File(FILE_PATH);
		Scanner in = new Scanner(file);

		String levelName;
		levelsScores.clear();
		while (in.hasNext()) {
			String profileName = in.next(); // or level name
			int score = in.nextInt(); // or pointer to next level data

			if (score == -1) {
				levelName = profileName;
				LevelScores levelScores = new LevelScores(levelName);
				levelsScores.add(levelScores);
			} else {
				Score scoreToLoad = new Score(profileName, score);
				levelsScores.get(levelsScores.size() - 1).saveScore(scoreToLoad);
			}

		}

		in.close();
		System.gc();
	}

	/**
	 * Saves data from memory to a file. Can be used many times (but it might be use
	 * only while switching the scenes). Strongly recommended using at the end of
	 * the program (otherwise changes are lost).
	 *
	 */
	public static void saveDataToFile() throws IOException {
		File file = new File(FILE_PATH);
		File tempFile = new File("temp.txt");
		// Scanner in = new Scanner(file);
		FileWriter fileWriter = new FileWriter(tempFile, true);
		PrintWriter printWriter = new PrintWriter(fileWriter);

		for (LevelScores lvlScr : levelsScores) {
			printWriter.println(lvlScr.getLevelName() + " -1");
			Score[] scoresToSave = lvlScr.getTopScores();
			for (Score scr : scoresToSave) {
				if (scr != null) {
					printWriter.println(scr.getProfileName() + " " + scr.getScore());
				}
			}
		}

		printWriter.flush();
		printWriter.close();
		file.delete();

		File rename = new File(FILE_PATH);
		tempFile.renameTo(rename);

	}

	/**
	 * Gets the tops scores for a level.
	 *
	 * @param levelName level of the game
	 * @return array of Score
	 */
	public static String[] getTopScores(String levelName) {
		Score[] topScores = null;
		for (LevelScores lvlScr : levelsScores) {
			if (lvlScr.getLevelName().equals(levelName)) {
				topScores = lvlScr.getTopScores();
			}
		}
		String[] stringScores = null;
		if (topScores != null) {
			stringScores = new String[topScores.length];
			for (int i = 0; i < topScores.length; i++) {
				if (topScores[i] != null)
					stringScores[i] = topScores[i].getProfileName() + " " + topScores[i].getScore();
			}
		}
		return stringScores;
	}

	/**
	 * Gets the tops scores for a level.
	 *
	 * @param levelName level of the game
	 * @return array of Score
	 */
	public static Score[] getTopScoresObject(String levelName) {
		Score[] topScores = null;
		for (LevelScores lvlScr : levelsScores) {
			if (lvlScr.getLevelName().equals(levelName)) {
				topScores = lvlScr.getTopScores();
			}
		}
		return topScores;
	}

	/**
	 * Method will try to safe a score, if it is in top 10.
	 *
	 * @param profileName name of a profile
	 * @param score       achieved score you want to safe
	 * @param levelName   level of the game
	 */
	public static void saveScore(String profileName, String levelName, int score) {
		for (LevelScores lvlScr : levelsScores) {
			if (lvlScr.getLevelName().equals(levelName)) {
				lvlScr.saveScore(new Score(profileName, score));
			}
		}
	}

	/**
	 * Delete every profile score from a database.
	 *
	 * @param profileName name of a profile
	 */
	public static void deleteProfile(String profileName) {
		for (LevelScores lvlScr : levelsScores) {
			lvlScr.deleteProfile(profileName);
		}
	}

	/**
	 * Creates new level in database, with no scores.
	 * 
	 * @param levelName name of new level
	 */
	public static void createNewLevel(String levelName) {
		levelsScores.add(new LevelScores(levelName));
	}

	/**
	 * Deletes level from a database, if exists.
	 * 
	 * @param levelName name of a level to delete
	 */
	public static void deleteLevel(String levelName) throws IllegalArgumentException {
		if (doesLevelExist(levelName)) {
			LevelScores levelToRemove = null;
			for (LevelScores lvlScr : levelsScores) {
				if (lvlScr.getLevelName().equals(levelName)) {
					levelToRemove = lvlScr;
				}
			}
			levelsScores.remove(levelToRemove);
		} else {
			throw new IllegalArgumentException("Level does not exit");
		}
	}

	/**
	 * Checks if level exist in database.
	 * 
	 * @param levelName name of the level to check
	 * @return true if level exist, false otherwise
	 */
	public static boolean doesLevelExist(String levelName) {
		boolean exists = false;
		for (LevelScores lvlScr : levelsScores) {
			if (lvlScr.getLevelName().equals(levelName)) {
				exists = true;
			}
		}
		return exists;
	}

}
