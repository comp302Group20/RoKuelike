package Domain;

import UI.BuildModePanel;
import Utils.AssetPaths;
import UI.GamePanel;

/**
 * ArcherMonster shoots arrows at the hero from a distance.
 */
public class ArcherMonster extends Monster {
    private long lastShot;
    private GamePanel gamePanel;

    /**
     * Constructor for ArcherMonster.
     *
     * @param sx Starting x-coordinate in pixels.
     * @param sy Starting y-coordinate in pixels.
     * @param h  Reference to the Hero.
     * @param mg Game grid.
     * @param gp Reference to the GamePanel.
     */
    public ArcherMonster(int sx, int sy, Hero h, BuildModePanel.CellType[][] mg, GamePanel gp) {
        super(sx, sy, AssetPaths.ARCHER, h, mg);
        lastShot = System.currentTimeMillis();
        gamePanel = gp;
    }

    /**
     * Updates the ArcherMonster's behavior.
     * It faces the hero and shoots if within range.
     */
    @Override
    public void update() {
        // Always update facing direction so the archer is visually oriented.
        updateFacingDirection();

        // If the cloak is active, the archer can't see or hurt the hero.
        if (gamePanel.isCloakActive()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastShot >= 1000) { // Shoot every second
            lastShot = now;
            int mr = y / CELL_SIZE;
            int mc = x / CELL_SIZE;
            int hr = hero.getY() / CELL_SIZE;
            int hc = hero.getX() / CELL_SIZE;
            double distance = Math.sqrt((mr - hr) * (mr - hr) + (mc - hc) * (mc - hc));

            // Adjust shooting conditions as needed:
            // This check means if hero is within ~3 tiles (Manhattan or diagonal combos),
            // and not exactly 2√2 away (which might correspond to a diagonal at distance 2),
            // the archer deals damage.
            if (distance <= 3 && distance != 2 * Math.sqrt(2)) {
                hero.setHealth(hero.getHealth() - 1);
                System.out.println("Hero hit by ArcherMonster! Health: " + hero.getHealth());
                if (hero.getHealth() <= 0) {
                    System.out.println("Game Over");
                }
            }
        }
    }
}
