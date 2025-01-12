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
 * JUnit tests for the handleMovementKeys method.
 */
@RunWith(JUnit4.class)
public class HandleMovementKeysTest {

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
    public HandleMovementKeysTest() {
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
    // handleMovementKeys Method
    // ---------------------------------------------------
    /**
     * requires: e != null (KeyEvent is valid).
     * modifies: this.hero (the hero's position).
     * effects: Moves the hero's position one cell up/down/left/right if possible,
     *          depending on the key pressed (arrow keys).
     *          If movement is blocked (wall, object, monster, out of bounds), hero doesn't move.
     */
    public void handleMovementKeys(KeyEvent e) {
        // Don't move hero if paused/game over/hero died
        if (isPaused || gameOver || heroDied) return;

        int step = cellSize;
        int dx = 0, dy = 0;
        switch (e.getKeyCode()) {
            case KeyEvent.VK_LEFT:  dx = -step; break;
            case KeyEvent.VK_RIGHT: dx =  step; break;
            case KeyEvent.VK_UP:    dy = -step; break;
            case KeyEvent.VK_DOWN:  dy =  step; break;
        }
        if (dx != 0 || dy != 0) {
            int oldX = hero.getX();
            int oldY = hero.getY();
            hero.move(dx, dy);

            Point newPos = new Point(hero.getX(), hero.getY());
            if (!canHeroMove(newPos)) {
                // revert if invalid
                hero.setPosition(oldX, oldY);
            }
        }
    }

    private boolean canHeroMove(Point p) {
        int c = p.x / cellSize;
        int r = p.y / cellSize;
        if (r < 0 || r >= GRID_ROWS || c < 0 || c >= GRID_COLS) return false;
        // block if WALL
        if (grid[r][c] == BuildModePanel.CellType.WALL) return false;
        // block if there's a placed object
        BuildModePanel.PlacedObject po = placedObjects[r][c];
        if (po != null) return false;
        // block if any monster is at (p.x, p.y)
        for (Monster m : monsters) {
            if (m.getX() == p.x && m.getY() == p.y) return false;
        }
        return true;
    }

    // ---------------------------------------------------
    // JUnit Tests for handleMovementKeys
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
    public void testHandleMovementKeys_moveUp() {
        hero.setPosition(64, 64); // row=1, col=1
        KeyEvent upEvent = new KeyEvent(
                new Button(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED
        );
        handleMovementKeys(upEvent);
        assertEquals("Hero Y should be 0 after moving up one cell", 0, hero.getY());
        assertEquals("Hero X should remain 64", 64, hero.getX());
    }

    @Test
    public void testHandleMovementKeys_blockedByWall() {
        grid[0][1] = BuildModePanel.CellType.WALL; // the cell above hero
        hero.setPosition(64, 64);
        KeyEvent upEvent = new KeyEvent(
                new Button(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, KeyEvent.VK_UP, KeyEvent.CHAR_UNDEFINED
        );
        handleMovementKeys(upEvent);
        // Should not move
        assertEquals("Hero remains at Y=64 if blocked", 64, hero.getY());
    }

    @Test
    public void testHandleMovementKeys_moveRightWithinBounds() {
        hero.setPosition(64, 64);
        KeyEvent rightEvent = new KeyEvent(
                new Button(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED
        );
        handleMovementKeys(rightEvent);
        assertEquals("Hero should move to X=128", 128, hero.getX());
        assertEquals("Hero Y stays the same", 64, hero.getY());
    }

    @Test
    public void testHandleMovementKeys_moveDownAtBoundary() {
        // row=12 => y=12*64=768 is bottom row
        hero.setPosition(64, 768);
        KeyEvent downEvent = new KeyEvent(
                new Button(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, KeyEvent.VK_DOWN, KeyEvent.CHAR_UNDEFINED
        );
        handleMovementKeys(downEvent);
        // Should remain the same if it's out of bounds
        assertEquals("Hero can't move out of bottom boundary", 768, hero.getY());
    }

    @Test
    public void testHandleMovementKeys_heroDiedOrPaused_NoMovement() {
        heroDied = true;  // hero is dead
        int oldX = hero.getX();
        KeyEvent rightEvent = new KeyEvent(
                new Button(), KeyEvent.KEY_PRESSED, System.currentTimeMillis(),
                0, KeyEvent.VK_RIGHT, KeyEvent.CHAR_UNDEFINED
        );
        handleMovementKeys(rightEvent);
        assertEquals("Hero won't move if heroDied is true", oldX, hero.getX());

        // Also test when paused
        init();
        isPaused = true;
        int pausedX = hero.getX();
        handleMovementKeys(rightEvent);
        assertEquals("Hero won't move if game is paused", pausedX, hero.getX());

        // Also test when gameOver
        init();
        gameOver = true;
        int gameOverX = hero.getX();
        handleMovementKeys(rightEvent);
        assertEquals("Hero won't move if game is over", gameOverX, hero.getX());
    }
}
