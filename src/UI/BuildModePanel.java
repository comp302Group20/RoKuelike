package UI;

import Domain.Hall;
import Utils.AssetPaths;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The panel for building a single Hall's layout: placing objects, walls, etc.
 */
public class BuildModePanel extends JPanel {

    public enum CellType {
        FLOOR,
        WALL
    }

    private Hall currentHall;

    // Grid for build mode (13×13 by default here)
    private static final int GRID_ROWS = 13;
    private static final int GRID_COLS = 13;
    private int cellSize = 64;

    private CellType[][] grid;
    private PlacedObject[][] placedObjectsGrid;

    // Tile images
    private BufferedImage floorImage;
    private BufferedImage horizontalWallImage;
    private BufferedImage leftVerticalWallImage;
    private BufferedImage rightVerticalWallImage;

    // Sidebar for selecting objects
    private JPanel objectColumnPanel;
    private final List<BufferedImage> availableObjects = new ArrayList<>();
    private final List<Boolean> isDoubleHeight = new ArrayList<>();
    private final List<JPanel> objectPanels = new ArrayList<>();
    private int selectedObjectIndex = -1;

    public BuildModePanel(Hall hall) {
        this.currentHall = hall;
        this.grid = new CellType[GRID_ROWS][GRID_COLS];
        this.placedObjectsGrid = new PlacedObject[GRID_ROWS][GRID_COLS];

        setPreferredSize(new Dimension(1600, 900));
        setLayout(null);

        initializeGrid();
        initializeImages();
        initializeUI();
        loadObjects();

        // Mouse listener to place objects
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) return;
                if (selectedObjectIndex < 0) return;

                int mouseX = e.getX();
                int mouseY = e.getY();

                // Check bounds
                if (mouseX < 0 || mouseX >= GRID_COLS * cellSize
                        || mouseY < 0 || mouseY >= GRID_ROWS * cellSize) {
                    return;
                }

                int gridCol = mouseX / cellSize;
                int gridRow = mouseY / cellSize;

                // Disallow placing on outer boundary or interior walls
                if (gridRow == 0 || gridRow == GRID_ROWS - 1
                        || gridCol == 0 || gridCol == GRID_COLS - 1
                        || grid[gridRow][gridCol] == CellType.WALL) {
                    return;
                }

                // Place the object
                placedObjectsGrid[gridRow][gridCol] = null; // remove any existing
                BufferedImage selImage = availableObjects.get(selectedObjectIndex);
                boolean selDouble = isDoubleHeight.get(selectedObjectIndex);

                placedObjectsGrid[gridRow][gridCol] = new PlacedObject(selImage, gridRow, gridCol, selDouble);
                repaint();

                // Increase Hall’s object count (optional—if you want to track each add)
                // currentHall.addObject(gridRow, gridCol, ... ) // Not implemented here

