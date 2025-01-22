package Domain;

import UI.GamePanel;
import java.util.Random;

public class HelpHeroBehavior implements WizardBehavior {
    private boolean done = false;

    @Override
    public void performAction(WizardMonster wizard, Hero hero, GamePanel gamePanel) {
        // If we've already done it, do nothing
        if (done) return;

        Random random = new Random();
        int newX, newY;

        while (true) {
            int col = random.nextInt(gamePanel.getGrid()[0].length);
            int row = random.nextInt(gamePanel.getGrid().length);

            // If hero can move there (floor, no monster, no object):
            if (gamePanel.canHeroMove(new java.awt.Point(col*64, row*64))) {
                newX = col * 64;
                newY = row * 64;
                break;
            }
        }

        // Move hero
        hero.setPosition(newX, newY);
        System.out.println("Wizard teleported Hero to (" + newX + ", " + newY + ").");

        // Remove wizard from game
        gamePanel.removeMonster(wizard);
        done = true;
    }
}
