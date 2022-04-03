import java.io.Serializable;
import java.util.ArrayList;

public class LevelNamesObject implements Serializable {
	private ArrayList<String> names;
	private boolean ifDefaultNames;

	public LevelNamesObject(ArrayList<String> names, boolean isDefaultNames) {
		this.names = names;
		this.ifDefaultNames = isDefaultNames;
	}

	public boolean getIsDefaultNames() {
		return ifDefaultNames;
	}
	
	public ArrayList<String> getLevelNames() {
		return names;
	}
}
