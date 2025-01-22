package Domain;

import UI.GamePanel;

public class IndecisiveBehavior implements WizardBehavior {
    private boolean done = false;

    @Override
    public void performAction(WizardMonster wizard, Hero hero, GamePanel gamePanel) {
        if (done) return;
        done = true;

        // Wait 2 seconds, then disappear
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                // After 2 seconds, remove wizard
                gamePanel.removeMonster(wizard);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
