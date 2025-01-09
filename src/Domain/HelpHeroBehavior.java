package Domain;

import java.util.Random;

public class HelpHeroBehavior implements WizardBehavior {
    @Override
    public void performAction(WizardMonster wizard, Hero hero, GridCell[][] grid, GameTimer timer) {
        Random random = new Random();
        int x, y;
//        do {
//            x = random.nextInt(grid.length);
//            y = random.nextInt(grid[0].length);
//        } // TODO
    // while (!grid[x][y].isEmpty()); // Ensure the location is empty
        // hero.setPosition(x, y);
        // wizard.disappear();
    }
}
