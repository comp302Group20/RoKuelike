package UI;

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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
/**
 * The main panel for playing the game. Handles rendering, user input,
 * monster spawning/movement, and game-over conditions.
 */
public class GamePanel extends JPanel {

    /**
     * The grid representing the hall layout (FLOOR vs WALL).
     */
    private BuildModePanel.CellType[][] grid;

    /**
     * The 2D array of placed objects (chests, boxes, etc.).
     */
    private PlacedObject[][] placedObjects;

    /**
     * Number of rows in the grid.
     */
    private static final int GRID_ROWS = 13;

    /**
     * Number of columns in the grid.
     */
    private static final int GRID_COLS = 13;

    /**
     * The size (in pixels) of each grid cell.
     */
    private int cellSize = 64;

    /**
     * Images for floor and walls.
     */
    private BufferedImage floorImage;
    private BufferedImage horizontalWallImage;
    private BufferedImage leftVerticalWallImage;
    private BufferedImage rightVerticalWallImage;

    /**
     * Images for the pause/resume/exit buttons, the rune, door, game-over screen, died hero, and hearts.
     */
    private BufferedImage pauseButtonImage;
    private BufferedImage resumeButtonImage;
    private BufferedImage exitButtonImage;
    private BufferedImage runeImage;
    private BufferedImage doorImage;
    private BufferedImage gameOverImage;
    private BufferedImage diedHeroImage; // New image for died hero
    private BufferedImage heartImage;

    /**
     * The list of monsters on the board.
     */
    private List<Monster> monsters;

    /**
     * The hero controlled by the player.
     */
    private Hero hero;

    /**
     * For random operations (monster spawn location, etc.).
     */
    private Random random;

    /**
     * Timers for spawning and moving monsters.
     */
    private Timer monsterSpawnerTimer;
    private Timer monsterMovementTimer;
    private Timer gameOverTimer; // Timer for game over transition

    /**
     * True if the game is paused; false otherwise.
     */
    private boolean isPaused = false;

    /**
     * True if the game is over; false otherwise.
     */
    private boolean gameOver = false;

    /**
     * True if the hero has died and is in the 2-second death animation.
     */
    private boolean heroDied = false;

    /**
     * The row and column where the door is placed.
     */
    private static final int DOOR_ROW = 11;
    private static final int DOOR_COL = 6;

    /**
     * References to the pause and exit buttons so we can hide them at game-over.
     */
    private JButton pauseButton;
    private JButton exitButton;

