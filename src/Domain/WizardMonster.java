package Domain;

import UI.BuildModePanel;
import UI.GamePanel;
import Utils.AssetPaths;
import java.io.IOException;
import java.io.Serializable;

public class WizardMonster extends Monster implements Serializable {
    private static final long serialVersionUID = 1L;

    // We'll create and reuse these behaviors:
    private transient WizardBehavior challengeBehavior;
    private transient WizardBehavior helpBehavior;
    private transient WizardBehavior indecisiveBehavior;

    // Tracks whichever behavior is currently active
    private transient WizardBehavior currentBehavior;

    private transient GamePanel gamePanel;

    public WizardMonster(int sx, int sy, Hero h, BuildModePanel.CellType[][] mg, GamePanel gp) {
        super(sx, sy, AssetPaths.WIZARD, h, mg);
        this.gamePanel = gp;

        // Create behavior objects once
        challengeBehavior = new ChallengeHeroBehavior();
        helpBehavior = new HelpHeroBehavior();
        indecisiveBehavior = new IndecisiveBehavior();

        // Start out, say, indecisive (or null)
        currentBehavior = indecisiveBehavior;
    }

    @Override
    public void update() {
        updateFacingDirection();

        // Check the ratio each update
        double ratio = gamePanel.getTimeRatio();

        // Switch behaviors only if necessary
        if (ratio > 0.7 && currentBehavior != challengeBehavior) {
            // Switch to ChallengeHeroBehavior (same instance)
            currentBehavior = challengeBehavior;
        }
        else if (ratio < 0.3 && currentBehavior != helpBehavior) {
            // Switch to HelpHeroBehavior
            currentBehavior = helpBehavior;
        }
        else if (ratio >= 0.3 && ratio <= 0.7 && currentBehavior != indecisiveBehavior) {
            // Switch to IndecisiveBehavior
            currentBehavior = indecisiveBehavior;
        }

        // Perform the chosen behavior
        if (currentBehavior != null) {
            currentBehavior.performAction(this, hero, gamePanel);
        }
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Re-init transient fields
        // The gamePanel reference is usually injected after load,
        // so once you set gamePanel again, re-create the behaviors:
        challengeBehavior = new ChallengeHeroBehavior();
        helpBehavior = new HelpHeroBehavior();
        indecisiveBehavior = new IndecisiveBehavior();

        // currentBehavior can be re-chosen next update() based on ratio,
        // or you can store which behavior was active and reassign it.
    }

    public String getCurrentBehaviorName() {
        if (currentBehavior instanceof ChallengeHeroBehavior) {
            return "Challenge";
        } else if (currentBehavior instanceof HelpHeroBehavior) {
            return "Help";
        } else if (currentBehavior instanceof IndecisiveBehavior) {
            return "Indecisive";
        }
        return "Unknown";
    }
}
