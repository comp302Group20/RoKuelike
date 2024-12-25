package Domain;

/**
 * ArcherMonster now stores its own image path internally.
 */
public class ArcherMonster extends Monster {

    // Image path is now defined here instead of AssetPaths or elsewhere
    private static final String IMAGE_PATH = "/resources/Assets/archer.png";

    public ArcherMonster(int startX, int startY) {
        super(startX, startY, IMAGE_PATH);
    }

    @Override
    public void update() {
        // Archer-specific behavior could go here.
    }
}
