import java.util.ArrayList;

/**
 * A class that swaps male rat to female if it steps on this Power.
 *
 * @author Daumantas Balakauskas
 * @version 1.0
 */

public class FemaleSwapper extends Power {

    private static final String FEMALE_SWAP_SOUND_PATH = "maleSwapperSound.mp3";
    private static final float FEMALE_SWAP_SOUND_VOLUME = 0.1f;

    /**
     * FemaleSwapper constructor
     *
     * @param xPos x coordinate
     * @param yPos y coordinate
     */
    FemaleSwapper(Controller controller, int xPos, int yPos) {
        super(controller, true, xPos, yPos);
    }


    /**
     * Activates the power
     *
     * @param rats        used to interact with all rats that stepped on the power.
     * @param currentTile used to access Tile of the Power.
     */
    @Override
    void activate(ArrayList<Rat> rats, Tile currentTile) {

        SeaShantySimulator seaSim = new SeaShantySimulator();
        seaSim.playAudioClip(FEMALE_SWAP_SOUND_PATH, FEMALE_SWAP_SOUND_VOLUME);

        // Loop through the rats and attempt to change its gender
        for (Rat r : rats) {
            if (r instanceof AdultMale) {
                AdultFemale copyRat = new AdultFemale(this.getController(), r.getSpeed(),  r.getDirection(), r.getGasTimer(), this.xPos, this.yPos, ((AdultMale) r).isFertile, 0, 0);

                currentTile.removeActivePower(this);
                currentTile.addOccupantRat(copyRat);
                currentTile.removeOccupantRat(r);

                this.getController().ratAdded(copyRat);
                this.getController().ratRemoved(r);
            } else {
                currentTile.removeActivePower(this);
            }

            if (r instanceof ChildRat) {
                ((ChildRat) r).setRatSex(Rat.Sex.FEMALE);
            }
        }
    }

    /**
     * We don't do anything on tick
     *
     * @param currentTile used for calling removeActivePower(this).
     * @param rats        used for updating the rat arraylist every game tick.
     *                    onTick not used for this Power.
     */
    @Override
    void onTick(ArrayList<Rat> rats, Tile currentTile) {}
}
