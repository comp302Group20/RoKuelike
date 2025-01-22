package Domain;

import UI.BuildModePanel;
import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private BuildModePanel.CellType[][] grid;
    private BuildModePanel.PlacedObject[][] placedObjects;
    private int heroHealth;
    private List<MonsterState> monsterStates;
    private int timeRemaining;
    private String hallName;
    private List<EnchantmentState> heroEnchantments; // For hero's bag of enchantments

    private List<EnchantmentState> floorEnchantments;

    private Point heroPixelPosition;

    private boolean runeFound = false;

    public GameState() {
        this.grid = null;
        this.placedObjects = null;
        this.heroHealth = 3;  // Default health
        this.monsterStates = new ArrayList<>();
        this.timeRemaining = 0;
        this.hallName = "";
        this.heroEnchantments = new ArrayList<>();
        this.floorEnchantments = new ArrayList<>();
        this.heroPixelPosition = new Point(0, 0);
        this.runeFound = false;
    }

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
        this.heroPixelPosition = new Point(hero.getX(), hero.getY()); // Store exact pixel position
        this.heroHealth = hero.getHealth();
        this.timeRemaining = timeRemaining;
        this.hallName = hallName;

        this.monsterStates = monsters.stream()
                .map(m -> new MonsterState(m.getX() / 64, m.getY() / 64, m.getClass().getSimpleName()))
                .collect(Collectors.toList());

        this.heroEnchantments = new ArrayList<>();
        this.floorEnchantments = new ArrayList<>();

        // Save floor enchantments
        this.floorEnchantments = floorEnchantments.stream()
                .map(Enchantment::toEnchantmentState)
                .collect(Collectors.toList());

        // Save inventory enchantments
        this.heroEnchantments = inventory.getCollectedEnchantments().stream()
                .map(Enchantment::toEnchantmentState)
                .collect(Collectors.toList());
    }

    // Getters
    public BuildModePanel.CellType[][] getGrid() {
        return grid;
    }

    public BuildModePanel.PlacedObject[][] getPlacedObjects() {
        return placedObjects;
    }

    public Point getHeroPixelPosition() {
        return heroPixelPosition;
    }

    public int getHeroHealth() {
        return heroHealth;
    }

    public List<MonsterState> getMonsterStates() {
        return monsterStates;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public String getHallName() {
        return hallName;
    }

    public List<EnchantmentState> getHeroEnchantments() {
        return heroEnchantments;
    }

    // ---------- NEW: floor enchantments methods ----------
    public List<EnchantmentState> getFloorEnchantments() {
        return floorEnchantments;
    }
    public void setFloorEnchantments(List<EnchantmentState> list) {
        this.floorEnchantments = list;
    }

    // If you want to save hero enchantments, do something like:
    public void setHeroEnchantments(List<EnchantmentState> list) {
        this.heroEnchantments = list;
    }

    // Inner classes for serialization
    public static class MonsterState implements Serializable {
        private static final long serialVersionUID = 1L;

        private int gridX;
        private int gridY;
        private String type; // "ArcherMonster", "FighterMonster", or "WizardMonster"

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

    // In GameState.java
    public static class EnchantmentState implements Serializable {
        private static final long serialVersionUID = 1L;

        private String type;
        private int x, y, width, height;
        private long spawnTime;

        public EnchantmentState(String type, int x, int y, int width, int height, long spawnTime) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.spawnTime = spawnTime;
        }

        // Add getters
        public String getType() { return type; }
        public int getX() { return x; }
        public int getY() { return y; }
        public int getWidth() { return width; }
        public int getHeight() { return height; }
        public long getSpawnTime() { return spawnTime; }

        // Helper method to convert state back to Enchantment
        public Enchantment toEnchantment() {
            return new Enchantment(
                    x, y, width, height,
                    EnchantmentType.valueOf(type),
                    spawnTime
            );
        }
    }

    // Helper method to convert pixel coordinates to grid coordinates
    public static Point pixelToGrid(int pixelX, int pixelY) {
        return new Point(pixelX / 64, pixelY / 64);
    }

    // Helper method to convert grid coordinates to pixel coordinates
    public static Point gridToPixel(int gridX, int gridY) {
        return new Point(gridX * 64, gridY * 64);
    }

    public void setRuneFound(boolean found) {
        this.runeFound = found;
    }

    public boolean isRuneFound() {
        return runeFound;
    }
}
