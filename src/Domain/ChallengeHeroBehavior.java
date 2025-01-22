package Domain;

import UI.GamePanel;
import java.util.Timer;
import java.util.TimerTask;

public class ChallengeHeroBehavior implements WizardBehavior {
    private Timer runeTeleportTimer;
    private boolean started = false; // so we only schedule once

    @Override
    public void performAction(WizardMonster wizard, Hero hero, GamePanel gamePanel) {
        // If we've already started, do nothing (prevents multiple timers)
        if (started) return;
        started = true;

        runeTeleportTimer = new Timer(true);
        runeTeleportTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double ratio = gamePanel.getTimeRatio();
                if (ratio <= 0.7) {
                    // No longer above 70% => stop and remove wizard
                    runeTeleportTimer.cancel();
                    gamePanel.removeMonster(wizard);
                    System.out.println("No more challenging. Time ratio is now " + ratio);
                } else {
                    // Teleport rune
                    gamePanel.teleportRuneRandomly();
                    System.out.println("Wizard teleports the rune for a challenge!");
                }
            }
        }, 0, 3000); // every 3 seconds
    }
}
