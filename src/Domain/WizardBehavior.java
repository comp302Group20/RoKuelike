package Domain;

public interface WizardBehavior {
    void performAction(WizardMonster wizard, Hero hero, GridCell[][] grid, GameTimer timer);
}
