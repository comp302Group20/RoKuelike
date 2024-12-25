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

public class GamePanel extends JPanel {
    private BuildModePanel.CellType[][] grid;
    private BuildModePanel.PlacedObject[][] placedObjects;
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
    private List<Monster> monsters;
    private Hero hero;
    private Random random;
    private Timer monsterSpawnerTimer;
    private Timer monsterMovementTimer;
    private boolean isPaused = false;
    private PausePopUp pausePopUp;


    public GamePanel(BuildModePanel.CellType[][] g, BuildModePanel.PlacedObject[][] p) {
        grid = g;
        placedObjects = p;
        setBackground(new Color(75, 30, 30));
        hero = new Hero(2 * cellSize, 2 * cellSize, cellSize, cellSize);
        monsters = new ArrayList<>();
        random = new Random();
        initializeFloorWallImages();
        initializeButtonImages();
        createPauseButton();
        createExitButton();
        startMonsterSpawner();
        startMonsterMovement();
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isPaused) return;
                int step = cellSize;
                int dx = 0;
                int dy = 0;
                if (e.getKeyCode() == KeyEvent.VK_LEFT) dx = -step;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) dx = step;
                if (e.getKeyCode() == KeyEvent.VK_UP) dy = -step;
                if (e.getKeyCode() == KeyEvent.VK_DOWN) dy = step;
                int ox = hero.getX();
                int oy = hero.getY();
                hero.move(dx, dy);
                Point np = new Point(hero.getX(), hero.getY());
                if (!isCellFree(np) || isCollisionWithMonsters(np)) {
                    hero.setPosition(ox, oy);
                }
                repaint();
            }
        });
    }

    private void spawnMonster() {
        int tries = 0;
        while (tries < 50) {
            int c = random.nextInt(GRID_COLS - 2) + 1;
            int r = random.nextInt(GRID_ROWS - 2) + 1;
            Point p = new Point(c * cellSize, r * cellSize);
            if (isCellFree(p) && !isCollisionWithMonsters(p) && (p.x != hero.getX() || p.y != hero.getY())) {
                Monster m;
                int t = random.nextInt(3);
                if (t == 0) m = new ArcherMonster(p.x, p.y, hero, grid);
                else if (t == 1) m = new FighterMonster(p.x, p.y, hero, grid);
                else m = new WizardMonster(p.x, p.y, hero, grid);
                monsters.add(m);
                break;
            }
            tries++;
        }
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
        }, 0, 8000);
    }

    private void startMonsterMovement() {
        monsterMovementTimer = new Timer(true);
        monsterMovementTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!isPaused) {
                    for (Monster m : monsters) {
                        if (m instanceof FighterMonster) {
                            m.update();
                        } else {
                            m.update();
                        }
                    }
                    repaint();
                }
            }
        }, 0, 500);
    }

    private boolean isCellFree(Point p) {
        int c = p.x / cellSize;
        int r = p.y / cellSize;
        if (r < 0 || r >= GRID_ROWS || c < 0 || c >= GRID_COLS) return false;
        if (grid[r][c] == BuildModePanel.CellType.WALL) return false;
        if (placedObjects[r][c] != null) return false;
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

    private void initializeFloorWallImages() {
        try {
            URL url = getClass().getClassLoader().getResource("resources/Assets/spritesheet.png");
            if (url == null) throw new IOException();
            BufferedImage sheet = ImageIO.read(url);
            floorImage = sheet.getSubimage(32, 48, 16, 16);
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

    private BufferedImage fallback() {
        BufferedImage f = new BufferedImage(cellSize, cellSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = f.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, cellSize, cellSize);
        g2d.dispose();
        return f;
    }

    private void initializeButtonImages() {
        try {
            URL p = getClass().getClassLoader().getResource("resources/Assets/pausegame.png");
            URL r = getClass().getClassLoader().getResource("resources/Assets/resumegame.png");
            URL e = getClass().getClassLoader().getResource("resources/Assets/exitgame.png");
            if (p == null || r == null || e == null) throw new IOException();
            pauseButtonImage = ImageIO.read(p);
            resumeButtonImage = ImageIO.read(r);
            exitButtonImage = ImageIO.read(e);
        } catch (IOException ex) {
        }
    }

    private void createPauseButton() {
        JButton b = new JButton();
        b.setBounds(850, 50, 64, 64);
        updatePauseButtonIcon(b);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.addActionListener(e -> {
            isPaused = !isPaused;
            requestFocusInWindow();
            updatePauseButtonIcon(b);
            if (isPaused) {
                showPausePopUp(); // Show the pop-up when paused
            } else {
                hidePausePopUp(); // Hide the pop-up when unpaused
            }
        });
        setLayout(null);
        add(b);
    }

    private void showPausePopUp() {
        if (pausePopUp == null) {
        pausePopUp = new PausePopUp();
        pausePopUp.setBounds(0,0, getWidth(), getHeight()); // Full-size overlay
        pausePopUp.setOpaque(false); // Transparency for the background
    
        setLayout(null);
        add(pausePopUp);
        pausePopUp.setVisible(true);
        // Force a repaint to ensure the panel appears
        revalidate(); 
        repaint();  
        }
        pausePopUp.setVisible(true);
    }

    private void hidePausePopUp() {
        if (pausePopUp != null) {
            pausePopUp.setVisible(false);
            remove(pausePopUp); // Optionally remove it from the panel to clean up
            pausePopUp = null; // Clear reference if removed
            revalidate(); 
            repaint(); 
        }
    }

    private void updatePauseButtonIcon(JButton b) {
        BufferedImage i = isPaused ? resumeButtonImage : pauseButtonImage;
        if (i != null) {
            b.setIcon(new ImageIcon(i.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        }
    }

    private void createExitButton() {
        JButton b = new JButton();
        b.setBounds(850, 120, 64, 64);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        if (exitButtonImage != null) {
            b.setIcon(new ImageIcon(exitButtonImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        }
        b.addActionListener(e -> {
            JFrame mm = new RokueLikeMainMenu();
            mm.setVisible(true);
            SwingUtilities.getWindowAncestor(this).dispose();
        });
        add(b);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawGridLines(g);
        drawPlacedObjects(g);
        hero.draw(g);
        for (Monster m : monsters) {
            m.draw(g);
        }
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Health: " + hero.getHealth(), 10, 20);
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
                    int dx = c * cellSize;
                    int dy = r * cellSize;
                    int dw = cellSize;
                    int dh = obj.isDouble ? cellSize * 2 : cellSize;
                    if (obj.isDouble) {
                        dy -= (dh - cellSize);
                    }
                    g.drawImage(obj.image, dx, dy, dw, dh, null);
                }
            }
        }
    }
}
