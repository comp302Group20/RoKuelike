package UI;

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
import java.util.Timer;

public class GamePanel extends JPanel {
    // References to the "final" layout from BuildModePanel
    private CellType[][] grid;                             // For walls/floors
    private BuildModePanel.PlacedObject[][] placedObjects; // For placed objects

    // Grid dimensions (matches BuildModePanel)
    private static final int GRID_ROWS = 13;
    private static final int GRID_COLS = 13;
    private int cellSize = 64;

    // Floor/wall images
    private BufferedImage floorImage;
    private BufferedImage horizontalWallImage;
    private BufferedImage leftVerticalWallImage;
    private BufferedImage rightVerticalWallImage;

    // Hero, monsters, etc.
    private BufferedImage heroImage;
    private BufferedImage pauseButtonImage;
    private BufferedImage resumeButtonImage;
    private BufferedImage exitButtonImage;
    private List<BufferedImage> monsterImages;
    private Point heroPosition;
    private List<Point> monsterPositions;
    private List<Integer> spawnedMonsterTypes;
    private Random random;
    private Timer monsterSpawnerTimer;
    private Timer monsterMovementTimer;
    private boolean isPaused = false;

    // KEY-related
    private BuildModePanel.PlacedObject keyObject; // the object that has the key
    private BufferedImage keyImage;                // loaded from (10,10,16,16)
    private boolean foundKey = false;              // whether the key is revealed

