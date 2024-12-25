package Domain;

/**
 * WizardMonster now stores its own image path internally.
 */
public class WizardMonster extends Monster {

    private static final String IMAGE_PATH = "/resources/Assets/wizard.png";

    public WizardMonster(int startX, int startY) {
        super(startX, startY, IMAGE_PATH);
    }

    @Override
    public void update() {
        // Wizard-specific behavior could go here.
    }
}
