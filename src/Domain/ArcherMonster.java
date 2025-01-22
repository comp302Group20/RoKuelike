package Domain;

import UI.BuildModePanel;
import Utils.AssetPaths;
import UI.GamePanel;

import java.io.Serializable;
import java.util.Timer;
import java.util.TimerTask;

/**
 * ArcherMonster shoots arrows at the hero from a distance.
 */
public class ArcherMonster extends Monster implements Serializable {
    private static final long serialVersionUID = 1L;
    private long lastShot;
    private transient GamePanel gamePanel;

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
        if (gamePanel != null && gamePanel.isCloakActive()) {
            System.out.println("Hero is cloaked - Archer cannot detect them!");
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

            if (distance <= 3 && distance != 2 * Math.sqrt(2)) {
                if (hero.getHealth() <= 0) {
                    System.out.println("Game Over");
                    return;
                }
                hero.setHealth(hero.getHealth() - 1);
                System.out.println("Hero hit by ArcherMonster! Health: " + hero.getHealth());
            }
        }
    }
}