    /**
     * New constructor that takes the BuildModePanel's grid data.
     */
    public GamePanel(CellType[][] grid, BuildModePanel.PlacedObject[][] placedObjects) {
        this.grid = grid;
        this.placedObjects = placedObjects;

        setBackground(new Color(75, 30, 30));
        // Hero starts at row=2, col=2 (inside the walls)
        this.heroPosition = new Point(2 * cellSize, 2 * cellSize);
        this.monsterPositions = new ArrayList<>();
        this.spawnedMonsterTypes = new ArrayList<>();
        this.monsterImages = new ArrayList<>();
        this.random = new Random();

        // Load hero, monster images, pause/resume buttons, etc.
        initializeHeroImage();
        initializeMonsterImages();
        initializeButtonImages();

        // Load the same floor/wall images used in BuildModePanel
        initializeFloorWallImages();

        // Load the key image from x=10, y=10, width=16, height=16
        initializeKeyImage();

        // Randomly choose one placed object (if any) to hold the key
        randomlyAssignKeyObject();

        // Start timers
        startMonsterSpawner();
        startMonsterMovement();

        // Create pause/exit buttons
        initializePauseButton();
        initializeExitButton();

        // Mouse to "click" objects and potentially reveal the key
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isPaused) return;
                if (SwingUtilities.isLeftMouseButton(e)) {
                    handleObjectClick(e.getX(), e.getY());
                }
            }
        });

        setFocusable(true);
        requestFocusInWindow();

        // Keyboard control
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isPaused) return;

                // Attempt to move hero by 64 pixels in one direction
                int step = cellSize;
                Point newHeroPosition = new Point(heroPosition);
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        newHeroPosition.translate(-step, 0);
                        break;
                    case KeyEvent.VK_RIGHT:
                        newHeroPosition.translate(step, 0);
                        break;
                    case KeyEvent.VK_UP:
                        newHeroPosition.translate(0, -step);
                        break;
                    case KeyEvent.VK_DOWN:
                        newHeroPosition.translate(0, step);
                        break;
                }

                // If the target cell is free (not wall, not object) and no monsters
                if (isCellFree(newHeroPosition) && !isCollisionWithMonsters(newHeroPosition)) {
                    heroPosition.setLocation(newHeroPosition);
                }
                repaint();
            }
        });
    }

    /**
     * Load the key image from the sprite sheet at (10,10,16,16).
     */
    private void initializeKeyImage() {
        try {
            URL resourceUrl = getClass().getClassLoader().getResource(AssetPaths.SPRITESHEET_2.substring(1));
            if (resourceUrl == null) {
                throw new IOException("Sprite sheet not found: " + AssetPaths.SPRITESHEET_2);
            }
            BufferedImage spriteSheet = ImageIO.read(resourceUrl);

            keyImage = spriteSheet.getSubimage(13*16, 11*16, 16, 16);

        } catch (IOException e) {
            System.err.println("Error loading key image: " + e.getMessage());
            // fallback
            keyImage = createFallbackImage();
        }
    }

    /**
     * Randomly pick one placed object (if any) as the key holder.
     */
    private void randomlyAssignKeyObject() {
        List<BuildModePanel.PlacedObject> objectList = new ArrayList<>();
        // Gather all placed objects
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                BuildModePanel.PlacedObject obj = placedObjects[r][c];
                if (obj != null) {
                    objectList.add(obj);
                }
            }
        }

        if (!objectList.isEmpty()) {
            // pick a random object to hold the key
            int idx = random.nextInt(objectList.size());
            keyObject = objectList.get(idx);
        } else {
            keyObject = null;
        }
    }

    /**
     * Checks if a pixel-based position is inside the grid, not a wall, and not occupied by a placed object.
     */
    private boolean isCellFree(Point p) {
        int col = p.x / cellSize;
        int row = p.y / cellSize;

        // 1) Check bounds
        if (row < 0 || row >= GRID_ROWS || col < 0 || col >= GRID_COLS) {
            return false;
        }
        // 2) Check if it's a wall
        if (grid[row][col] == CellType.WALL) {
            return false;
        }
        // 3) Check if there's a placed object in that cell
        if (placedObjects[row][col] != null) {
            return false;
        }

        return true;
    }

    /**
     * Called when the user clicks in the game panel.
     * We see if the user clicked on a cell that has an object.
     * If that object = keyObject, and hero is 1 cell away (orthogonally), reveal the key.
     */
    private void handleObjectClick(int mouseX, int mouseY) {
        // Convert click to cell
        if (mouseX < 0 || mouseX >= GRID_COLS * cellSize
                || mouseY < 0 || mouseY >= GRID_ROWS * cellSize) {
            return;
        }
        int clickCol = mouseX / cellSize;
        int clickRow = mouseY / cellSize;

        // If there's no object here, do nothing
        BuildModePanel.PlacedObject clickedObj = placedObjects[clickRow][clickCol];
        if (clickedObj == null) {
            return;
        }

        // Is this the key object?
        if (clickedObj == keyObject && !foundKey) {
            // Check if hero is 1 cell away (orthogonally)
            int heroRow = heroPosition.y / cellSize;
            int heroCol = heroPosition.x / cellSize;

            // difference in row + difference in col = 1 => adjacent orth
            int rowDiff = Math.abs(heroRow - clickRow);
            int colDiff = Math.abs(heroCol - clickCol);

            if ((rowDiff + colDiff) == 1) {
                // Hero is adjacent => reveal the key
                foundKey = true;
                repaint();
            }
        }
    }

    /**
     * Load the same floor/wall sub-images that BuildModePanel uses.
     */
    private void initializeFloorWallImages() {
        try {
            URL resourceUrl = getClass().getClassLoader().getResource(AssetPaths.SPRITESHEET.substring(1));
            if (resourceUrl == null) {
                throw new IOException("Sprite sheet not found: " + AssetPaths.SPRITESHEET);
            }

            BufferedImage spriteSheet = ImageIO.read(resourceUrl);

            // Floor
            int[] floorCoords = AssetPaths.FLOOR_TILE;
            floorImage = spriteSheet.getSubimage(
                    floorCoords[0], floorCoords[1],
                    floorCoords[2], floorCoords[3]
            );

            // Walls
            horizontalWallImage = spriteSheet.getSubimage(17 * 16, 16, 16, 16);
            leftVerticalWallImage = spriteSheet.getSubimage(16 * 16, 16, 16, 16);
            rightVerticalWallImage = spriteSheet.getSubimage(18 * 16, 16, 16, 16);

        } catch (IOException e) {
            System.err.println("Error loading floor/wall images: " + e.getMessage());
            e.printStackTrace();
            // Fallback images if loading fails
            floorImage = createFallbackImage();
            horizontalWallImage = createFallbackImage();
            leftVerticalWallImage = createFallbackImage();
            rightVerticalWallImage = createFallbackImage();
        }
    }

    private void initializeHeroImage() {
        try {
            URL resourceUrl = getClass().getClassLoader().getResource(AssetPaths.HERO.substring(1));
            if (resourceUrl == null) {
                throw new IOException("Hero image resource not found: " + AssetPaths.HERO);
            }
            heroImage = ImageIO.read(resourceUrl);
            if (heroImage == null) {
                throw new IOException("Failed to load hero image: " + AssetPaths.HERO);
            }
        } catch (IOException e) {
            System.err.println("Error loading hero image: " + e.getMessage());
            heroImage = createFallbackImage();
        }
    }

    private void initializeMonsterImages() {
        monsterImages = new ArrayList<>();
        String[] monsterPaths = {AssetPaths.ARCHER, AssetPaths.FIGHTER, AssetPaths.WIZARD};
        for (String path : monsterPaths) {
            try {
                URL resourceUrl = getClass().getClassLoader().getResource(path.substring(1));
                if (resourceUrl == null) {
                    throw new IOException("Monster image resource not found: " + path);
                }
                BufferedImage monsterImage = ImageIO.read(resourceUrl);
                if (monsterImage == null) {
                    throw new IOException("Failed to load monster image: " + path);
                }
                monsterImages.add(monsterImage);
            } catch (IOException e) {
                System.err.println("Error loading monster image: " + e.getMessage());
                monsterImages.add(createFallbackImage());
            }
        }
    }

    private void initializeButtonImages() {
        try {
            URL pauseUrl = getClass().getClassLoader().getResource(AssetPaths.PAUSE_BUTTON.substring(1));
            URL resumeUrl = getClass().getClassLoader().getResource(AssetPaths.RESUME_BUTTON.substring(1));
            URL exitUrl = getClass().getClassLoader().getResource(AssetPaths.EXIT_GAME.substring(1));

            if (pauseUrl == null || resumeUrl == null || exitUrl == null) {
                throw new IOException("Pause/Resume/Exit button images not found.");
            }
            pauseButtonImage = ImageIO.read(pauseUrl);
            resumeButtonImage = ImageIO.read(resumeUrl);
            exitButtonImage = ImageIO.read(exitUrl);
        } catch (IOException e) {
            System.err.println("Error loading button images: " + e.getMessage());
        }
    }

    private BufferedImage createFallbackImage() {
        BufferedImage fallback = new BufferedImage(cellSize, cellSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = fallback.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, cellSize, cellSize);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, cellSize - 1, cellSize - 1);
        g2d.dispose();
        return fallback;
    }

    /**
     * Spawn a monster in a free cell (not outside, not a wall, not an object, not on the hero, not on another monster).
     */
    private void startMonsterSpawner() {
        monsterSpawnerTimer = new Timer(true);
        monsterSpawnerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isPaused) {
                    spawnMonster();
                    repaint();
                }
            }
        }, 0, 8000); // spawn a monster every 8 seconds
    }

    private void spawnMonster() {
        // Try up to 50 times to find a free cell
        int attempts = 0;
        Point randomPosition = null;
        while (attempts < 50) {
            int randomX = random.nextInt(GRID_COLS) * cellSize;
            int randomY = random.nextInt(GRID_ROWS) * cellSize;
            Point candidatePos = new Point(randomX, randomY);

            // Check if the cell is free AND not already occupied by a monster or the hero
            if (isCellFree(candidatePos)
                    && !monsterPositions.contains(candidatePos)
                    && !candidatePos.equals(heroPosition)) {
                randomPosition = candidatePos;
                break;
            }
            attempts++;
        }

        // If found a free cell, add monster
        if (randomPosition != null) {
            int monsterType = random.nextInt(monsterImages.size());
            monsterPositions.add(randomPosition);
            spawnedMonsterTypes.add(monsterType);
        }
    }

    /**
     * Move each monster within the grid, ensuring it cannot pass walls/objects, go outside, or step onto hero.
     */
    private void startMonsterMovement() {
        monsterMovementTimer = new Timer(true);
        monsterMovementTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isPaused) {
                    moveMonsters();
                    repaint();
                }
            }
        }, 0, 500);
    }

    private void moveMonsters() {
        for (int i = 0; i < monsterPositions.size(); i++) {
            Point monsterPos = monsterPositions.get(i);

            // We'll try up to 10 random directions; if none is valid, monster won't move
            boolean moved = false;
            for (int attempt = 0; attempt < 10; attempt++) {
                Point newMonsterPosition = new Point(monsterPos);
                int direction = random.nextInt(4);
                switch (direction) {
                    case 0: // Move up
                        newMonsterPosition.translate(0, -cellSize);
                        break;
                    case 1: // Move down
                        newMonsterPosition.translate(0, cellSize);
                        break;
                    case 2: // Move left
                        newMonsterPosition.translate(-cellSize, 0);
                        break;
                    case 3: // Move right
                        newMonsterPosition.translate(cellSize, 0);
                        break;
                }

                // Check collisions with walls, objects, hero, or other monsters
                if (isCellFree(newMonsterPosition)
                        && !newMonsterPosition.equals(heroPosition)
                        && !monsterPositions.contains(newMonsterPosition)) {
                    // Valid move => update monster position
                    monsterPositions.set(i, newMonsterPosition);
                    moved = true;
                    break;
                }
            }
        }
    }

    /**
     * Check if a given pixel-based position is already occupied by a monster.
     */
    private boolean isCollisionWithMonsters(Point position) {
        return monsterPositions.contains(position);
    }

    private void initializePauseButton() {
        JButton pauseButton = new JButton();
        pauseButton.setBounds(850, 50, 64, 64);
        updatePauseButtonImage(pauseButton);
        pauseButton.setBorderPainted(false);
        pauseButton.setFocusPainted(false);
        pauseButton.setContentAreaFilled(false);
        pauseButton.addActionListener(e -> {
            isPaused = !isPaused;
            requestFocusInWindow(); // Refocus to ensure key events work
            updatePauseButtonImage(pauseButton);
        });
        setLayout(null);
        add(pauseButton);
    }

    private void updatePauseButtonImage(JButton button) {
        BufferedImage buttonImage = isPaused ? resumeButtonImage : pauseButtonImage;
        if (buttonImage != null) {
            button.setIcon(new ImageIcon(buttonImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        }
    }

    private void initializeExitButton() {
        JButton exitButton = new JButton();
        exitButton.setBounds(850, 120, 64, 64);
        exitButton.setBorderPainted(false);
        exitButton.setFocusPainted(false);
        exitButton.setContentAreaFilled(false);

        if (exitButtonImage != null) {
            exitButton.setIcon(new ImageIcon(exitButtonImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        }

        exitButton.addActionListener(e -> {
            // Return to main menu or close the panel
            JFrame mainMenu = new RokueLikeMainMenu();
            mainMenu.setVisible(true);
            SwingUtilities.getWindowAncestor(GamePanel.this).dispose();
        });

        setLayout(null);
        add(exitButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // 1) Draw the floor and walls from the BuildMode grid
        drawBoard(g);

        // 2) (Optional) faint grid lines
        drawGridLines(g);

        // 3) Draw the placed objects from BuildMode
        drawPlacedObjects(g);

        // If the key was found, we overlay the key on top of the object’s cell
        drawKeyIfFound(g);

        // 4) Draw hero
        g.drawImage(heroImage, heroPosition.x, heroPosition.y, cellSize, cellSize, null);

        // 5) Draw monsters
        for (int i = 0; i < monsterPositions.size(); i++) {
            Point monsterPos = monsterPositions.get(i);
            BufferedImage monsterImage = monsterImages.get(spawnedMonsterTypes.get(i));
            g.drawImage(monsterImage, monsterPos.x, monsterPos.y, cellSize, cellSize, null);
        }
    }

    /**
     * If foundKey == true, we draw the key icon in the cell of keyObject.
     */
    private void drawKeyIfFound(Graphics g) {
        if (!foundKey || keyObject == null) {
            return;
        }
        int row = keyObject.gridRow;
        int col = keyObject.gridCol;
        int x = col * cellSize;
        int y = row * cellSize;

        // If the object is double-height, it might be drawn upwards, but let's just show the key at the cell's floor
        g.drawImage(keyImage, x, y, cellSize, cellSize, null);
    }

    private void drawBoard(Graphics g) {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                int x = c * cellSize;
                int y = r * cellSize;

                // Always draw floor
                g.drawImage(floorImage, x, y, cellSize, cellSize, null);

                // If it's a wall, overlay the wall image
                if (grid[r][c] == CellType.WALL) {
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

    private void drawGridLines(Graphics g) {
        g.setColor(new Color(75, 30, 30, 50));
        for (int x = 0; x <= GRID_COLS; x++) {
            g.drawLine(x * cellSize, 0, x * cellSize, GRID_ROWS * cellSize);
        }
        for (int y = 0; y <= GRID_ROWS; y++) {
            g.drawLine(0, y * cellSize, GRID_COLS * cellSize, y * cellSize);
        }
    }

    private void drawPlacedObjects(Graphics g) {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                BuildModePanel.PlacedObject obj = placedObjects[r][c];
                if (obj != null) {
                    int drawX = c * cellSize;
                    int drawY = r * cellSize;
                    int drawW = cellSize;
                    int drawH = obj.isDouble ? (cellSize * 2) : cellSize;

                    // Shift up if double-height
                    if (obj.isDouble) {
                        drawY -= (drawH - cellSize);
                    }

                    g.drawImage(obj.image, drawX, drawY, drawW, drawH, null);
                }
            }
        }
    }

    public static void main(String[] args) {
        // Example test for standalone usage
        JFrame frame = new JFrame("Game Panel Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 900);
        frame.setLocationRelativeTo(null);

        // Create dummy data for testing
        CellType[][] testGrid = new CellType[GRID_ROWS][GRID_COLS];
        BuildModePanel.PlacedObject[][] testObjects = new BuildModePanel.PlacedObject[GRID_ROWS][GRID_COLS];

        // Fill testGrid with all FLOOR except outer walls
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                testGrid[r][c] = CellType.FLOOR;
            }
        }
        // Outer walls
        for (int r = 1; r <= 11; r++) {
            testGrid[r][0] = CellType.WALL;
            testGrid[r][GRID_COLS - 1] = CellType.WALL;
        }
        for (int c = 1; c <= 11; c++) {
            testGrid[1][c] = CellType.WALL;
            testGrid[11][c] = CellType.WALL;
        }

        // e.g. create one dummy object at (3,3)
        BufferedImage dummyImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = dummyImg.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, 16, 16);
        g2d.dispose();
        BuildModePanel.PlacedObject someObject = new BuildModePanel.PlacedObject(dummyImg, 3, 3, false);
        testObjects[3][3] = someObject;

        GamePanel gamePanel = new GamePanel(testGrid, testObjects);
        frame.add(gamePanel);

        frame.setVisible(true);
    }
}
