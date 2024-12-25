package Domain;

import UI.BuildModePanel;
import Utils.AssetPaths;
import java.util.Random;

public class FighterMonster extends Monster {
    private final Random random = new Random();

    public FighterMonster(int sx, int sy, Hero h, BuildModePanel.CellType[][] mg) {
        super(sx, sy, AssetPaths.FIGHTER, h, mg);
    }

    @Override
    public void update() {
        if (isHeroAdjacent()) {
            hero.setHealth(hero.getHealth() - 1);
            if (hero.getHealth() <= 0) {
                System.out.println("Game Over");
            }
            return;
        }
        int d = random.nextInt(4);
        int nx = x;
        int ny = y;
        if (d == 0) ny -= 64;
        if (d == 1) ny += 64;
        if (d == 2) nx -= 64;
        if (d == 3) nx += 64;
        if (canMove(nx, ny)) {
            setPosition(nx, ny);
        }
    }

    private boolean isHeroAdjacent() {
        int mr = y / 64;
        int mc = x / 64;
        int hr = hero.getY() / 64;
        int hc = hero.getX() / 64;
        boolean r = (mr == hr && Math.abs(mc - hc) == 1);
        boolean c = (mc == hc && Math.abs(mr - hr) == 1);
        return r || c;
    }

    private boolean canMove(int nx, int ny) {
        int r = ny / 64;
        int c = nx / 64;
        if (r < 0 || r >= mapGrid.length || c < 0 || c >= mapGrid[0].length) return false;
        if (mapGrid[r][c] == BuildModePanel.CellType.WALL) return false;
        return true;
    }
}