                // Deselect
                deselectCurrentObject();
            }
        });
    }

    /**
     * Provide access to the final grid layout (FLOOR vs WALL).
     */
    public CellType[][] getGrid() {
        return grid;
    }

    /**
     * Provide access to the final placed objects array.
     */
    public PlacedObject[][] getPlacedObjectsGrid() {
        return placedObjectsGrid;
    }

    private void initializeGrid() {
        // Fill all with FLOOR
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                grid[r][c] = CellType.FLOOR;
            }
        }
        // Vertical walls on left & right edges, for rows 1..11
        for (int r = 1; r <= 11; r++) {
            grid[r][0] = CellType.WALL;
            grid[r][GRID_COLS - 1] = CellType.WALL;
        }
        // Horizontal walls on row=1 and row=11, for columns 1..11
        for (int c = 1; c <= 11; c++) {
            grid[1][c] = CellType.WALL;
            grid[11][c] = CellType.WALL;
        }
    }

    private void initializeImages() {
        try {
            URL resourceUrl = getClass().getClassLoader().getResource(AssetPaths.SPRITESHEET.substring(1));
            if (resourceUrl == null) {
                throw new IOException("Sprite sheet not found: " + AssetPaths.SPRITESHEET);
            }
            BufferedImage spriteSheet = ImageIO.read(resourceUrl);

            // Floor
            int[] floorCoords = AssetPaths.FLOOR_TILE;
            floorImage = spriteSheet.getSubimage(floorCoords[0], floorCoords[1], floorCoords[2], floorCoords[3]);

            // Walls
            horizontalWallImage = spriteSheet.getSubimage(17 * 16, 16, 16, 16);
            leftVerticalWallImage = spriteSheet.getSubimage(16 * 16, 16, 16, 16);
            rightVerticalWallImage = spriteSheet.getSubimage(18 * 16, 16, 16, 16);

        } catch (IOException e) {
            System.err.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
            floorImage = createFallbackImage();
            horizontalWallImage = createFallbackImage();
            leftVerticalWallImage = createFallbackImage();
            rightVerticalWallImage = createFallbackImage();
        }
    }

    private BufferedImage createFallbackImage() {
        BufferedImage fallback = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = fallback.createGraphics();
        g2d.setColor(Color.GRAY);
        g2d.fillRect(0, 0, 16, 16);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, 15, 15);
        g2d.dispose();
        return fallback;
    }

    private void initializeUI() {
        // We do not finalize everything here because the controlling JFrame is outside
        objectColumnPanel = new JPanel();
        objectColumnPanel.setBounds(1300, 0, 200, 900);
        objectColumnPanel.setLayout(new BoxLayout(objectColumnPanel, BoxLayout.Y_AXIS));
        objectColumnPanel.setBorder(BorderFactory.createTitledBorder("Available Objects"));
        objectColumnPanel.setBackground(new Color(240, 240, 240));
        add(objectColumnPanel);
    }

    private void loadObjects() {
        try {
            URL resourceUrl = getClass().getClassLoader().getResource(AssetPaths.SPRITESHEET.substring(1));
            if (resourceUrl == null) {
                System.err.println("Spritesheet not found!");
                return;
            }

            BufferedImage spriteSheet = ImageIO.read(resourceUrl);

            // { coords, isDouble }
            Object[][] objectDefinitions = {
                    {AssetPaths.PILLAR, true},
                    {AssetPaths.LADDER, false},
                    {AssetPaths.BOX, false},
                    {AssetPaths.DOUBLE_BOX, true},
                    {AssetPaths.TORCH, false},
                    {AssetPaths.SKULL, false},
                    {AssetPaths.CHEST, false},
                    {AssetPaths.POTION, false}
            };

            for (int i = 0; i < objectDefinitions.length; i++) {
                int[] coords = (int[]) objectDefinitions[i][0];
                boolean dbl = (boolean) objectDefinitions[i][1];
                BufferedImage objImage = spriteSheet.getSubimage(coords[0], coords[1], coords[2], coords[3]);

                availableObjects.add(objImage);
                isDoubleHeight.add(dbl);

                // Create panel in sidebar
                JPanel panel = createObjectPanel(objImage, dbl, i);
                objectPanels.add(panel);
                objectColumnPanel.add(panel);
                objectColumnPanel.add(Box.createVerticalStrut(10));
            }
        } catch (IOException e) {
            System.err.println("Error loading spritesheet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private JPanel createObjectPanel(BufferedImage objectImage, boolean isDouble, int index) {
        JPanel objectPanel = new JPanel(new BorderLayout());
        objectPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        int h = isDouble ? (cellSize * 2) : cellSize;
        Image scaledImage = objectImage.getScaledInstance(cellSize, h, Image.SCALE_SMOOTH);
        JLabel objectLabel = new JLabel(new ImageIcon(scaledImage));
        objectPanel.add(objectLabel, BorderLayout.CENTER);

        objectPanel.setMaximumSize(new Dimension(190, h + 10));
        objectPanel.setPreferredSize(new Dimension(190, h + 10));

        objectPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (selectedObjectIndex == index) {
                    // Deselect
                    deselectCurrentObject();
                } else {
                    selectObject(index);
                }
            }
        });

        return objectPanel;
    }

    private void selectObject(int index) {
        // Remove highlight from previous
        if (selectedObjectIndex >= 0 && selectedObjectIndex < objectPanels.size()) {
            JPanel prev = objectPanels.get(selectedObjectIndex);
            prev.setBackground(null);
            prev.setOpaque(false);
        }
        selectedObjectIndex = index;

        // Highlight new
        JPanel newPanel = objectPanels.get(index);
        newPanel.setBackground(new Color(0, 0, 139));
        newPanel.setOpaque(true);
        newPanel.repaint();
    }

    private void deselectCurrentObject() {
        if (selectedObjectIndex >= 0 && selectedObjectIndex < objectPanels.size()) {
            JPanel curr = objectPanels.get(selectedObjectIndex);
            curr.setBackground(null);
            curr.setOpaque(false);
            curr.repaint();
        }
        selectedObjectIndex = -1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawGridLines(g);
        drawPlacedObjects(g);
    }

    private void drawBoard(Graphics g) {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                int x = c * cellSize;
                int y = r * cellSize;
                g.drawImage(floorImage, x, y, cellSize, cellSize, null);

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
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(new Color(0, 0, 0, 30));
        for (int row = 0; row <= GRID_ROWS; row++) {
            int y = row * cellSize;
            g2d.drawLine(0, y, GRID_COLS * cellSize, y);
        }
        for (int col = 0; col <= GRID_COLS; col++) {
            int x = col * cellSize;
            g2d.drawLine(x, 0, x, GRID_ROWS * cellSize);
        }
        g2d.dispose();
    }

    private void drawPlacedObjects(Graphics g) {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                PlacedObject obj = placedObjectsGrid[r][c];
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

    /**
     * Data class for placed objects.
     */
    public static class PlacedObject {
        public final BufferedImage image;
        public final int gridRow;
        public final int gridCol;
        public final boolean isDouble;

        public PlacedObject(BufferedImage image, int gridRow, int gridCol, boolean isDouble) {
            this.image = image;
            this.gridRow = gridRow;
            this.gridCol = gridCol;
            this.isDouble = isDouble;
        }
    }
}
