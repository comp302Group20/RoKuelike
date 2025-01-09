package Domain;

public class IndecisiveBehavior implements WizardBehavior {
    @Override
    public void performAction(WizardMonster wizard, Hero hero, GridCell[][] grid, GameTimer timer) {
        try {
            Thread.sleep(2000); // Wait for 2 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // TODO
        // wizard.disappear();
    }
}
