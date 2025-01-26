package Domain;

import UI.BuildModePanel;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a snapshot of the game's state, used for saving and loading.
 */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private BuildModePanel.CellType[][] grid;
    private BuildModePanel.PlacedObject[][] placedObjects;
    private int heroHealth;
    private List<MonsterState> monsterStates;
    private int timeRemaining;
    private String hallName;
    private List<EnchantmentState> heroEnchantments;
    private List<EnchantmentState> floorEnchantments;
    private Point heroPixelPosition;
    private boolean runeFound = false;

    /**
     * Default constructor for an empty or initial GameState.
     */
    public GameState() {
        this.grid = null;
        this.placedObjects = null;
        this.heroHealth = 3;
        this.monsterStates = new ArrayList<>();
        this.timeRemaining = 0;
        this.hallName = "";
        this.heroEnchantments = new ArrayList<>();
        this.floorEnchantments = new ArrayList<>();
        this.heroPixelPosition = new Point(0, 0);
        this.runeFound = false;
    }

    /**
     * Constructs a fully populated GameState from the provided game data.
     * @param grid a 2D array representing the cell layout
     * @param placedObjects a 2D array of objects placed in the grid
     * @param hero the current Hero instance
     * @param monsters the list of active monsters
     * @param timeRemaining the hero's remaining time
     * @param hallName the name of the current hall
     * @param floorEnchantments the enchantments present on the floor
     * @param inventory the hero's inventory
     */
    public GameState(BuildModePanel.CellType[][] grid,
                     BuildModePanel.PlacedObject[][] placedObjects,
                     Hero hero,
                     List<Monster> monsters,
                     int timeRemaining,
                     String hallName,
                     List<Enchantment> floorEnchantments,
                     Inventory inventory) {
        this.grid = grid;
        this.placedObjects = placedObjects;
        this.heroPixelPosition = new Point(hero.getX(), hero.getY());
        System.out.println("Saving hero position: " + heroPixelPosition.x + "," + heroPixelPosition.y);
        this.heroHealth = hero.getHealth();
        this.timeRemaining = timeRemaining;
        this.hallName = hallName;

        this.monsterStates = monsters.stream()
                .map(m -> new MonsterState(m.getX() / 64, m.getY() / 64, m.getClass().getSimpleName()))
                .collect(Collectors.toList());

        this.heroEnchantments = new ArrayList<>();
        this.floorEnchantments = new ArrayList<>();

        this.floorEnchantments = floorEnchantments.stream()
                .map(Enchantment::toEnchantmentState)
                .collect(Collectors.toList());

        this.heroEnchantments = inventory.getCollectedEnchantments().stream()
                .map(Enchantment::toEnchantmentState)
                .collect(Collectors.toList());
    }

    /**
     * Returns the 2D array of cell types for the grid.
     * @return a 2D array of BuildModePanel.CellType
     */
    public BuildModePanel.CellType[][] getGrid() {
        return grid;
    }

    /**
     * Returns the 2D array of placed objects in the grid.
     * @return a 2D array of PlacedObject instances
     */
    public BuildModePanel.PlacedObject[][] getPlacedObjects() {
        return placedObjects;
    }

    /**
     * Provides the hero's current position in pixel coordinates.
     * @return a Point representing x and y in pixels
     */
    public Point getHeroPixelPosition() {
        return heroPixelPosition;
    }

    /**
     * Retrieves the hero's health stored in this GameState.
     * @return an integer health value
     */
    public int getHeroHealth() {
        return heroHealth;
    }

    /**
     * Returns the list of serialized monster states.
     * @return a list of MonsterState objects
     */
    public List<MonsterState> getMonsterStates() {
        return monsterStates;
    }

    /**
     * Indicates how many seconds remain for the hero.
     * @return an integer time in seconds
     */
    public int getTimeRemaining() {
        return timeRemaining;
    }

    /**
     * Retrieves the name of the current hall.
     * @return a String hall name
     */
    public String getHallName() {
        return hallName;
    }

    /**
     * Returns the list of enchantments currently in the hero's inventory.
     * @return a list of EnchantmentState objects
     */
    public List<EnchantmentState> getHeroEnchantments() {
        return heroEnchantments;
    }

    /**
     * Provides the list of enchantments positioned on the floor.
     * @return a list of EnchantmentState objects
     */
    public List<EnchantmentState> getFloorEnchantments() {
        return floorEnchantments;
    }

    /**
     * Sets the list of floor enchantments to the provided list.
     * @param list a list of EnchantmentState objects
     */
    public void setFloorEnchantments(List<EnchantmentState> list) {
        this.floorEnchantments = list;
    }

    /**
     * Updates the hero's enchantments to the provided list.
     * @param list a list of EnchantmentState objects
     */
    public void setHeroEnchantments(List<EnchantmentState> list) {
        this.heroEnchantments = list;
    }

    /**
     * Sets whether the rune has been found or not.
     * @param found true if found, false otherwise
     */
    public void setRuneFound(boolean found) {
        this.runeFound = found;
    }

    /**
     * Checks if the rune has been discovered by the hero.
     * @return true if found, false otherwise
     */
    public boolean isRuneFound() {
        return runeFound;
    }

    /**
     * Converts pixel coordinates to grid coordinates (assuming 64x64 cells).
     * @param pixelX the x position in pixels
     * @param pixelY the y position in pixels
     * @return a Point with grid-based x and y
     */
    public static Point pixelToGrid(int pixelX, int pixelY) {
        return new Point(pixelX / 64, pixelY / 64);
    }

    /**
     * Converts grid coordinates to pixel coordinates (assuming 64x64 cells).
     * @param gridX the grid x-coordinate
     * @param gridY the grid y-coordinate
     * @return a Point with pixel-based x and y
     */
    public static Point gridToPixel(int gridX, int gridY) {
        return new Point(gridX * 64, gridY * 64);
    }

    /**
     * A serializable inner class storing minimal monster information (position and type).
     */
    public static class MonsterState implements Serializable {
        private static final long serialVersionUID = 1L;

        private int gridX;
        private int gridY;
        private String type;

        /**
         * Constructs a MonsterState from the monster's grid coordinates and type.
         * @param x the monster's grid x-position
         * @param y the monster's grid y-position
         * @param type the class name of the monster
         */
        public MonsterState(int x, int y, String type) {
            this.gridX = x;
            this.gridY = y;
            this.type = type;
        }

        public int getGridX() {
            return gridX;
        }

        public int getGridY() {
            return gridY;
        }

        public String getType() {
            return type;
        }
    }

    /**
     * A serializable representation of an Enchantment's essential data (type, position, size, spawn time).
     */
    public static class EnchantmentState implements Serializable {
        private static final long serialVersionUID = 1L;

        private String type;
        private int x, y, width, height;
        private long spawnTime;

        /**
         * Constructs an EnchantmentState with all necessary attributes.
         * @param type the name of the EnchantmentType
         * @param x the x-coordinate
         * @param y the y-coordinate
         * @param width the width
         * @param height the height
         * @param spawnTime the time when the enchantment was created
         */
        public EnchantmentState(String type, int x, int y, int width, int height, long spawnTime) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.spawnTime = spawnTime;
        }

        public String getType() { return type; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public long getSpawnTime() { return spawnTime; }

        /**
         * Converts this EnchantmentState back into a functional Enchantment instance.
         * @return a new Enchantment object
         */
        public Enchantment toEnchantment() {
            return new Enchantment(
                    x, y, width, height,
                    EnchantmentType.valueOf(type),
                    spawnTime
            );
        }
    }
}
