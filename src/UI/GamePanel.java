package UI;

import Controller.GameController;
import Domain.Hero;
import Domain.Monster;
import Domain.ArcherMonster;
import Domain.FighterMonster;
import Domain.WizardMonster;
import UI.BuildModePanel.PlacedObject;
import Utils.AssetPaths;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import Domain.Enchantment;
import Domain.EnchantmentType;
import Domain.Inventory;

/**
 * The main panel for playing the game. Handles rendering, user input,
 * monster spawning/movement, enchantment usage, and game-over conditions.
 */
public class GamePanel extends JPanel {

    private BuildModePanel.CellType[][] grid;
    private PlacedObject[][] placedObjects;
    private static final int GRID_ROWS = 13;
    private static final int GRID_COLS = 13;
    private int cellSize = 64;

    private BufferedImage floorImage;
    private BufferedImage horizontalWallImage;
    private BufferedImage leftVerticalWallImage;
    private BufferedImage rightVerticalWallImage;

    private BufferedImage pauseButtonImage;
    private BufferedImage resumeButtonImage;
    private BufferedImage exitButtonImage;
    private BufferedImage runeImage;
    private BufferedImage doorImage;
    private BufferedImage gameOverImage;
    private BufferedImage diedHeroImage;
    private BufferedImage heartImage;

    private List<Monster> monsters;
    private Hero hero;
    private Random random;

    private Timer monsterSpawnerTimer;
    private Timer monsterMovementTimer;
    private Timer gameOverTimer;
    private Timer redirectTimer;
    private Timer enchantmentSpawnTimer;

    private boolean isPaused = false;
    private boolean gameOver = false;
    private boolean heroDied = false;

    private static final int DOOR_ROW = 11;
    private static final int DOOR_COL = 6;

    private JButton pauseButton;
    private JButton exitButton;

    private int timeRemaining; // seconds
    private List<Enchantment> enchantments = new ArrayList<>();

    // Inventory for storing enchantments
    private Inventory inventory;

    // For the "reveal" effect:
    private boolean revealActive = false;
    private long revealEndTime = 0L;
    // We'll store a random 4×4 region that contains the rune
    private int revealTopRow, revealLeftCol;
    private static final long REVEAL_DURATION_MS = 10_000; // 10s in ms

    // For the cloak effect:
    private boolean cloakActive = false;
    private long cloakEndTime = 0L;
    private static final long CLOAK_DURATION_MS = 20_000; // 20s in ms

    private GameController gameController;

