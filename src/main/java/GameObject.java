import java.io.Serializable;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

/**
 * Class that describes a game object.
 *
 * @author Vilija Pundyte
 * @version 1.0
 */
public abstract class GameObject implements Serializable {
	private static final int WIDTH = 64;

	// private final Image img;
	private final String img;
	private final boolean isPassable;
	private Controller controller;

	/**
	 * Object constructor.
	 *
	 * @param isPassable can rats walk through.
	 */
	public GameObject(boolean isPassable, Controller controller) {
		this.controller = controller;
		this.isPassable = isPassable;
		img = createTexturePath();
	}
	
	public Controller getController() {
		return this.controller;
	}
	
	public void setController(Controller controller) {
		this.controller = controller;
	}

	/**
	 * Draws object on screen.
	 *
	 * @param x Horizontal position.
	 * @param y Vertical position.
	 * @param g Graphics context being drawn on.
	 */
	public void draw(int x, int y, GraphicsContext g) {
		x = WIDTH * x;
		y = WIDTH * y;
		g.drawImage(this.getImg(), x, y);
	}

	/**
	 * Gets width of a single game object texture in pixels.
	 *
	 * @return width of game object.
	 */
	protected static int getWIDTH() {
		return WIDTH;
	}

	/**
	 * Gets game object image.
	 *
	 * @return game object image.
	 */
	protected Image getImg() {
		return new Image(img);
	}

	/**
	 * Creates file path to the texture of a specific object.
	 *
	 * @return File path as String.
	 */
	public String createTexturePath() {
		String className = this.getClass().getSimpleName().toLowerCase();
		return "file:target/classes/" + className + ".png";
	}

	/**
	 * Returns whether rats can pass through game object.
	 *
	 * @return can be passed.
	 */
	public boolean isPassable() {
		return isPassable;
	}
}
