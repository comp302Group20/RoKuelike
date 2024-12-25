package Domain;

import UI.BuildModePanel;
import Utils.AssetPaths;

public class WizardMonster extends Monster {
    public WizardMonster(int sx, int sy, Hero h, BuildModePanel.CellType[][] mg) {
        super(sx, sy, AssetPaths.WIZARD, h, mg);
    }

    @Override
    public void update() {
    }
}
