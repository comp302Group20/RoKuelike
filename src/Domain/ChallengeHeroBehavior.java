package Domain;

import UI.GamePanel;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A WizardMonster behavior that continuously teleports the rune while the time ratio is above 70%.
 */
public class ChallengeHeroBehavior implements WizardBehavior {
    private Timer runeTeleportTimer;
    private boolean started = false;

    /**
     * Executes the behavior logic: periodically teleport the rune to challenge the hero until time ratio < 70%.
     * @param wizard the WizardMonster performing this behavior
     * @param hero the Hero being challenged
     * @param gamePanel the GamePanel for teleporting the rune and removing the wizard
     */
    @Override
    public void performAction(WizardMonster wizard, Hero hero, GamePanel gamePanel) {
        if (runeTeleportTimer != null) {
            runeTeleportTimer.cancel();
            runeTeleportTimer = null;
        }

        double ratio = gamePanel.getTimeRatio();
        if (ratio <= 0.7) {
            started = false;
            return;
        }

        if (started) return;
        started = true;

        runeTeleportTimer = new Timer(true);
        runeTeleportTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                double currentRatio = gamePanel.getTimeRatio();
                if (currentRatio <= 0.7) {
                    runeTeleportTimer.cancel();
                    gamePanel.removeMonster(wizard);
                    started = false;
                    System.out.println("No more challenging. Time ratio is now " + currentRatio);
                } else {
                    gamePanel.teleportRuneRandomly();
                    System.out.println("Wizard teleports the rune for a challenge!");
                }
            }
        }, 0, 3000);
    }
}
