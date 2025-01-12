package UI;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * JUnit tests for the canMonsterMove method.
 */
@RunWith(JUnit4.class)
public class CanMonsterMoveTest {

    // ---------------------------------------------------
    // Stub classes to make the tests compile/run
    // ---------------------------------------------------

    // Minimal stub for CellType
    public static class BuildModePanel {
        public enum CellType { FLOOR, WALL }

        // Minimal PlacedObject stub:
        public static class PlacedObject {
            public boolean hasRune;
            public boolean runeVisible;

            public PlacedObject() {
                hasRune = false;
                runeVisible = false;
            }
        }
    }

    // Minimal stub for Hero
    public static class Hero {
        private int x, y;    // hero's position
        private int health;
        private boolean facingLeft = true;

        public Hero(int x, int y) {
            this.x = x;
            this.y = y;
            this.health = 3;  // default health
        }

        public int getX() { return x; }
        public int getY() { return y; }
        public void setPosition(int newX, int newY) { this.x = newX; this.y = newY; }

        public void move(int dx, int dy) {
            this.x += dx;
            this.y += dy;
        }

        public boolean isFacingLeft() { return facingLeft; }
        public void setFacingLeft(boolean left) { this.facingLeft = left; }

        public int getHealth() { return health; }
        public void setHealth(int h) { this.health = h; }
    }

    // Minimal stub for Monster
    public static abstract class Monster {
        protected int x, y;
        public Monster(int x, int y) { this.x = x; this.y = y; }
        public int getX() { return x; }
        public int getY() { return y; }
    }
    public static class ArcherMonster extends Monster {
        public ArcherMonster(int x, int y) { super(x, y); }
    }
    public static class FighterMonster extends Monster {
        public FighterMonster(int x, int y) { super(x, y); }
    }

    // Minimal stub for Enchantment
    public static class Enchantment {
        private int x, y;
        public Enchantment(int x, int y) {
            this.x = x; this.y = y;
        }
    }

    // ---------------------------------------------------
    // Fields needed to mimic partial GamePanel logic
    // ---------------------------------------------------
    private Hero hero;
    private boolean isPaused;
    private boolean gameOver;
    private boolean heroDied;

    private static final int GRID_ROWS = 13;
    private static final int GRID_COLS = 13;
    private int cellSize = 64;

    private BuildModePanel.CellType[][] grid;
    private BuildModePanel.PlacedObject[][] placedObjects;
    private List<Monster> monsters;
    private List<Enchantment> enchantments;

    private Random random;

