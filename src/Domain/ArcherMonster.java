package Domain;

import UI.BuildModePanel;
import Utils.AssetPaths;
import UI.GamePanel;

public class ArcherMonster extends Monster {
    private long lastShot;
    private GamePanel gamePanel;

    public ArcherMonster(int sx, int sy, Hero h, BuildModePanel.CellType[][] mg, GamePanel gp) {
        super(sx, sy, AssetPaths.ARCHER, h, mg);
        lastShot = System.currentTimeMillis();
        gamePanel = gp;
    }

    @Override
    public void update() {
        long now = System.currentTimeMillis();
        if (now - lastShot >= 1000) {
            lastShot = now;
            int mr = y / 64;
            int mc = x / 64;
            int hr = hero.getY() / 64;
            int hc = hero.getX() / 64;
            double d = Math.sqrt((mr - hr)*(mr - hr) + (mc - hc)*(mc - hc));
            if (d <= 3 && d!=2*Math.sqrt(2)) { // THIS IS GOING TO BE MODIFIED LATER.
                hero.setHealth(hero.getHealth() - 1);
                if (hero.getHealth() <= 0) {
                    System.out.println("Game Over");
                }
            }
        }
    }
}