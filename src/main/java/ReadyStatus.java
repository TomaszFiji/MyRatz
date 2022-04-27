import java.io.Serializable;

public class ReadyStatus implements Serializable {
	private final int readyPlayers;
	private final int allPlayers;
	private final String levelName;
	private boolean isGameFinished = false;

	public ReadyStatus(int readyPlayers, int allPlayers, String levelName) {
		super();
		this.readyPlayers = readyPlayers;
		this.allPlayers = allPlayers;
		this.levelName = levelName;
	}
	
	public ReadyStatus(int readyPlayers, int allPlayers, String levelName, boolean isGameFinished) {
		super();
		this.readyPlayers = readyPlayers;
		this.allPlayers = allPlayers;
		this.levelName = levelName;
		this.isGameFinished = isGameFinished;
	}

	public boolean isGameFinished() {
		return isGameFinished;
	}

	public void setGameFinished(boolean isGameFinished) {
		this.isGameFinished = isGameFinished;
	}

	public int getReadyPlayers() {
		return readyPlayers;
	}

	public int getAllPlayers() {
		return allPlayers;
	}
	
	public String getLevelName() {
		return levelName;
	}
}