    public GamePanel(BuildModePanel.CellType[][] g, PlacedObject[][] p, GameController controller) {
        this.grid = g;
        this.placedObjects = p;
        this.gameController = controller;
        setBackground(Color.BLACK);

        hero = new Hero(2 * cellSize, 2 * cellSize, cellSize, cellSize);
        monsters = new ArrayList<>();
        random = new Random();

        // Create our inventory
        inventory = new Inventory();

        hideRuneInRandomObject();
        loadDoorImage();
        placeDoorAsObject();
        loadGameOverImage();
        loadDiedHeroImage();
        loadHeartImage();
        initializeFloorWallImages();
        initializeButtonImages();
        loadRuneImage();

        createPauseButton();
        createExitButton();

        startMonsterSpawner();
        startMonsterMovement();
        startEnchantmentSpawner();

        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Don't move hero if paused/game over/hero died
                if (!isPaused && !gameOver && !heroDied) {
                    handleMovementKeys(e);
                }
                // Handle usage of enchantments (R for Reveal, P for Cloak)
                if (!gameOver && !heroDied) {
                    handleEnchantmentKeys(e);
                }
            }
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isPaused || gameOver || heroDied) return;
                if (e.getButton() != MouseEvent.BUTTON1) return;

                int mx = e.getX();
                int my = e.getY();

                // 1) Check if the user clicked on an enchantment
                Enchantment clickedEnch = getClickedEnchantment(mx, my);
                if (clickedEnch != null) {
                    collectEnchantment(clickedEnch);
                    return;
                }

                // 2) Check if the user clicked an object with a hidden rune
                PlacedObject obj = getClickedObject(mx, my);
                if (obj != null && obj.hasRune) {
                    // We'll reveal it automatically
                    obj.runeVisible = true;
                    System.out.println("Rune discovered!");
                }
            }
        });
    }

    /**
     * Increase hero's time left (UI).
     */
    public void updateTime(int timeRemaining) {
        this.timeRemaining = timeRemaining;
        repaint();
    }

    /**
     * Triggers game over screen.
     */
    public void triggerGameOver() {
        gameOver = true;
        SwingUtilities.invokeLater(this::repaint);
    }

    /**
     * Handle arrow-key or WASD movement.
     */
    private void handleMovementKeys(KeyEvent e) {
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

            Point np = new Point(hero.getX(), hero.getY());
            if (!canHeroMove(np)) {
                hero.setPosition(oldX, oldY);
            }
            checkDoorCondition();
            checkHealthCondition();
            repaint();
        }
    }

    /**
     * Handle usage of enchantments:
     *  - 'R' for Reveal
     *  - 'P' for Cloak
     */
    private void handleEnchantmentKeys(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_R) {
            // Use a Reveal if we have any
            int idx = findEnchantmentIndex(EnchantmentType.REVEAL);
            if (idx >= 0) {
                // Remove one from inventory
                inventory.getCollectedEnchantments().remove(idx);

                // Mark reveal as active for 10s
                revealActive = true;
                revealEndTime = System.currentTimeMillis() + REVEAL_DURATION_MS;

                // Choose a 4×4 region that definitely contains the rune
                pickRevealRegion();
                System.out.println("Reveal used! Highlighting a 4×4 region for 10s.");
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_P) {
            // Use a Cloak if we have any
            int idx = findEnchantmentIndex(EnchantmentType.CLOAK);
            if (idx >= 0) {
                inventory.getCollectedEnchantments().remove(idx);

                cloakActive = true;
                cloakEndTime = System.currentTimeMillis() + CLOAK_DURATION_MS;
                System.out.println("Cloak of Protection used! Archer cannot see (or hurt) hero for 20s.");
            }
        }
    }

    /**
     * Find the first index of an enchantment of the given type in the inventory, or -1 if none.
     */
    private int findEnchantmentIndex(EnchantmentType type) {
        List<Enchantment> list = inventory.getCollectedEnchantments();
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getType() == type) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Picks a 4×4 region that definitely contains the current rune-holding object (if any).
     */
    private void pickRevealRegion() {
        PlacedObject target = null;
        int runeRow = -1, runeCol = -1;
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                PlacedObject po = placedObjects[r][c];
                if (po != null && po.hasRune) {
                    target = po;
                    runeRow = r;
                    runeCol = c;
                    break;
                }
            }
            if (target != null) break;
        }
        if (target == null) {
            // No rune found
            revealTopRow = 0;
            revealLeftCol = 0;
            return;
        }

        int maxRowStart = GRID_ROWS - 4;
        int maxColStart = GRID_COLS - 4;

        int offsetR = Math.min(runeRow, 3);
        int offsetC = Math.min(runeCol, 3);

        revealTopRow = runeRow - offsetR;
        revealLeftCol = runeCol - offsetC;

        if (revealTopRow < 0) revealTopRow = 0;
        if (revealTopRow > maxRowStart) revealTopRow = maxRowStart;
        if (revealLeftCol < 0) revealLeftCol = 0;
        if (revealLeftCol > maxColStart) revealLeftCol = maxColStart;
    }

    /**
     * Spawns new enchantments every 12s.
     */
    private void startEnchantmentSpawner() {
        enchantmentSpawnTimer = new Timer(true);
        enchantmentSpawnTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isPaused && !gameOver && !heroDied) {
                    spawnRandomEnchantment();
                }
            }
        }, 0, 12000);
    }

    /**
     * Spawns a random enchantment on a floor cell. (Tries up to 50 times.)
     */
    private void spawnRandomEnchantment() {
        int tries = 0;
        while (tries < 50) {
            int r = 2 + random.nextInt(10);
            int c = 1 + random.nextInt(11);
            if (grid[r][c] == BuildModePanel.CellType.FLOOR && placedObjects[r][c] == null) {
                EnchantmentType etype = EnchantmentType.getRandomType(random);
                Enchantment ench = new Enchantment(
                        c * cellSize,
                        r * cellSize,
                        cellSize, cellSize,
                        etype
                );
                enchantments.add(ench);
                break;
            }
            tries++;
        }
        SwingUtilities.invokeLater(this::repaint);
    }

    /**
     * Checks if the user clicked on an enchantment.
     */
    private Enchantment getClickedEnchantment(int mx, int my) {
        for (Enchantment e : enchantments) {
            int ex = e.getX();
            int ey = e.getY();
            int ew = e.getWidth();
            int eh = e.getHeight();
            if (mx >= ex && mx < ex + ew && my >= ey && my < ey + eh) {
                return e;
            }
        }
        return null;
    }

    /**
     * Collect the enchantment: +1 life if EXTRALIFE, +5 seconds if EXTRATIME, or store in inventory otherwise.
     * Also syncs new time with gameController if EXTRATIME.
     */
    private void collectEnchantment(Enchantment ench) {
        switch (ench.getType()) {
            case EXTRALIFE:
                // Unlimited hearts
                hero.setHealth(hero.getHealth() + 1);
                System.out.println("Collected EXTRA LIFE. Hero health: " + hero.getHealth());
                break;
            case EXTRATIME:
                // Now it is +5 seconds
                if (gameController != null && gameController.getGameTimer() != null) {
                    gameController.getGameTimer().addTime(6);
                    System.out.println("Collected EXTRA TIME. Timer is now "
                            + gameController.getGameTimer().getTimeRemaining() + "s");
                }
                break;
            default:
                // Store in inventory: REVEAL, CLOAK, LURINGGEM, etc.
                inventory.addEnchantment(ench);
                System.out.println("Collected " + ench.getType().name() + " (stored in inventory).");
                break;
        }
        enchantments.remove(ench);
        repaint();
    }

    /**
     * Removes any enchantments older than 6s.
     */
    private void checkEnchantmentExpiry() {
        enchantments.removeIf(Enchantment::isExpired);
    }

    /**
     * Spawns monsters every 8s.
     */
    private void startMonsterSpawner() {
        monsterSpawnerTimer = new Timer(true);
        monsterSpawnerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isPaused && !gameOver && !heroDied) {
                    spawnMonster();
                    repaint();
                }
            }
        }, 0, 8000);
    }

    /**
     * Move monsters every 0.5s, also remove expired enchantments.
     */
    private void startMonsterMovement() {
        monsterMovementTimer = new Timer(true);
        monsterMovementTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isPaused && !gameOver && !heroDied) {
                    for (Monster m : monsters) {
                        m.update();
                    }
                    checkEnchantmentExpiry();
                    checkHealthCondition();
                    repaint();
                }
            }
        }, 0, 500);
    }

    /**
     * Attempt to spawn a monster in a random valid location.
     */
    private void spawnMonster() {
        int tries = 0;
        while (tries < 50) {
            int c = random.nextInt(GRID_COLS - 2) + 1;
            int r = random.nextInt(GRID_ROWS - 2) + 1;
            Point pt = new Point(c * cellSize, r * cellSize);
            if (canMonsterMove(null, pt.x, pt.y) &&
                    (pt.x != hero.getX() || pt.y != hero.getY())) {

                Monster m;
                int t = random.nextInt(3);
                if (t == 0) {
                    m = new ArcherMonster(pt.x, pt.y, hero, grid, this);
                } else if (t == 1) {
                    m = new FighterMonster(pt.x, pt.y, hero, grid, this);
                } else {
                    m = new WizardMonster(pt.x, pt.y, hero, grid, this);
                }
                monsters.add(m);
                break;
            }
            tries++;
        }
    }

    /**
     * Whether a monster can move/spawn at (nx, ny).
     */
    public boolean canMonsterMove(Monster monster, int nx, int ny) {
        int c = nx / cellSize;
        int r = ny / cellSize;
        if (r < 0 || r >= GRID_ROWS || c < 0 || c >= GRID_COLS) return false;
        if (grid[r][c] == BuildModePanel.CellType.WALL) return false;
        PlacedObject po = placedObjects[r][c];
        if (po != null && po != placedObjects[DOOR_ROW][DOOR_COL]) return false;

        // If there's already another monster at that position, no
        for (Monster mm : monsters) {
            if (mm != monster && mm.getX() == nx && mm.getY() == ny) {
                return false;
            }
        }
        // Also not if hero is there
        if (hero.getX() == nx && hero.getY() == ny) return false;

        return true;
    }

    /**
     * Whether the hero can move to p.
     */
    private boolean canHeroMove(Point p) {
        int c = p.x / cellSize;
        int r = p.y / cellSize;
        if (r < 0 || r >= GRID_ROWS || c < 0 || c >= GRID_COLS) return false;
        if (grid[r][c] == BuildModePanel.CellType.WALL) return false;
        PlacedObject po = placedObjects[r][c];
        if (po != null && po != placedObjects[DOOR_ROW][DOOR_COL]) return false;

        // Can't overlap a monster
        for (Monster mm : monsters) {
            if (mm.getX() == p.x && mm.getY() == p.y) {
                return false;
            }
        }
        return true;
    }

    private PlacedObject getClickedObject(int mx, int my) {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                PlacedObject obj = placedObjects[r][c];
                if (obj != null) {
                    int topY = r * cellSize;
                    int leftX = c * cellSize;
                    int w = cellSize;
                    int h = obj.isDouble ? cellSize * 2 : cellSize;
                    if (obj.isDouble) {
                        topY -= (h - cellSize);
                    }
                    if (mx >= leftX && mx < leftX + w && my >= topY && my < topY + h) {
                        return obj;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Teleport the rune if wizard monster triggers it.
     */
    public void teleportRuneRandomly() {
        if (gameOver) return;
        List<PlacedObject> allObjects = new ArrayList<>();
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                PlacedObject obj = placedObjects[r][c];
                if (obj != null) {
                    allObjects.add(obj);
                }
            }
        }
        // Find who currently has the rune
        PlacedObject runeHolder = null;
        for (PlacedObject po : allObjects) {
            if (po.hasRune) {
                runeHolder = po;
                break;
            }
        }
        if (runeHolder == null) return;
        // Hide the old
        runeHolder.hasRune = false;
        runeHolder.runeVisible = false;
        // Move to a new random
        if (!allObjects.isEmpty()) {
            int idx = random.nextInt(allObjects.size());
            allObjects.get(idx).hasRune = true;
        }
        repaint();
    }

    /**
     * Called to see if hero is next to the door with the rune to escape.
     */
    private void checkDoorCondition() {
        int hr = hero.getY() / cellSize;
        int hc = hero.getX() / cellSize;
        if (hr == DOOR_ROW - 1 && hc == DOOR_COL) {
            if (heroHasRune()) {
                // Stop timers
                if (monsterSpawnerTimer != null) monsterSpawnerTimer.cancel();
                if (monsterMovementTimer != null) monsterMovementTimer.cancel();
                if (enchantmentSpawnTimer != null) enchantmentSpawnTimer.cancel();

                System.out.println("Hero escaped with the rune!");
                gameController.onHeroEscaped();
            }
        }
    }

    /**
     * Checks if hero's health is 0 → heroDied animation.
     */
    private void checkHealthCondition() {
        if (hero.getHealth() <= 0 && !heroDied && !gameOver) {
            heroDied = true;
            loadDiedHeroImage();
            isPaused = true;
            System.out.println("Hero died");

            // Cancel timers
            if (monsterSpawnerTimer != null) monsterSpawnerTimer.cancel();
            if (monsterMovementTimer != null) monsterMovementTimer.cancel();
            if (enchantmentSpawnTimer != null) enchantmentSpawnTimer.cancel();

            // 2s before official game over
            gameOverTimer = new Timer(true);
            gameOverTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    gameOver = true;
                    heroDied = false;
                    SwingUtilities.invokeLater(() -> repaint());

                    // 3s after that, go to main menu
                    redirectTimer = new Timer(true);
                    redirectTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() -> {
                                JFrame mm = new RokueLikeMainMenu();
                                mm.setVisible(true);
                                SwingUtilities.getWindowAncestor(GamePanel.this).dispose();
                            });
                        }
                    }, 3000);
                }
            }, 2000);

            SwingUtilities.invokeLater(this::repaint);
        }
    }

    /**
     * Does hero already have a revealed rune?
     */
    private boolean heroHasRune() {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                PlacedObject po = placedObjects[r][c];
                if (po != null && po.hasRune && po.runeVisible) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // If gameOver, show image and stop drawing
        if (gameOver && gameOverImage != null) {
            hideButtonsIfGameOver();
            int imgWidth = 900, imgHeight = 900;
            int x = (getWidth() - imgWidth) / 2;
            int y = (getHeight() - imgHeight) / 2;
            g.drawImage(gameOverImage, x, y, imgWidth, imgHeight, null);
            return;
        }

        // If heroDied, show diedHeroImage
        if (heroDied && diedHeroImage != null) {
            hideButtonsIfGameOver();
            g.drawImage(diedHeroImage, hero.getX(), hero.getY(), cellSize, cellSize, null);
            return;
        }

        drawBoard(g);
        drawGridLines(g);
        drawPlacedObjects(g);

        // Draw hero
        hero.draw(g);

        // Draw monsters (some may be behind objects)
        for (Monster m : monsters) {
            if (!isCoveredByObject(m)) {
                m.draw(g);
            }
        }
        highlightArcherZones(g);

        for (Monster m : monsters) {
            if (isCoveredByObject(m)) {
                m.draw(g);
            }
        }
        if (!isCoveredByObject(hero)) {
            hero.draw(g);
        }

        // Draw enchantments
        for (Enchantment e : enchantments) {
            e.draw(g);
        }

        // Time
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Time Remaining: " + timeRemaining + "s", 1000, 400);

        // Draw double-height objects above hero
        drawObjectsAboveHero(g);

        // Draw hearts
        drawHearts(g);

        // Check reveal duration
        long now = System.currentTimeMillis();
        if (revealActive) {
            if (now > revealEndTime) {
                revealActive = false;
            } else {
                drawRevealHighlight(g);
            }
        }

        // Check cloak duration
        if (cloakActive && now > cloakEndTime) {
            cloakActive = false;
            System.out.println("Cloak of Protection wore off.");
        }

        // Draw the inventory
        if (inventory != null) {
            inventory.draw(g, 850, 200);
        }
    }

    /**
     * Draw a 4×4 green rectangle for reveal effect.
     */
    private void drawRevealHighlight(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setColor(new Color(0, 255, 0, 60)); // transparent green
        int highlightW = cellSize * 4;
        int highlightH = cellSize * 4;
        int x = revealLeftCol * cellSize;
        int y = revealTopRow * cellSize;
        g2.fillRect(x, y, highlightW, highlightH);
        g2.dispose();
    }

    /**
     * Archer-monster coverage.
     * If cloak is active, archer effectively does NOT see the hero (skip BFS coverage).
     */
    private void highlightArcherZones(Graphics g) {
        // If cloak is active, skip BFS coverage entirely so archers can't see the hero
        if (cloakActive) {
            return;
        }

        boolean[][] coverage = new boolean[GRID_ROWS][GRID_COLS];
        for (Monster m : monsters) {
            if (m instanceof ArcherMonster) {
                int startR = m.getY() / cellSize;
                int startC = m.getX() / cellSize;
                Queue<int[]> queue = new LinkedList<>();
                queue.add(new int[]{startR, startC, 0});
                boolean[][] visited = new boolean[GRID_ROWS][GRID_COLS];
                visited[startR][startC] = true;
                while (!queue.isEmpty()) {
                    int[] curr = queue.poll();
                    int rr = curr[0];
                    int cc = curr[1];
                    int dist = curr[2];
                    if (dist < 4) {
                        coverage[rr][cc] = true;
                        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
                        for (int[] d : dirs) {
                            int nr = rr + d[0];
                            int nc = cc + d[1];
                            if (nr >= 0 && nr < GRID_ROWS && nc >= 0 && nc < GRID_COLS) {
                                if (!visited[nr][nc] && grid[nr][nc] != BuildModePanel.CellType.WALL) {
                                    visited[nr][nc] = true;
                                    queue.add(new int[]{nr, nc, dist+1});
                                }
                            }
                        }
                    }
                }
            }
        }

        // Draw coverage in yellow
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(new Color(255, 255, 0, 10));
        for (int rr = 0; rr < GRID_ROWS; rr++) {
            for (int cc = 0; cc < GRID_COLS; cc++) {
                if (coverage[rr][cc] && grid[rr][cc] != BuildModePanel.CellType.WALL) {
                    g2d.fillRect(cc * cellSize, rr * cellSize, cellSize, cellSize);
                }
            }
        }
        g2d.dispose();
    }

    /**
     * Draw unlimited hearts = hero's current health.
     */
    private void drawHearts(Graphics g) {
        if (heartImage == null) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Health: " + hero.getHealth(), 10, 20);
            return;
        }
        int heartWidth = 40;
        int heartHeight = 40;
        for (int i = 0; i < hero.getHealth(); i++) {
            int xPos = 10 + i * (heartWidth + 5);
            int yPos = 10;
            g.drawImage(heartImage, xPos, yPos, heartWidth, heartHeight, null);
        }
    }

    /**
     * Draw the board's floor and walls.
     */
    private void drawBoard(Graphics g) {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                int x = c * cellSize;
                int y = r * cellSize;
                g.drawImage(floorImage, x, y, cellSize, cellSize, null);

                if (grid[r][c] == BuildModePanel.CellType.WALL) {
                    if (c == 0) {
                        g.drawImage(leftVerticalWallImage, x, y, cellSize, cellSize, null);
                    } else if (c == GRID_COLS - 1) {
                        g.drawImage(rightVerticalWallImage, x, y, cellSize, cellSize, null);
                    } else {
                        g.drawImage(horizontalWallImage, x, y, cellSize, cellSize, null);
                    }
                }
            }
        }
    }

    /**
     * Draw faint grid lines (optional).
     */
    private void drawGridLines(Graphics g) {
        g.setColor(new Color(75, 30, 30, 50));
        for (int x = 0; x <= GRID_COLS; x++) {
            g.drawLine(x * cellSize, 0, x * cellSize, GRID_ROWS * cellSize);
        }
        for (int y = 0; y <= GRID_ROWS; y++) {
            g.drawLine(0, y * cellSize, GRID_COLS * cellSize, y * cellSize);
        }
    }

    /**
     * Draw placed objects such as boxes, crates, door, etc.
     */
    private void drawPlacedObjects(Graphics g) {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                PlacedObject obj = placedObjects[r][c];
                if (obj != null) {
                    int dx = c * cellSize;
                    int dy = r * cellSize;
                    int dw = cellSize;
                    int dh = obj.isDouble ? cellSize * 2 : cellSize;
                    if (obj.isDouble) {
                        dy -= (dh - cellSize);
                    }
                    g.drawImage(obj.image, dx, dy, dw, dh, null);

                    if (obj.runeVisible && runeImage != null) {
                        g.drawImage(runeImage, c * cellSize, r * cellSize, cellSize, cellSize, null);
                    }
                }
            }
        }
    }

    /**
     * Determine if hero/monster is behind a double-height object.
     */
    private boolean isCoveredByObject(Object entity) {
        int ex = (entity instanceof Monster) ? ((Monster) entity).getX() : hero.getX();
        int ey = (entity instanceof Monster) ? ((Monster) entity).getY() : hero.getY();
        int ec = ex / cellSize;
        int er = ey / cellSize;
        if (er < 0 || er >= GRID_ROWS || ec < 0 || ec >= GRID_COLS) return false;
        PlacedObject obj = placedObjects[er][ec];
        return obj != null && obj.isDouble;
    }

    /**
     * Draw double-height objects above hero/monsters for that "2.5D" effect.
     */
    private void drawObjectsAboveHero(Graphics g) {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                PlacedObject obj = placedObjects[r][c];
                if (obj != null && obj.isDouble) {
                    int dx = c * cellSize;
                    int dy = r * cellSize - cellSize;
                    g.drawImage(obj.image, dx, dy, cellSize, cellSize * 2, null);

                    if (obj.runeVisible && runeImage != null) {
                        g.drawImage(runeImage, c * cellSize, r * cellSize, cellSize, cellSize, null);
                    }
                }
            }
        }
    }

    /**
     * Load the died-hero image (mirrored if hero is facing right).
     */
    private void loadDiedHeroImage() {
        try {
            URL diedHeroUrl = getClass().getClassLoader().getResource(AssetPaths.DIED_HERO.substring(1));
            if (diedHeroUrl != null) {
                BufferedImage originalImage = ImageIO.read(diedHeroUrl);
                if (!hero.isFacingLeft()) {
                    diedHeroImage = mirrorImage(originalImage);
                } else {
                    diedHeroImage = originalImage;
                }
            }
        } catch (IOException e) {
            diedHeroImage = null;
            e.printStackTrace();
        }
    }

    public BufferedImage mirrorImage(BufferedImage original) {
        AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
        transform.translate(-original.getWidth(), 0);
        BufferedImage mirrored = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                original.getType()
        );
        Graphics2D g = mirrored.createGraphics();
        g.drawImage(original, transform, null);
        g.dispose();
        return mirrored;
    }

    /**
     * Load door image.
     */
    private void loadDoorImage() {
        try {
            URL doorUrl = getClass().getClassLoader().getResource(AssetPaths.DOOR_IMAGE.substring(1));
            if (doorUrl != null) {
                doorImage = ImageIO.read(doorUrl);
            }
        } catch (IOException e) {
            doorImage = null;
        }
    }

    /**
     * Place door object at (DOOR_ROW, DOOR_COL).
     */
    private void placeDoorAsObject() {
        if (doorImage != null) {
            placedObjects[DOOR_ROW][DOOR_COL] = new BuildModePanel.PlacedObject(
                    toBufferedImage(doorImage),
                    DOOR_ROW,
                    DOOR_COL,
                    false
            );
        }
    }

    private BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }
        BufferedImage b = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = b.createGraphics();
        g2d.drawImage(img, 0, 0, null);
        g2d.dispose();
        return b;
    }

    /**
     * Load heart image.
     */
    private void loadHeartImage() {
        try {
            URL heartUrl = getClass().getClassLoader().getResource(AssetPaths.HEART.substring(1));
            if (heartUrl != null) {
                heartImage = ImageIO.read(heartUrl);
            }
        } catch (IOException e) {
            heartImage = null;
        }
    }

    /**
     * Hide the rune in a random placed object.
     */
    private void hideRuneInRandomObject() {
        List<PlacedObject> objs = new ArrayList<>();
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                if (placedObjects[r][c] != null) {
                    objs.add(placedObjects[r][c]);
                }
            }
        }
        if (!objs.isEmpty()) {
            int idx = random.nextInt(objs.size());
            objs.get(idx).hasRune = true;
        }
    }

    /**
     * Load game-over image.
     */
    private void loadGameOverImage() {
        try {
            URL go = getClass().getClassLoader().getResource(AssetPaths.GAME_OVER.substring(1));
            if (go != null) {
                gameOverImage = ImageIO.read(go);
            }
        } catch (IOException e) {
            gameOverImage = null;
        }
    }

    /**
     * Load rune image.
     */
    private void loadRuneImage() {
        try {
            URL r = getClass().getClassLoader().getResource(AssetPaths.RUNE.substring(1));
            if (r != null) {
                runeImage = ImageIO.read(r);
            }
        } catch (IOException e) {
            runeImage = null;
        }
    }

    /**
     * Initialize floor/wall images from sprite sheet.
     */
    private void initializeFloorWallImages() {
        try {
            URL url = getClass().getClassLoader().getResource(AssetPaths.SPRITESHEET.substring(1));
            if (url == null) throw new IOException();
            BufferedImage sheet = ImageIO.read(url);

            int[] floorCoords = AssetPaths.FLOOR_TILE;
            floorImage = sheet.getSubimage(floorCoords[0], floorCoords[1], floorCoords[2], floorCoords[3]);
            horizontalWallImage = sheet.getSubimage(17 * 16, 16, 16, 16);
            leftVerticalWallImage = sheet.getSubimage(16 * 16, 16, 16, 16);
            rightVerticalWallImage = sheet.getSubimage(18 * 16, 16, 16, 16);
        } catch (IOException e) {
            floorImage = fallback();
            horizontalWallImage = fallback();
            leftVerticalWallImage = fallback();
            rightVerticalWallImage = fallback();
        }
    }

    /**
     * Fallback image if sprite loading fails.
     */
    private BufferedImage fallback() {
        BufferedImage f = new BufferedImage(cellSize, cellSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = f.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, cellSize, cellSize);
        g2d.dispose();
        return f;
    }

    /**
     * Load pause/resume/exit images.
     */
    private void initializeButtonImages() {
        try {
            URL p = getClass().getClassLoader().getResource(AssetPaths.PAUSE_BUTTON.substring(1));
            URL r = getClass().getClassLoader().getResource(AssetPaths.RESUME_BUTTON.substring(1));
            URL e = getClass().getClassLoader().getResource(AssetPaths.EXIT_GAME.substring(1));
            if (p == null || r == null || e == null) throw new IOException();
            pauseButtonImage = ImageIO.read(p);
            resumeButtonImage = ImageIO.read(r);
            exitButtonImage = ImageIO.read(e);
        } catch (IOException ex) {
            pauseButtonImage = null;
            resumeButtonImage = null;
            exitButtonImage = null;
        }
    }

    /**
     * Create pause button.
     */
    private void createPauseButton() {
        pauseButton = new JButton();
        pauseButton.setBounds(850, 50, 64, 64);
        updatePauseButtonIcon(pauseButton);
        pauseButton.setBorderPainted(false);
        pauseButton.setFocusPainted(false);
        pauseButton.setContentAreaFilled(false);
        pauseButton.addActionListener(ev -> {
            if (!gameOver && !heroDied) {
                isPaused = !isPaused;
                updatePauseButtonIcon(pauseButton);
                if (isPaused) {
                    gameController.pauseGame();
                } else {
                    gameController.resumeGame();
                }
            }
        });
        setLayout(null);
        add(pauseButton);
    }

    private void updatePauseButtonIcon(JButton b) {
        BufferedImage i = isPaused ? resumeButtonImage : pauseButtonImage;
        if (i != null) {
            b.setIcon(new ImageIcon(i.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        }
    }

    /**
     * Create exit button.
     */
    private void createExitButton() {
        exitButton = new JButton();
        exitButton.setBounds(850, 120, 64, 64);
        exitButton.setBorderPainted(false);
        exitButton.setFocusPainted(false);
        exitButton.setContentAreaFilled(false);

        if (exitButtonImage != null) {
            exitButton.setIcon(new ImageIcon(exitButtonImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        }
        exitButton.addActionListener(e -> {
            JFrame mm = new RokueLikeMainMenu();
            mm.setVisible(true);
            SwingUtilities.getWindowAncestor(this).dispose();
        });
        add(exitButton);
    }

    /**
     * Hides pause/exit buttons if the game is over or hero died.
     */
    private void hideButtonsIfGameOver() {
        if (pauseButton != null) {
            pauseButton.setVisible(false);
        }
        if (exitButton != null) {
            exitButton.setVisible(false);
        }
    }

    // -------------------------------------------------------------------------
    // ADD THIS GETTER so that ArcherMonster can check if cloak is active
    // -------------------------------------------------------------------------
    public boolean isCloakActive() {
        return cloakActive;
    }
}
