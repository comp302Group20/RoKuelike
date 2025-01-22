package Domain;

import UI.BuildModePanel;
import Utils.AssetPaths;
import UI.GamePanel;

import java.awt.Point;
import java.util.List;  // Add this import
import java.io.Serializable;
import java.util.Random;

public class FighterMonster extends Monster implements Serializable {
    private static final long serialVersionUID = 1L;
    private Random random;
    private transient GamePanel gamePanel;
    private static final int GRID_ROWS = 13;  // Add these constants
    private static final int GRID_COLS = 13;

    public FighterMonster(int sx, int sy, Hero h, BuildModePanel.CellType[][] mg, GamePanel gp) {
        super(sx, sy, AssetPaths.FIGHTER, h, mg);
        random = new Random();
        gamePanel = gp;
    }

    @Override
    public void update() {
        updateFacingDirection();

        if (adjacentToHero()) {
            hero.setHealth(hero.getHealth() - 1);
            System.out.println("Hero hit by FighterMonster! Health: " + hero.getHealth());
            if (hero.getHealth() <= 0) {
                System.out.println("Game Over");
            }
            return;
        }

        // Check for lure first
        if (gamePanel.isLureActive()) {
            Point lurePos = gamePanel.getLurePosition();
            if (lurePos != null) {
                System.out.println("Fighter Monster detecting lure at: " + lurePos.x/CELL_SIZE + "," + lurePos.y/CELL_SIZE);

                // Convert current position and lure position to grid coordinates
                Point currentGrid = new Point(y / CELL_SIZE, x / CELL_SIZE);
                Point lureGrid = new Point(lurePos.y / CELL_SIZE, lurePos.x / CELL_SIZE);

                System.out.println("Fighter at: " + currentGrid.x + "," + currentGrid.y);
                System.out.println("Moving towards lure at: " + lureGrid.x + "," + lureGrid.y);

                // Create walkable grid
                boolean[][] walkable = new boolean[GRID_ROWS][GRID_COLS];
                for (int r = 0; r < GRID_ROWS; r++) {
                    for (int c = 0; c < GRID_COLS; c++) {
                        walkable[r][c] = gamePanel.canMonsterMove(this, c * CELL_SIZE, r * CELL_SIZE);
                    }
                }

                // Find path to lure
                try {
                    List<Point> path = PathFinder.findPath(currentGrid, lureGrid, walkable);

                    if (path != null && path.size() > 1) {
                        // Move to next point in path
                        Point nextPoint = path.get(1); // Index 1 is the next step (0 is current position)
                        int nextX = nextPoint.y * CELL_SIZE;
                        int nextY = nextPoint.x * CELL_SIZE;

                        System.out.println("Moving to next point: " + nextX/CELL_SIZE + "," + nextY/CELL_SIZE);

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

        // If no lure or can't find path, move randomly
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

    private boolean adjacentToHero() {
        int mr = y / CELL_SIZE;
        int mc = x / CELL_SIZE;
        int hr = hero.getY() / CELL_SIZE;
        int hc = hero.getX() / CELL_SIZE;
        return (mr == hr && Math.abs(mc - hc) == 1) || (mc == hc && Math.abs(mr - hr) == 1);
    }
}