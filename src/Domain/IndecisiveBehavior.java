package Domain;

import UI.GamePanel;

/**
 * A WizardMonster behavior that does nothing for 2 seconds before disappearing.
 */
public class IndecisiveBehavior implements WizardBehavior {
    private boolean done = false;

    /**
     * Makes the wizard wait for 2 seconds, then removes it from the game.
     * @param wizard the WizardMonster performing this action
     * @param hero the Hero (unused in this behavior)
     * @param gamePanel the GamePanel for removing the wizard
     */
    @Override
    public void performAction(WizardMonster wizard, Hero hero, GamePanel gamePanel) {
        if (done) return;
        done = true;

        new Thread(() -> {
            try {
                Thread.sleep(2000);
                gamePanel.removeMonster(wizard);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
