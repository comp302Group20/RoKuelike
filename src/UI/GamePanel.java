package UI;

import Utils.AssetPaths;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GamePanel extends JPanel {
    private BufferedImage heroImage;
    private BufferedImage pauseButtonImage;
    private BufferedImage resumeButtonImage;
    private BufferedImage exitButtonImage;
    private List<BufferedImage> monsterImages;
    private Point heroPosition;
    private int cellSize = 64;
    private List<Point> monsterPositions;
    private List<Integer> spawnedMonsterTypes;
    private Random random;
    private Timer monsterSpawnerTimer;
    private Timer monsterMovementTimer;
    private boolean isPaused = false;

    public GamePanel() {
        setBackground(new Color(75, 30, 30));
        this.heroPosition = new Point(0, 0); // Hero starting position at top-left
        this.monsterPositions = new ArrayList<>();
        this.spawnedMonsterTypes = new ArrayList<>();
        this.monsterImages = new ArrayList<>();
        this.random = new Random();

        initializeHeroImage();
        initializeMonsterImages();
        initializeButtonImages();
        startMonsterSpawner();
        startMonsterMovement();
        initializePauseButton();
        initializeExitButton();

        setFocusable(true);
        requestFocusInWindow();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isPaused) return;

                int step = cellSize;
                Point newHeroPosition = new Point(heroPosition);
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        if (heroPosition.x > 0) newHeroPosition.translate(-step, 0);
                        break;
                    case KeyEvent.VK_RIGHT:
                        if (heroPosition.x < (12 * cellSize)) newHeroPosition.translate(step, 0);
                        break;
                    case KeyEvent.VK_UP:
                        if (heroPosition.y > 0) newHeroPosition.translate(0, -step);
                        break;
                    case KeyEvent.VK_DOWN:
                        if (heroPosition.y < (12 * cellSize)) newHeroPosition.translate(0, step);
                        break;
                }
                if (!isCollisionWithMonsters(newHeroPosition)) {
                    heroPosition.setLocation(newHeroPosition);
                }
                repaint();
            }
        });
    }

    private void initializeHeroImage() {
        try {
            URL resourceUrl = getClass().getClassLoader().getResource(AssetPaths.HERO.substring(1));
            System.out.println("Loading hero image from: " + resourceUrl);

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
        String[] monsterPaths = {AssetPaths.ARCHER, AssetPaths.FIGHTER, AssetPaths.WIZARD};
        for (String path : monsterPaths) {
            try {
                URL resourceUrl = getClass().getClassLoader().getResource(path.substring(1));
                System.out.println("Loading monster image from: " + resourceUrl);

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
        }, 0, 8000); // Spawn a monster every 8 seconds
    }

    private void spawnMonster() {
        Point randomPosition;
        do {
            int randomX = random.nextInt(13) * cellSize;
            int randomY = random.nextInt(13) * cellSize;
            randomPosition = new Point(randomX, randomY);
        } while (randomPosition.equals(heroPosition) || monsterPositions.contains(randomPosition));

        int monsterType = random.nextInt(monsterImages.size());
        monsterPositions.add(randomPosition);
        spawnedMonsterTypes.add(monsterType);
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
        for (Point monsterPosition : monsterPositions) {
            Point newMonsterPosition;
            do {
                newMonsterPosition = new Point(monsterPosition);
                int direction = random.nextInt(4);
                switch (direction) {
                    case 0: // Move up
                        if (newMonsterPosition.y > 0) newMonsterPosition.translate(0, -cellSize);
                        break;
                    case 1: // Move down
                        if (newMonsterPosition.y < (12 * cellSize)) newMonsterPosition.translate(0, cellSize);
                        break;
                    case 2: // Move left
                        if (newMonsterPosition.x > 0) newMonsterPosition.translate(-cellSize, 0);
                        break;
                    case 3: // Move right
                        if (newMonsterPosition.x < (12 * cellSize)) newMonsterPosition.translate(cellSize, 0);
                        break;
                }
            } while (newMonsterPosition.equals(heroPosition) || monsterPositions.contains(newMonsterPosition));

            monsterPosition.setLocation(newMonsterPosition);
        }
    }

    private boolean isCollisionWithMonsters(Point position) {
        return monsterPositions.contains(position);
    }

    private void initializePauseButton() {
        JButton pauseButton = new JButton();
        pauseButton.setBounds(850, 50, 64, 64); // Positioned outside the grid
        updatePauseButtonImage(pauseButton);
        pauseButton.setBorderPainted(false);
        pauseButton.setFocusPainted(false);
        pauseButton.setContentAreaFilled(false);
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                isPaused = !isPaused;
                requestFocusInWindow(); // Refocus to ensure key events work after resuming
                updatePauseButtonImage(pauseButton);
            }
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
        exitButton.setBounds(850, 120, 64, 64); // Positioned below the pause button
        exitButton.setBorderPainted(false);
        exitButton.setFocusPainted(false);
        exitButton.setContentAreaFilled(false);

        if (exitButtonImage != null) {
            exitButton.setIcon(new ImageIcon(exitButtonImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        }

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFrame mainMenu = new RokueLikeMainMenu();
                mainMenu.setVisible(true);
                SwingUtilities.getWindowAncestor(GamePanel.this).dispose(); // Close the game panel window
            }
        });

        setLayout(null);
        add(exitButton);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw grid
        drawGrid(g);
        drawGridLines(g);

        // Draw hero
        g.drawImage(heroImage, heroPosition.x, heroPosition.y, cellSize, cellSize, null);

        // Draw monsters
        for (int i = 0; i < monsterPositions.size(); i++) {
            Point monsterPosition = monsterPositions.get(i);
            BufferedImage monsterImage = monsterImages.get(spawnedMonsterTypes.get(i));
            g.drawImage(monsterImage, monsterPosition.x, monsterPosition.y, cellSize, cellSize, null);
        }
    }

    private void drawGrid(Graphics g) {
        g.setColor(new Color(37, 0, 0));

        for (int r = 0; r < 13; r++) {
            for (int c = 0; c < 13; c++) {
                int drawX = c * cellSize;
                int drawY = r * cellSize;
                g.fillRect(drawX, drawY, cellSize, cellSize);
            }
        }
    }

    private void drawGridLines(Graphics g) {
        g.setColor(new Color(75, 30, 30, 50));

        for (int x = 0; x <= 13; x++) {
            g.drawLine(x * cellSize, 0, x * cellSize, 13 * cellSize);
        }

        for (int y = 0; y <= 13; y++) {
            g.drawLine(0, y * cellSize, 13 * cellSize, y * cellSize);
        }
    }

    public static void main(String[] args) {
        // Debug resource path
        URL resource = GamePanel.class.getClassLoader().getResource(AssetPaths.HERO.substring(1));
        if (resource != null) {
            System.out.println("Resource found: " + resource);
        } else {
            System.out.println("Resource not found: " + AssetPaths.HERO);
        }

        JFrame frame = new JFrame("Game Panel Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 900); // Ensure grid aligns within frame and space for the button
        frame.setLocationRelativeTo(null);

        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel);

        frame.setVisible(true);
    }
}
