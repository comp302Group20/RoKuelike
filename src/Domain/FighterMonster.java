package Domain;

/**
 * FighterMonster now stores its own image path internally.
 */
public class FighterMonster extends Monster {

    private static final String IMAGE_PATH = "/resources/Assets/fighter.png";

    public FighterMonster(int startX, int startY) {
        super(startX, startY, IMAGE_PATH);
    }

    @Override
    public void update() {
        // Fighter-specific behavior could go here.
    }
}
