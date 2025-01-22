package Domain;

import Domain.Hero;
import Domain.WizardMonster;
import UI.GamePanel;

public interface WizardBehavior {
    void performAction(WizardMonster wizard, Hero hero, GamePanel gamePanel);
}