    /**
     * Constructor: sets up a minimal grid and references.
     */
    public CanMonsterMoveTest() {
        // Initialize random
        random = new Random();

        // Default flags
        isPaused = false;
        gameOver = false;
        heroDied = false;

        // Create a hero at some position
        hero = new Hero(64, 64);

        // Initialize empty grid (all FLOOR by default)
        grid = new BuildModePanel.CellType[GRID_ROWS][GRID_COLS];
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                grid[r][c] = BuildModePanel.CellType.FLOOR;
            }
        }

        // Initialize empty placedObjects
        placedObjects = new BuildModePanel.PlacedObject[GRID_ROWS][GRID_COLS];
        // Initialize monsters and enchantments
        monsters = new ArrayList<>();
        enchantments = new ArrayList<>();
    }

    // ---------------------------------------------------
    // canMonsterMove Method
    // ---------------------------------------------------
    /**
     * requires: monster may be null if checking a spawn spot, 0 <= nx,ny <= infinite
     * modifies: none
     * effects: Returns true if (nx, ny) is a valid tile for a monster
     *          (not a wall, not out of bounds, not hero's cell,
     *           not another monster's cell unless it's the same monster,
     *           not blocked by an object).
     */
    public boolean canMonsterMove(Monster monster, int nx, int ny) {
        int c = nx / cellSize;
        int r = ny / cellSize;
        if (r < 0 || r >= GRID_ROWS || c < 0 || c >= GRID_COLS) return false;
        // if it's a wall, no
        if (grid[r][c] == BuildModePanel.CellType.WALL) return false;

        // if there's an object (we'll assume objects block monsters)
        if (placedObjects[r][c] != null) {
            return false;
        }

        // if hero is there
        if (hero.getX() == nx && hero.getY() == ny) {
            return false;
        }

        // if another monster is there
        for (Monster mm : monsters) {
            // if it's the same monster, it's okay to stand in the same spot
            if (mm != monster && mm.getX() == nx && mm.getY() == ny) {
                return false;
            }
        }
        return true;
    }

    // ---------------------------------------------------
    // JUnit Tests for canMonsterMove
    // ---------------------------------------------------

    @Before
    public void init() {
        // Re-instantiate the stub so each test starts fresh
        isPaused = false;
        gameOver = false;
        heroDied = false;
        hero = new Hero(64, 64);
        grid = new BuildModePanel.CellType[GRID_ROWS][GRID_COLS];
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                grid[r][c] = BuildModePanel.CellType.FLOOR;
            }
        }
        placedObjects = new BuildModePanel.PlacedObject[GRID_ROWS][GRID_COLS];
        monsters = new ArrayList<>();
        enchantments = new ArrayList<>();
    }

    @Test
    public void testCanMonsterMove_validFloorCell() {
        // [3][3] is floor
        Monster fighter = new FighterMonster(0, 0);
        boolean canMove = canMonsterMove(fighter, 3 * 64, 3 * 64);
        assertTrue("Should be able to move to [3,3]", canMove);
    }

    @Test
    public void testCanMonsterMove_wallCell() {
        grid[3][3] = BuildModePanel.CellType.WALL;
        Monster fighter = new FighterMonster(0, 0);
        boolean canMove = canMonsterMove(fighter, 3 * 64, 3 * 64);
        assertFalse("Cannot move onto a wall cell", canMove);
    }

    @Test
    public void testCanMonsterMove_occupiedByHero() {
        hero.setPosition(3 * 64, 3 * 64);
        Monster fighter = new FighterMonster(0, 0);
        boolean canMove = canMonsterMove(fighter, 3 * 64, 3 * 64);
        assertFalse("Cannot move onto hero's cell", canMove);
    }

    @Test
    public void testCanMonsterMove_alreadyOccupiedByAnotherMonster() {
        Monster monsterA = new FighterMonster(3 * 64, 3 * 64);
        monsters.add(monsterA);
        Monster monsterB = new ArcherMonster(0, 0);

        boolean canMove = canMonsterMove(monsterB, 3 * 64, 3 * 64);
        assertFalse("Cannot move onto a cell occupied by a different monster", canMove);
    }

    @Test
    public void testCanMonsterMove_sameMonsterMove() {
        Monster monsterA = new FighterMonster(3 * 64, 3 * 64);
        monsters.add(monsterA);

        boolean canMove = canMonsterMove(monsterA, 3 * 64, 3 * 64);
        assertTrue("The same monster can remain in its own cell", canMove);
    }

    @Test
    public void testCanMonsterMove_outOfBoundsNegative() {
        Monster monster = new ArcherMonster(0, 0);
        boolean canMove = canMonsterMove(monster, -64, -64);
        assertFalse("Cannot move to negative coordinates", canMove);
    }

    @Test
    public void testCanMonsterMove_outOfBoundsExceedingGrid() {
        Monster monster = new ArcherMonster(0, 0);
        boolean canMove = canMonsterMove(monster, GRID_COLS * cellSize, GRID_ROWS * cellSize);
        assertFalse("Cannot move beyond grid boundaries", canMove);
    }

    @Test
    public void testCanMonsterMove_blockedByObject() {
        placedObjects[3][3] = new BuildModePanel.PlacedObject();
        Monster monster = new FighterMonster(0, 0);
        boolean canMove = canMonsterMove(monster, 3 * 64, 3 * 64);
        assertFalse("Cannot move onto a cell blocked by a placed object", canMove);
    }
}
