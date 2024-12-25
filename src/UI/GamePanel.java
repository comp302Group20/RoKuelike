package UI;

import Domain.Hero;
import Domain.Monster;
import Domain.ArcherMonster;
import Domain.FighterMonster;
import Domain.WizardMonster;

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

/**
 * Play Mode panel that uses the final layout from BuildModePanel and spawns
 * the hero & monsters. Now it no longer relies on monster image paths from elsewhere.
 */
public class GamePanel extends JPanel {

    private BuildModePanel.CellType[][] grid;
    private BuildModePanel.PlacedObject[][] placedObjects;

    private static final int GRID_ROWS = 13;
    private static final int GRID_COLS = 13;
    private int cellSize = 64;

    // Floor/wall images
    private BufferedImage floorImage;
    private BufferedImage horizontalWallImage;
    private BufferedImage leftVerticalWallImage;
    private BufferedImage rightVerticalWallImage;

    // Button images
    private BufferedImage pauseButtonImage;
    private BufferedImage resumeButtonImage;
    private BufferedImage exitButtonImage;

    // Monsters
    private List<Monster> monsters;

    // Hero
    private Hero hero;
    private Random random;

    // Timers
    private Timer monsterSpawnerTimer;
    private Timer monsterMovementTimer;

    // Pause
    private boolean isPaused = false;

    public GamePanel(BuildModePanel.CellType[][] grid, BuildModePanel.PlacedObject[][] placedObjects) {
        this.grid = grid;
        this.placedObjects = placedObjects;

        setBackground(new Color(75, 30, 30));

        // Example hero position at row=2, col=2
        hero = new Hero(2 * cellSize, 2 * cellSize, cellSize, cellSize);


        monsters = new ArrayList<>();
        random = new Random();

        initializeFloorWallImages();
        initializeButtonImages();
        createPauseButton();
        createExitButton();

        startMonsterSpawner();
        startMonsterMovement();

        // Keyboard input for hero movement
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isPaused) return;

