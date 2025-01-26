package Domain;

import UI.BuildModePanel;
import Utils.AssetPaths;
import UI.GamePanel;
import Utils.SoundPlayer;

import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.Random;

/**
 * A monster that moves around and damages the hero if adjacent, preferring lures if active.
 */
public class FighterMonster extends Monster implements Serializable {
    private static final long serialVersionUID = 1L;
    private Random random;
    private transient GamePanel gamePanel;
    private static final int GRID_ROWS = 13;
    private static final int GRID_COLS = 13;

    /**
     * Constructs a FighterMonster at the specified position, referencing the hero and game panel.
     * @param sx the initial x-coordinate in pixels
     * @param sy the initial y-coordinate in pixels
     * @param h the hero instance
     * @param mg the 2D map grid
     * @param gp the GamePanel for interactions
     */
    public FighterMonster(int sx, int sy, Hero h, BuildModePanel.CellType[][] mg, GamePanel gp) {
        super(sx, sy, AssetPaths.FIGHTER, h, mg);
        random = new Random();
        gamePanel = gp;
    }

    /**
     * Updates the FighterMonster's position and actions (checks adjacency, lure presence, random movement).
     */
    @Override
    public void update() {
        updateFacingDirection();

        if (adjacentToHero()) {
            hero.setHealth(hero.getHealth() - 1);
            SoundPlayer.playSound("/resources/sounds/hurt.wav");
            System.out.println("Hero hit by FighterMonster! Health: " + hero.getHealth());
            if (hero.getHealth() <= 0) {
                System.out.println("Game Over");
            }
            return;
        }

        if (gamePanel.isLureActive()) {
            Point lurePos = gamePanel.getLurePosition();
            if (lurePos != null) {
                System.out.println("Fighter Monster detecting lure at: " + lurePos.x / CELL_SIZE + "," + lurePos.y / CELL_SIZE);

                Point currentGrid = new Point(y / CELL_SIZE, x / CELL_SIZE);
                Point lureGrid = new Point(lurePos.y / CELL_SIZE, lurePos.x / CELL_SIZE);

                System.out.println("Fighter at: " + currentGrid.x + "," + currentGrid.y);
                System.out.println("Moving towards lure at: " + lureGrid.x + "," + lureGrid.y);

                boolean[][] walkable = new boolean[GRID_ROWS][GRID_COLS];
                for (int r = 0; r < GRID_ROWS; r++) {
                    for (int c = 0; c < GRID_COLS; c++) {
                        walkable[r][c] = gamePanel.canMonsterMove(this, c * CELL_SIZE, r * CELL_SIZE);
                    }
                }

                try {
                    List<Point> path = PathFinder.findPath(currentGrid, lureGrid, walkable);

                    if (path != null && path.size() > 1) {
                        Point nextPoint = path.get(1);
                        int nextX = nextPoint.y * CELL_SIZE;
                        int nextY = nextPoint.x * CELL_SIZE;

                        System.out.println("Moving to next point: " + nextX / CELL_SIZE + "," + nextY / CELL_SIZE);

                        if (gamePanel.canMonsterMove(this, nextX, nextY)) {
                            setPosition(nextX, nextY);
                            return;
                        }
                    } else {
                        System.out.println("No path found to lure");
                    }
                } catch (Exception e) {
                    System.out.println("Error in pathfinding: " + e.getMessage());
                }
            }
        }

        int direction = random.nextInt(4);
        int nx = x, ny = y;
        if (direction == 0) ny -= CELL_SIZE;
        if (direction == 1) ny += CELL_SIZE;
        if (direction == 2) nx -= CELL_SIZE;
        if (direction == 3) nx += CELL_SIZE;

        if (gamePanel.canMonsterMove(this, nx, ny)) {
            setPosition(nx, ny);
        }
    }

    /**
     * Checks if the hero is adjacent to the monster (horizontally or vertically).
     * @return true if adjacent, false otherwise
     */
    private boolean adjacentToHero() {
        int mr = y / CELL_SIZE;
        int mc = x / CELL_SIZE;
        int hr = hero.getY() / CELL_SIZE;
        int hc = hero.getX() / CELL_SIZE;
        return (mr == hr && Math.abs(mc - hc) == 1) || (mc == hc && Math.abs(mr - hr) == 1);
    }
}