    /**
     * Constructs a GamePanel with the given grid and placed objects.
     * @param g the grid of CellTypes (FLOOR or WALL)
     * @param p the array of placed objects
     */
    public GamePanel(BuildModePanel.CellType[][] g, PlacedObject[][] p) {
        grid = g;
        placedObjects = p;
        setBackground(Color.BLACK);
        hero = new Hero(2 * cellSize, 2 * cellSize, cellSize, cellSize);
        monsters = new ArrayList<>();
        random = new Random();

        hideRuneInRandomObject();
        loadDoorImage();
        placeDoorAsObject();
        loadGameOverImage();
        loadDiedHeroImage(); // Load DIED_HERO image
        loadHeartImage();
        initializeFloorWallImages();
        initializeButtonImages();
        loadRuneImage();

        createPauseButton();
        createExitButton();

        startMonsterSpawner();
        startMonsterMovement();

        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isPaused || gameOver || heroDied) return; // Disable movement if paused, game over, or hero died
                int step = cellSize;
                int dx = 0;
                int dy = 0;
                if (e.getKeyCode() == KeyEvent.VK_LEFT)  dx = -step;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) dx =  step;
                if (e.getKeyCode() == KeyEvent.VK_UP)    dy = -step;
                if (e.getKeyCode() == KeyEvent.VK_DOWN)  dy =  step;

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
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isPaused || gameOver || heroDied) return; // Disable actions if paused, game over, or hero died
                if (e.getButton() != MouseEvent.BUTTON1) return;

                int mx = e.getX();
                int my = e.getY();
                PlacedObject obj = getClickedObject(mx, my);
                if (obj != null && obj.hasRune) {
                    int hr = hero.getY() / cellSize;
                    int hc = hero.getX() / cellSize;
                    if (Math.abs(hr - obj.gridRow) + Math.abs(hc - obj.gridCol) == 1) {
                        obj.runeVisible = true;
                        System.out.println("Rune discovered");
                    }
                }
            }
        });
    }

    /**
     * Loads the DIED_HERO image from AssetPaths.
     */

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

    private void loadDiedHeroImage() {
        try {
            URL diedHeroUrl = getClass().getClassLoader().getResource(AssetPaths.DIED_HERO.substring(1));
            if (diedHeroUrl != null) {
                BufferedImage originalImage = ImageIO.read(diedHeroUrl);
                if (!hero.isFacingLeft()) { // Mirror when facing right
                    diedHeroImage = mirrorImage(originalImage); // Mirror for right-facing
                } else {
                    diedHeroImage = originalImage; // Original for left-facing
                }
            }
        } catch (IOException e) {
            diedHeroImage = null;
            e.printStackTrace(); // Optional: Log the exception for debugging
        }
    }



    /**
     * Loads the door image from AssetPaths.
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
     * Places the door as a PlacedObject at DOOR_ROW, DOOR_COL if we have a door image.
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

    /**
     * Converts a general Image to a BufferedImage for consistent usage.
     * @param img the Image to convert
     * @return a BufferedImage version of img
     */
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
     * Loads the heart image from AssetPaths.
     */
    private void loadHeartImage() {
        try {
            URL heartUrl = getClass().getClassLoader().getResource(AssetPaths.HEART.substring(1));
            if (heartUrl == null) throw new IOException();
            heartImage = ImageIO.read(heartUrl);
        } catch (IOException e) {
            heartImage = null;
        }
    }

    /**
     * Randomly hides the rune in one of the placed objects.
     */
    private void hideRuneInRandomObject() {
        List<BuildModePanel.PlacedObject> objs = new ArrayList<>();
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
     * Teleports the rune from its current object to another random object, if the wizard monster triggers it.
     */
    public void teleportRuneRandomly() {
        if (gameOver) return;
        List<BuildModePanel.PlacedObject> allObjects = new ArrayList<>();
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                BuildModePanel.PlacedObject obj = placedObjects[r][c];
                if (obj != null) {
                    allObjects.add(obj);
                }
            }
        }
        BuildModePanel.PlacedObject runeHolder = null;
        for (BuildModePanel.PlacedObject po : allObjects) {
            if (po.hasRune) {
                runeHolder = po;
                break;
            }
        }
        if (runeHolder == null) return;
        runeHolder.hasRune = false;
        runeHolder.runeVisible = false;
        if (!allObjects.isEmpty()) {
            int idx = random.nextInt(allObjects.size());
            allObjects.get(idx).hasRune = true;
        }
        repaint();
    }

    /**
     * Loads the game-over image from AssetPaths.
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
     * Loads the rune image from AssetPaths.
     */
    private void loadRuneImage() {
        try {
            URL r = getClass().getClassLoader().getResource(AssetPaths.RUNE.substring(1));
            if (r == null) throw new IOException();
            runeImage = ImageIO.read(r);
        } catch (IOException e) {
            runeImage = null;
        }
    }

    /**
     * Determines which placed object (if any) was clicked at (mx, my).
     * @param mx the mouse x coordinate
     * @param my the mouse y coordinate
     * @return the PlacedObject clicked, or null if none
     */
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
     * Spawns a monster at a random valid location, if possible.
     */
    private void spawnMonster() {
        int tries = 0;
        while (tries < 50) {
            int c = random.nextInt(GRID_COLS - 2) + 1;
            int r = random.nextInt(GRID_ROWS - 2) + 1;
            Point pt = new Point(c * cellSize, r * cellSize);
            if (canMonsterMove(null, pt.x, pt.y) && (pt.x != hero.getX() || pt.y != hero.getY())) {
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
     * Schedules regular spawning of monsters.
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
     * Schedules regular monster movement.
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

                    // **Add this line to check health after monsters move**
                    checkHealthCondition();

                    repaint();
                }
            }
        }, 0, 500);
    }

    /**
     * Determines if a monster can move to the given pixel location.
     * @param monster the monster attempting to move (null if spawning)
     * @param nx the candidate x coordinate in pixels
     * @param ny the candidate y coordinate in pixels
     * @return true if the monster can move there; false otherwise
     */
    public boolean canMonsterMove(Monster monster, int nx, int ny) {
        int c = nx / cellSize;
        int r = ny / cellSize;
        if (r < 0 || r >= GRID_ROWS || c < 0 || c >= GRID_COLS) return false;
        if (grid[r][c] == BuildModePanel.CellType.WALL) return false;
        PlacedObject po = placedObjects[r][c];
        if (po != null && po != placedObjects[DOOR_ROW][DOOR_COL]) return false;
        for (Monster mm : monsters) {
            if (mm != monster && mm.getX() == nx && mm.getY() == ny) {
                return false;
            }
        }
        if (hero.getX() == nx && hero.getY() == ny) return false;
        return true;
    }

    /**
     * Determines if the hero can move to the given pixel location.
     * @param p the candidate location in pixels
     * @return true if the hero can move there; false otherwise
     */
    private boolean canHeroMove(Point p) {
        int c = p.x / cellSize;
        int r = p.y / cellSize;
        if (r < 0 || r >= GRID_ROWS || c < 0 || c >= GRID_COLS) return false;
        if (grid[r][c] == BuildModePanel.CellType.WALL) return false;
        PlacedObject po = placedObjects[r][c];
        if (po != null && po != placedObjects[DOOR_ROW][DOOR_COL]) return false;
        for (Monster mm : monsters) {
            if (mm.getX() == p.x && mm.getY() == p.y) {
                return false;
            }
        }
        return true;
    }

    /**
     * Highlights archer-monster zones with a yellow overlay.
     * @param g the Graphics context
     */
    private void highlightArcherZones(Graphics g) {
        boolean[][] coverage = new boolean[GRID_ROWS][GRID_COLS];
        for (Monster m : monsters) {
            if (m instanceof ArcherMonster) {
                int startR = m.getY() / 64;
                int startC = m.getX() / 64;
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
                                    queue.add(new int[]{nr,nc,dist+1});
                                }
                            }
                        }
                    }
                }
            }
        }

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
     * Paints the game scene.
     * @param g the Graphics context
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (gameOver && gameOverImage != null) {
            hideButtonsIfGameOver();
            g.drawImage(gameOverImage, 0, 0, getWidth(), getHeight(), null);
            return;
        }

        if (heroDied && diedHeroImage != null) {
            hideButtonsIfGameOver();
            g.drawImage(diedHeroImage, hero.getX(), hero.getY(), cellSize, cellSize, null);
            return;
        }

        drawBoard(g);
        drawGridLines(g);
        drawPlacedObjects(g);

        hero.draw(g);

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

        drawObjectsAboveHero(g);
        drawHearts(g);
    }

    /**
     * Draws the hearts representing the hero's health.
     * @param g the Graphics context
     */
    private void drawHearts(Graphics g) {
        if (heartImage == null) {
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("Health: " + hero.getHealth(), 10, 20);
            return;
        }
        int heartWidth = 96;
        int heartHeight = 96;
        int health = hero.getHealth();
        for (int i = 0; i < 3; i++) {
            Graphics2D g2d = (Graphics2D) g.create();
            int xPos = 10 + (i * (heartWidth));
            int yPos = -20;
            if (health > i) {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            } else {
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f));
            }
            g2d.drawImage(heartImage, xPos, yPos, heartWidth, heartHeight, null);
            g2d.dispose();
        }
    }

    /**
     * Checks if the hero has reached the door row & col and discovered the rune; ends the game if so.
     */
    private void checkDoorCondition() {
        int hr = hero.getY() / cellSize;
        int hc = hero.getX() / cellSize;
        if (hr == DOOR_ROW-1 && hc == DOOR_COL) {
            if (heroHasRune()) {
                gameOver = true;
                System.out.println("Hero escaped with the rune");

                // **Stop all timers to prevent further game actions**
                if (monsterSpawnerTimer != null) {
                    monsterSpawnerTimer.cancel();
                }
                if (monsterMovementTimer != null) {
                    monsterMovementTimer.cancel();
                }

                // **Repaint immediately to show the game-over screen**
                SwingUtilities.invokeLater(() -> repaint());
            }
        }
    }

    /**
     * Determines if the hero currently holds (or discovered) the rune.
     * @return true if hero discovered the rune; false otherwise
     */
    private boolean heroHasRune() {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                BuildModePanel.PlacedObject po = placedObjects[r][c];
                if (po != null && po.hasRune && po.runeVisible) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the hero's health has dropped to 0; triggers hero death if so.
     */
    private void checkHealthCondition() {
        if (hero.getHealth() <= 0 && !heroDied && !gameOver) {
            heroDied = true;
            loadDiedHeroImage(); // Load diedHeroImage based on current direction
            isPaused = true; // Disable movement
            System.out.println("Hero died");

            // Cancel existing timers to stop monster actions
            if (monsterSpawnerTimer != null) {
                monsterSpawnerTimer.cancel();
            }
            if (monsterMovementTimer != null) {
                monsterMovementTimer.cancel();
            }

            // Start a 2-second timer to transition to game over
            gameOverTimer = new Timer(true);
            gameOverTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    gameOver = true;
                    heroDied = false; // Reset heroDied flag
                    SwingUtilities.invokeLater(() -> repaint());
                }
            }, 2000); // 2000 milliseconds = 2 seconds

            // Repaint to show the died hero image immediately
            SwingUtilities.invokeLater(() -> repaint());
        }
    }


    /**
     * Draws the floor/wall tiles.
     * @param g the Graphics context
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
     * Draws faint grid lines over the board.
     * @param g the Graphics context
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
     * Draws all placed objects (boxes, chests, etc.), including the rune if discovered.
     * @param g the Graphics context
     */
    private void drawPlacedObjects(Graphics g) {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                BuildModePanel.PlacedObject obj = placedObjects[r][c];
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
     * Determines if a monster/hero is behind a double-height object at the same cell.
     * @param entity the monster or hero
     * @return true if the entity is behind a double-height object
     */
    private boolean isCoveredByObject(Object entity) {
        int ex = entity instanceof Monster ? ((Monster) entity).getX() : hero.getX();
        int ey = entity instanceof Monster ? ((Monster) entity).getY() : hero.getY();
        int ec = ex / cellSize;
        int er = ey / cellSize;
        if (er < 0 || er >= GRID_ROWS || ec < 0 || ec >= GRID_COLS) return false;
        BuildModePanel.PlacedObject obj = placedObjects[er][ec];
        return obj != null && obj.isDouble;
    }

    /**
     * Draws double-height objects above the hero/monsters for a pseudo-3D effect.
     * @param g the Graphics context
     */
    private void drawObjectsAboveHero(Graphics g) {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                BuildModePanel.PlacedObject obj = placedObjects[r][c];
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
     * Loads floor/wall tile images from the sprite sheet.
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
     * Creates a fallback red-tile image if loading the sprite sheet fails.
     * @return the fallback BufferedImage
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
     * Loads the pause/resume/exit button images from AssetPaths.
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
        }
    }

    /**
     * Creates the pause button and adds it to the panel.
     */
    private void createPauseButton() {
        pauseButton = new JButton();
        pauseButton.setBounds(850, 50, 64, 64);
        updatePauseButtonIcon(pauseButton);
        pauseButton.setBorderPainted(false);
        pauseButton.setFocusPainted(false);
        pauseButton.setContentAreaFilled(false);
        pauseButton.addActionListener(ev -> {
            if (!gameOver && !heroDied) { // Allow pausing only if not game over or hero died
                isPaused = !isPaused;
                updatePauseButtonIcon(pauseButton);
            }
        });
        setLayout(null);
        add(pauseButton);
    }

    /**
     * Updates the icon of the pause button, toggling between pause/resume images.
     * @param b the pause button
     */
    private void updatePauseButtonIcon(JButton b) {
        BufferedImage i = isPaused ? resumeButtonImage : pauseButtonImage;
        if (i != null) {
            b.setIcon(new ImageIcon(i.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        }
    }

    /**
     * Creates the exit button and adds it to the panel.
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
     * Hides the pause and exit buttons if the game is over or the hero has died.
     */
    private void hideButtonsIfGameOver() {
        if (pauseButton != null) {
            pauseButton.setVisible(false);
        }
        if (exitButton != null) {
            exitButton.setVisible(false);
        }
    }
}