                int step = cellSize;
                int dx = 0, dy = 0;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:  dx = -step; break;
                    case KeyEvent.VK_RIGHT: dx =  step; break;
                    case KeyEvent.VK_UP:    dy = -step; break;
                    case KeyEvent.VK_DOWN:  dy =  step; break;
                }

                int oldX = hero.getX();
                int oldY = hero.getY();

                hero.move(dx, dy);

                // If we collided with a wall/object/monster, revert
                Point newPos = new Point(hero.getX(), hero.getY());
                if (!isCellFree(newPos) || isCollisionWithMonsters(newPos)) {
                    hero.setPosition(oldX, oldY);
                }

                repaint();
            }
        });
    }

    private void initializeFloorWallImages() {
        try {
            // Adjust the path to your sprite sheet as needed
            URL resourceUrl = getClass().getClassLoader().getResource("resources/Assets/spritesheet.png");
            if (resourceUrl == null) {
                throw new IOException("Sprite sheet not found: resources/Assets/spritesheet.png");
            }
            BufferedImage spriteSheet = ImageIO.read(resourceUrl);

            // Example floor tile: (32, 48, 16, 16)
            floorImage = spriteSheet.getSubimage(32, 48, 16, 16);
            horizontalWallImage = spriteSheet.getSubimage(17 * 16, 16, 16, 16);
            leftVerticalWallImage = spriteSheet.getSubimage(16 * 16, 16, 16, 16);
            rightVerticalWallImage = spriteSheet.getSubimage(18 * 16, 16, 16, 16);

        } catch (IOException e) {
            System.err.println("Error loading floor/wall images: " + e.getMessage());
            floorImage = createFallbackImage();
            horizontalWallImage = createFallbackImage();
            leftVerticalWallImage = createFallbackImage();
            rightVerticalWallImage = createFallbackImage();
        }
    }

    private BufferedImage createFallbackImage() {
        BufferedImage fallback = new BufferedImage(cellSize, cellSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = fallback.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, cellSize, cellSize);
        g2d.dispose();
        return fallback;
    }

    private void initializeButtonImages() {
        try {
            URL pauseUrl = getClass().getClassLoader().getResource("resources/Assets/pausegame.png");
            URL resumeUrl = getClass().getClassLoader().getResource("resources/Assets/resumegame.png");
            URL exitUrl = getClass().getClassLoader().getResource("resources/Assets/exitgame.png");

            if (pauseUrl == null || resumeUrl == null || exitUrl == null) {
                throw new IOException("Could not load pause/resume/exit button images.");
            }
            pauseButtonImage = ImageIO.read(pauseUrl);
            resumeButtonImage = ImageIO.read(resumeUrl);
            exitButtonImage = ImageIO.read(exitUrl);
        } catch (IOException e) {
            System.err.println("Error loading button images: " + e.getMessage());
        }
    }

    private void createPauseButton() {
        JButton pauseButton = new JButton();
        pauseButton.setBounds(850, 50, 64, 64);
        updatePauseButtonIcon(pauseButton);
        pauseButton.setBorderPainted(false);
        pauseButton.setFocusPainted(false);
        pauseButton.setContentAreaFilled(false);

        pauseButton.addActionListener(e -> {
            isPaused = !isPaused;
            requestFocusInWindow();
            updatePauseButtonIcon(pauseButton);
        });

        setLayout(null);
        add(pauseButton);
    }

    private void updatePauseButtonIcon(JButton button) {
        BufferedImage img = isPaused ? resumeButtonImage : pauseButtonImage;
        if (img != null) {
            button.setIcon(new ImageIcon(img.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        }
    }

    private void createExitButton() {
        JButton exitButton = new JButton();
        exitButton.setBounds(850, 120, 64, 64);
        exitButton.setBorderPainted(false);
        exitButton.setFocusPainted(false);
        exitButton.setContentAreaFilled(false);

        if (exitButtonImage != null) {
            exitButton.setIcon(new ImageIcon(exitButtonImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        }

        exitButton.addActionListener(e -> {
            // Return to main menu or close
            JFrame mainMenu = new RokueLikeMainMenu();
            mainMenu.setVisible(true);
            SwingUtilities.getWindowAncestor(this).dispose();
        });

        add(exitButton);
    }

    private void startMonsterSpawner() {
        monsterSpawnerTimer = new Timer(true);
        // Spawn a monster every 8 seconds
        monsterSpawnerTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isPaused) {
                    spawnMonster();
                    repaint();
                }
            }
        }, 0, 8000);
    }

    /**
     * Randomly picks one of the three monster types and creates it,
     * now that each monster has its own image path inside its class.
     */
    private void spawnMonster() {
        int attempts = 0;
        while (attempts < 50) {
            int col = random.nextInt(GRID_COLS - 2) + 1; // inside walls
            int row = random.nextInt(GRID_ROWS - 2) + 1;
            Point candidatePos = new Point(col * cellSize, row * cellSize);

            if (isCellFree(candidatePos)
                    && !isCollisionWithMonsters(candidatePos)
                    && (candidatePos.x != hero.getX() || candidatePos.y != hero.getY())) {

                Monster newMonster;
                int monsterType = random.nextInt(3);
                switch (monsterType) {
                    case 0:
                        newMonster = new ArcherMonster(candidatePos.x, candidatePos.y);
                        break;
                    case 1:
                        newMonster = new FighterMonster(candidatePos.x, candidatePos.y);
                        break;
                    default:
                        newMonster = new WizardMonster(candidatePos.x, candidatePos.y);
                        break;
                }
                monsters.add(newMonster);
                break;
            }
            attempts++;
        }
    }

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
        for (Monster monster : monsters) {
            Point monsterPos = new Point(monster.getX(), monster.getY());
            boolean moved = false;
            // Try up to 10 moves
            for (int attempt = 0; attempt < 10; attempt++) {
                Point newPos = new Point(monsterPos);
                int direction = random.nextInt(4);
                switch (direction) {
                    case 0: newPos.translate(0, -cellSize); break; // up
                    case 1: newPos.translate(0, cellSize);  break; // down
                    case 2: newPos.translate(-cellSize, 0); break; // left
                    case 3: newPos.translate(cellSize, 0);  break; // right
                }
                if (isCellFree(newPos)
                        && !isCollisionWithMonsters(newPos)
                        && (newPos.x != hero.getX() || newPos.y != hero.getY())) {
                    monster.setPosition(newPos.x, newPos.y);
                    moved = true;
                    break;
                }
            }
            monster.update(); // Let monster do any extra logic
        }
    }

    private boolean isCellFree(Point p) {
        int col = p.x / cellSize;
        int row = p.y / cellSize;
        if (row < 0 || row >= GRID_ROWS || col < 0 || col >= GRID_COLS) return false;
        if (grid[row][col] == BuildModePanel.CellType.WALL) return false;
        if (placedObjects[row][col] != null) return false;
        return true;
    }

    private boolean isCollisionWithMonsters(Point p) {
        for (Monster m : monsters) {
            if (m.getX() == p.x && m.getY() == p.y) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        drawBoard(g);
        drawGridLines(g);
        drawPlacedObjects(g);

        // Draw hero
        hero.draw(g);

        // Draw monsters
        for (Monster m : monsters) {
            m.draw(g);
        }
    }

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
                    int drawH = obj.isDouble ? cellSize * 2 : cellSize;
                    if (obj.isDouble) {
                        drawY -= (drawH - cellSize);
                    }
                    g.drawImage(obj.image, drawX, drawY, drawW, drawH, null);
                }
            }
        }
    }
}
