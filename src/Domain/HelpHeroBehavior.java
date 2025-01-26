package Domain;

import UI.GamePanel;
import java.util.Random;

/**
 * A WizardMonster behavior that helps the hero by teleporting them to a safe location, then disappears.
 */
public class HelpHeroBehavior implements WizardBehavior {
    private boolean done = false;

    /**
     * Teleports the hero to a random valid floor cell and removes the wizard from the game.
     * @param wizard the WizardMonster performing this action
     * @param hero the Hero to be teleported
     * @param gamePanel the GamePanel for monster removal and position checking
     */
    @Override
    public void performAction(WizardMonster wizard, Hero hero, GamePanel gamePanel) {
        if (done) return;

        Random random = new Random();
        int newX, newY;

        while (true) {
            int col = random.nextInt(gamePanel.getGrid()[0].length);
            int row = random.nextInt(gamePanel.getGrid().length);

            if (gamePanel.canHeroMove(new java.awt.Point(col * 64, row * 64))) {
                newX = col * 64;
                newY = row * 64;
                break;
            }
        }

        hero.setPosition(newX, newY);
        System.out.println("Wizard teleported Hero to (" + newX + ", " + newY + ").");

        gamePanel.removeMonster(wizard);
        done = true;
    }
}
