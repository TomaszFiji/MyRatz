import java.io.Serializable;

public class Settings implements Serializable {
	private int maxRats;
	private int parTime;
	private int[] dropRates = new int[8];

	public Settings(int maxRats, int parTime, int[] dropRates) {
		this.maxRats = maxRats;
		this.parTime = parTime;
		this.dropRates = dropRates;
	}

	public void setMaxRats(int maxRats) {
		this.maxRats = maxRats;
	}

	public void setParTime(int parTime) {
		this.parTime = parTime;
	}

	public void setDropRates(int[] dropRates) {
		this.dropRates = dropRates;
	}

	public void setDropRate(int index, int dropRate) {
		if (index >= 0 && index <= this.dropRates.length) {
			this.dropRates[index] = dropRate;
		}
	}

	public int getMaxRats() {
		return this.maxRats;
	}

	public int getParTime() {
		return this.parTime;
	}

	public int[] getDropRates() {
		return this.dropRates;
	}

	public int getDropRate(int index) {
		if (index >= 0 && index <= this.dropRates.length) {
			return this.dropRates[index];
		} else {
			return -1;
		}
	}
}
