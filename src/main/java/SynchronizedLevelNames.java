import java.util.ArrayList;

public class SynchronizedLevelNames {
	private ArrayList<String> levelsNames = new ArrayList<>();
	private boolean updated = false;
	
	public synchronized void setLevelsNames(ArrayList<String> levelsNames) {
		while (updated) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		updated = true;
		this.levelsNames = levelsNames;
		notifyAll();
	}
	
	public synchronized ArrayList<String> getLevelsNames() {
		while (!updated) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		updated = false;
		notifyAll();
		return this.levelsNames;
	}
}
