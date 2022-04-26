import java.io.Serializable;

public class TimeAndRatCounters implements Serializable {
	private String timeLeft;
	private String maleRatCounter;
	private String femaleRatCounter;
	private String ratCounter;
	private boolean gameEnd = false;
	private boolean gameWon = false;

	public TimeAndRatCounters(String timeLeft, String maleRatCounter, String femaleRatCounter, String ratCounter) {
		this.timeLeft = timeLeft;
		this.maleRatCounter = maleRatCounter;
		this.femaleRatCounter = femaleRatCounter;
		this.ratCounter = ratCounter;
	}

	public TimeAndRatCounters(String timeLeft, String maleRatCounter, String femaleRatCounter, String ratCounter,
			boolean gameEnd, boolean gameWon) {
		this.timeLeft = timeLeft;
		this.maleRatCounter = maleRatCounter;
		this.femaleRatCounter = femaleRatCounter;
		this.ratCounter = ratCounter;
		this.gameEnd = gameEnd;
		this.gameWon = gameWon;
	}

	public boolean isGameEnd() {
		return gameEnd;
	}

	public void setGameEnd(boolean gameEnd) {
		this.gameEnd = gameEnd;
	}

	public boolean isGameWon() {
		return gameWon;
	}

	public void setGameWon(boolean gameWon) {
		this.gameWon = gameWon;
	}

	public String getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(String timeLeft) {
		this.timeLeft = timeLeft;
	}

	public String getMaleRatCounter() {
		return maleRatCounter;
	}

	public void setMaleRatCounter(String maleRatCounter) {
		this.maleRatCounter = maleRatCounter;
	}

	public String getFemaleRatCounter() {
		return femaleRatCounter;
	}

	public void setFemaleRatCounter(String femaleRatCounter) {
		this.femaleRatCounter = femaleRatCounter;
	}

	public String getRatCounter() {
		return ratCounter;
	}

	public void setRatCounter(String ratCounter) {
		this.ratCounter = ratCounter;
	}

}
