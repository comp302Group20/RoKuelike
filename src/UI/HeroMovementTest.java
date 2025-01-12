package UI;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Random;

/**
 * JUnit tests for Hero's movement within the game grid.
 */
@RunWith(JUnit4.class)
public class HeroMovementTest {

    // ---------------------------------------------------
    // Stub classes to make the tests compile/run
    // ---------------------------------------------------

    // Minimal stub for CellType (to represent floor and wall tiles)
    public static class BuildModePanel {
        public enum CellType { FLOOR, WALL }
    }

    // Minimal Hero class
    public static class Hero {
        private int x, y;    // Hero's position in pixels
        private int cellSize = 64;

        public Hero(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() { return x; }
        public int getY() { return y; }

        public void move(int dx, int dy, BuildModePanel.CellType[][] grid) {
            int newX = x + dx;
            int newY = y + dy;

            int col = newX / cellSize;
            int row = newY / cellSize;

            // Prevent movement if the new cell is a WALL or out of bounds
            if (row >= 0 && row < grid.length && col >= 0 && col < grid[0].length) {
                if (grid[row][col] == BuildModePanel.CellType.FLOOR) {
                    x = newX;
                    y = newY;
                }
            }
        }
    }

    // ---------------------------------------------------
    // Fields for Testing
    // ---------------------------------------------------
    private Hero hero;
    private BuildModePanel.CellType[][] grid;

    private static final int GRID_ROWS = 5;
    private static final int GRID_COLS = 5;
    private int cellSize = 64;

    @Before
    public void setUp() {
        // Initialize hero at (0,0)
        hero = new Hero(0, 0);

        // Create a 5x5 grid filled with FLOOR cells
        grid = new BuildModePanel.CellType[GRID_ROWS][GRID_COLS];
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                grid[r][c] = BuildModePanel.CellType.FLOOR;
            }
        }

        // Add walls at specific positions
        grid[0][1] = BuildModePanel.CellType.WALL;  // Right of the hero
        grid[1][0] = BuildModePanel.CellType.WALL;  // Below the hero
    }

    // ---------------------------------------------------
    // JUnit Tests for Hero Movement
    // ---------------------------------------------------

    @Test
    public void testHeroMovesToEmptyCell() {
        // Move the hero to the right by 2 cells (skipping the wall)
        hero.move(cellSize * 2, 0, grid);

        assertEquals("Hero should have moved to (128,0)", 128, hero.getX());
        assertEquals("Hero Y position should remain the same", 0, hero.getY());
    }

    @Test
    public void testHeroBlockedByWall() {
        // Attempt to move the hero right into a wall
        hero.move(cellSize, 0, grid);

        assertEquals("Hero should not move into a wall (X position)", 0, hero.getX());
        assertEquals("Hero should not move into a wall (Y position)", 0, hero.getY());
    }

    @Test
    public void testHeroBlockedByWallDownward() {
        // Attempt to move the hero downward into a wall
        hero.move(0, cellSize, grid);

        assertEquals("Hero should not move into a wall below", 0, hero.getX());
        assertEquals("Hero should not move into a wall below", 0, hero.getY());
    }

    @Test
    public void testHeroCannotMoveOutOfBoundsLeft() {
        // Attempt to move left out of the grid
        hero.move(-cellSize, 0, grid);

        assertEquals("Hero should not move out of the grid to the left", 0, hero.getX());
        assertEquals("Hero should not move out of the grid to the left", 0, hero.getY());
    }

    @Test
    public void testHeroCannotMoveOutOfBoundsUp() {
        // Attempt to move up out of the grid
        hero.move(0, -cellSize, grid);

        assertEquals("Hero should not move out of the grid upwards", 0, hero.getX());
        assertEquals("Hero should not move out of the grid upwards", 0, hero.getY());
    }

    @Test
    public void testHeroMovesMultipleSteps() {
        // Move the hero diagonally to an empty cell
        hero.move(cellSize * 2, cellSize * 2, grid);

        assertEquals("Hero should move to (128, 128)", 128, hero.getX());
        assertEquals("Hero should move to (128, 128)", 128, hero.getY());
    }
}