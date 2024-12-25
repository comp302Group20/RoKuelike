package Domain;

import UI.BuildModePanel;
import UI.GamePanel;
import Utils.AssetPaths;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class WizardMonster extends Monster {
    private GamePanel gamePanel;
    private Timer teleportTimer;
    private Random rng;
    private long lastTeleport;

    public WizardMonster(int sx, int sy, Hero h, BuildModePanel.CellType[][] mg, GamePanel gp) {
        super(sx, sy, AssetPaths.WIZARD, h, mg);
        gamePanel = gp;
        rng = new Random();
        teleportTimer = new Timer(true);
        startRuneTeleportTimer();
    }

    @Override
    public void update() {
    }

    private void startRuneTeleportTimer() {
        teleportTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                gamePanel.teleportRuneRandomly();
            }
        }, 0, 5000);
    }
}
