package Domain;

import UI.BuildModePanel;
import Utils.AssetPaths;
import UI.GamePanel;
import java.util.Random;

/**
 * FighterMonster moves towards the hero and attacks when adjacent.
 */
public class FighterMonster extends Monster {
    private Random random;
    private GamePanel gamePanel;

    /**
     * Constructor for FighterMonster.
     *
     * @param sx Starting x-coordinate in pixels.
     * @param sy Starting y-coordinate in pixels.
     * @param h  Reference to the Hero.
     * @param mg Game grid.
     * @param gp Reference to the GamePanel.
     */
    public FighterMonster(int sx, int sy, Hero h, BuildModePanel.CellType[][] mg, GamePanel gp) {
        super(sx, sy, AssetPaths.FIGHTER, h, mg);
        random = new Random();
        gamePanel = gp;
    }

    /**
     * Updates the FighterMonster's behavior.
     * It faces the hero, moves randomly, and attacks if adjacent.
     */
    @Override
    public void update() {
        updateFacingDirection();

        if (adjacentToHero()) {
            hero.setHealth(hero.getHealth() - 1);
            System.out.println("Hero hit by FighterMonster! Health: " + hero.getHealth());
            if (hero.getHealth() <= 0) {
                System.out.println("Game Over");
            }
            return;
        }

        // Random movement
        int direction = random.nextInt(4);
        int nx = x;
        int ny = y;
        if (direction == 0) ny -= CELL_SIZE; // Up
        if (direction == 1) ny += CELL_SIZE; // Down
        if (direction == 2) nx -= CELL_SIZE; // Left
        if (direction == 3) nx += CELL_SIZE; // Right

        if (gamePanel.canMonsterMove(this, nx, ny)) {
            setPosition(nx, ny);
        }
    }

    /**
     * Checks if the FighterMonster is adjacent to the hero.
     *
     * @return True if adjacent; false otherwise.
     */
    private boolean adjacentToHero() {
        int mr = y / CELL_SIZE;
        int mc = x / CELL_SIZE;
        int hr = hero.getY() / CELL_SIZE;
        int hc = hero.getX() / CELL_SIZE;
        return (mr == hr && Math.abs(mc - hc) == 1) || (mc == hc && Math.abs(mr - hr) == 1);
    }
}
