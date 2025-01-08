package Domain;

import UI.BuildModePanel;
import UI.GamePanel;
import Utils.AssetPaths;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * WizardMonster can teleport the rune randomly on the map.
 */
public class WizardMonster extends Monster {
    private GamePanel gamePanel;
    private Timer teleportTimer;
    private Random rng;

    /**
     * Constructor for WizardMonster.
     *
     * @param sx Starting x-coordinate in pixels.
     * @param sy Starting y-coordinate in pixels.
     * @param h  Reference to the Hero.
     * @param mg Game grid.
     * @param gp Reference to the GamePanel.
     */
    public WizardMonster(int sx, int sy, Hero h, BuildModePanel.CellType[][] mg, GamePanel gp) {
        super(sx, sy, AssetPaths.WIZARD, h, mg);
        gamePanel = gp;
        rng = new Random();
        teleportTimer = new Timer(true);
        startRuneTeleportTimer();
    }

    /**
     * Updates the WizardMonster's behavior.
     * It faces the hero and performs teleportation.
     */
    @Override
    public void update() {
        updateFacingDirection();
        // WizardMonster-specific behaviors can be added here
    }

    /**
     * Starts a timer to teleport the rune randomly at fixed intervals.
     */
    private void startRuneTeleportTimer() {
        teleportTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gamePanel.teleportRuneRandomly();
            }
        }, 0, 5000); // Teleport every 5 seconds
    }
}
