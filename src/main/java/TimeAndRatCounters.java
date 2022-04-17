import java.io.Serializable;

public class TimeAndRatCounters implements Serializable {
	String timeLeft;
	String maleRatCounter;
	String femaleRatCounter;
	String ratCounter;

	public TimeAndRatCounters(String timeLeft, String maleRatCounter, String femaleRatCounter, String ratCounter) {
		this.timeLeft = timeLeft;
		this.maleRatCounter = maleRatCounter;
		this.femaleRatCounter = femaleRatCounter;
		this.ratCounter = ratCounter;
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
