
public interface Controller {
	public Tile getTileAt(int x, int y);

	public void ratKilled(Rat rat);

	public void ratAdded(Rat rat);

	public void ratRemoved(Rat rat);

	public int[] getCounters();

	public int getCurrentTimeLeft();

	public void addPowersFromSave(int[] inProgInv);
}
