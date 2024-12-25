package Domain;

import UI.BuildModePanel;
import Utils.AssetPaths;
import java.util.Random;
import UI.GamePanel;

public class FighterMonster extends Monster {
    private Random random;
    private GamePanel gamePanel;

    public FighterMonster(int sx, int sy, Hero h, BuildModePanel.CellType[][] mg, GamePanel gp) {
        super(sx, sy, AssetPaths.FIGHTER, h, mg);
        random = new Random();
        gamePanel = gp;
    }

    @Override
    public void update() {
        if (adjacentToHero()) {
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
        if (gamePanel.canMonsterMove(this, nx, ny)) {
            setPosition(nx, ny);
        }
    }

    private boolean adjacentToHero() {
        int mr = y / 64;
        int mc = x / 64;
        int hr = hero.getY() / 64;
        int hc = hero.getX() / 64;
        return (mr == hr && Math.abs(mc - hc) == 1) || (mc == hc && Math.abs(mr - hr) == 1);
    }
}
