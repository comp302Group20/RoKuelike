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
import java.util.Queue;
import java.util.LinkedList;
import java.util.Timer;

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
    private List<Monster> monsters;
    private Hero hero;
    private Random random;
    private Timer monsterSpawnerTimer;
    private Timer monsterMovementTimer;
    private boolean isPaused = false;
    private PausePopUp pausePopUp;


    public GamePanel(BuildModePanel.CellType[][] g, PlacedObject[][] p) {
        grid = g;
        placedObjects = p;
        setBackground(new Color(75, 30, 30));
        hero = new Hero(2 * cellSize, 2 * cellSize, cellSize, cellSize);
        monsters = new ArrayList<>();
        random = new Random();
        hideRuneInRandomObject();
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
                if (!canHeroMove(np)) {
                    hero.setPosition(ox, oy);
                }
                repaint();
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (isPaused) return;
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
                        System.out.println("Door opens (placeholder)");
                    }
                }
            }
        });
    }

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

    private PlacedObject getClickedObject(int mx, int my) {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                PlacedObject obj = placedObjects[r][c];
                if (obj != null) {
                    int topY = r * cellSize;
                    int leftX = c * cellSize;
                    int w = cellSize;
                    int h = obj.isDouble ? (cellSize * 2) : cellSize;
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

    private void loadRuneImage() {
        try {
            URL r = getClass().getClassLoader().getResource(AssetPaths.RUNE.substring(1));
            if (r == null) throw new IOException();
            runeImage = ImageIO.read(r);
        } catch (IOException e) {
            runeImage = null;
        }
    }

    private void spawnMonster() {
        int tries = 0;
        while (tries < 50) {
            int c = random.nextInt(GRID_COLS - 2) + 1;
            int r = random.nextInt(GRID_ROWS - 2) + 1;
            Point pt = new Point(c * cellSize, r * cellSize);
            if (canMonsterMove(null, pt.x, pt.y) && (pt.x != hero.getX() || pt.y != hero.getY())) {
                Monster m;
                int t = random.nextInt(3);
                if (t == 0) m = new ArcherMonster(pt.x, pt.y, hero, grid, this);
                else if (t == 1) m = new FighterMonster(pt.x, pt.y, hero, grid, this);
                else m = new WizardMonster(pt.x, pt.y, hero, grid);
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
                        m.update();
                    }
                    repaint();
                }
            }
        }, 0, 500);
    }

    public boolean canMonsterMove(Monster monster, int nx, int ny) {
        int c = nx / cellSize;
        int r = ny / cellSize;
        if (r < 0 || r >= GRID_ROWS || c < 0 || c >= GRID_COLS) return false;
        if (grid[r][c] == BuildModePanel.CellType.WALL) return false;
        PlacedObject po = placedObjects[r][c];
        if (po != null) return false;
        for (Monster mm : monsters) {
            if (mm != monster && mm.getX() == nx && mm.getY() == ny) {
                return false;
            }
        }
        if (hero.getX() == nx && hero.getY() == ny) return false;
        return true;
    }

    private boolean canHeroMove(Point p) {
        int c = p.x / cellSize;
        int r = p.y / cellSize;
        if (r < 0 || r >= GRID_ROWS || c < 0 || c >= GRID_COLS) return false;
        if (grid[r][c] == BuildModePanel.CellType.WALL) return false;
        if (placedObjects[r][c] != null) return false;
        for (Monster mm : monsters) {
            if (mm.getX() == p.x && mm.getY() == p.y) {
                return false;
            }
        }
        return true;
    }

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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
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

    private boolean isCoveredByObject(Object entity) {
        int ex = entity instanceof Monster ? ((Monster) entity).getX() : hero.getX();
        int ey = entity instanceof Monster ? ((Monster) entity).getY() : hero.getY();
        int ec = ex / cellSize;
        int er = ey / cellSize;
        if (er < 0 || er >= GRID_ROWS || ec < 0 || ec >= GRID_COLS) return false;
        PlacedObject obj = placedObjects[er][ec];
        return obj != null && obj.isDouble;
    }

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

    private void initializeFloorWallImages() {
        try {
            URL url = getClass().getClassLoader().getResource(AssetPaths.SPRITESHEET.substring(1));
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

    private void createPauseButton() {
        JButton b = new JButton();
        b.setBounds(850, 50, 64, 64);
        updatePauseButtonIcon(b);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.addActionListener(ev -> {
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
        JButton x = new JButton();
        x.setBounds(850, 120, 64, 64);
        x.setBorderPainted(false);
        x.setFocusPainted(false);
        x.setContentAreaFilled(false);
        if (exitButtonImage != null) {
            x.setIcon(new ImageIcon(exitButtonImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
        }
        x.addActionListener(e -> {
            JFrame mm = new RokueLikeMainMenu();
            mm.setVisible(true);
            SwingUtilities.getWindowAncestor(this).dispose();
        });
        add(x);
    }
}
