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
 * Enum to mark each cell as FLOOR or WALL.
 */
enum CellType {
    FLOOR,
    WALL
}

public class BuildModePanel extends JPanel {
    private Hall currentHall;

    // Grid dimensions
    private static final int GRID_ROWS = 13;
    private static final int GRID_COLS = 13;
    private int cellSize = 64;  // pixel size of each cell

    // 2D array for layout: WALL or FLOOR
    private CellType[][] grid;

    // 2D array for placed objects; null if none
    private PlacedObject[][] placedObjectsGrid;

    // Tile images
    private BufferedImage floorImage;
    private BufferedImage horizontalWallImage;
    private BufferedImage leftVerticalWallImage;
    private BufferedImage rightVerticalWallImage;

    // UI elements
    private JButton finishButton;
    private JPanel objectColumnPanel;

    // Objects in the sidebar
    private final List<BufferedImage> availableObjects;
    private final List<Boolean> isDoubleHeight;
    private final List<JPanel> objectPanels;

    // Currently selected object index; -1 if none
    private int selectedObjectIndex = -1;

    public BuildModePanel(Hall hall) {
        this.currentHall = hall;

        this.availableObjects = new ArrayList<>();
        this.isDoubleHeight = new ArrayList<>();
        this.objectPanels = new ArrayList<>();

        // 2D arrays
        this.grid = new CellType[GRID_ROWS][GRID_COLS];
        this.placedObjectsGrid = new PlacedObject[GRID_ROWS][GRID_COLS];

        setPreferredSize(new Dimension(1600, 900));
        setLayout(null);

        initializeGrid();
        initializeImages();
        initializeUI();
        loadObjects();

        // Mouse listener to place objects on the grid
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Only left-click
                if (e.getButton() != MouseEvent.BUTTON1) {
                    return;
                }

                // Must have a selected object
                if (selectedObjectIndex < 0) {
                    return;
                }

                int mouseX = e.getX();
                int mouseY = e.getY();

                // Ensure click is within the 13×13 grid
                if (mouseX < 0 || mouseX >= GRID_COLS * cellSize
                        || mouseY < 0 || mouseY >= GRID_ROWS * cellSize) {
                    return;
                }

                // Convert to row/column
                int gridCol = mouseX / cellSize;
                int gridRow = mouseY / cellSize;

                // Prevent placing on the outer boundary (top/bottom rows or left/right columns)
                if (gridRow == 0 || gridRow == GRID_ROWS - 1
                        || gridCol == 0 || gridCol == GRID_COLS - 1) {
                    return;
                }

                // Prevent placing on interior walls
                if (grid[gridRow][gridCol] == CellType.WALL) {
                    return;
                }

                // Remove any existing object at this cell
                placedObjectsGrid[gridRow][gridCol] = null;

                // Place the new object
                BufferedImage selImage = availableObjects.get(selectedObjectIndex);
                boolean selDouble = isDoubleHeight.get(selectedObjectIndex);

                placedObjectsGrid[gridRow][gridCol] = new PlacedObject(
                        selImage, gridRow, gridCol, selDouble
                );

                repaint();

                // Deselect after placing
                deselectCurrentObject();
            }
        });
    }

    /**
     * Initialize the 13×13 grid:
     * - Top (row=0) and bottom (row=12) are floors only (no walls).
     * - Left (col=0) and right (col=12) are walls for rows 1..11.
     * - Horizontal walls at row=1 and row=11, for columns 1..11.
     * - Everything else is floor.
     */
    private void initializeGrid() {
        // Fill all cells with FLOOR
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                grid[r][c] = CellType.FLOOR;
            }
        }

        // Vertical walls on the left & right edges, for rows 1..11
        for (int r = 1; r <= 11; r++) {
            grid[r][0] = CellType.WALL;          // left column
            grid[r][GRID_COLS - 1] = CellType.WALL;  // right column
        }

        // Horizontal walls on row=1 and row=11, for columns 1..11
        for (int c = 1; c <= 11; c++) {
            grid[1][c] = CellType.WALL;
            grid[11][c] = CellType.WALL;
        }

        // Notice we do NOT mark row=0 or row=12 as WALL,
        // so those are purely floors.
    }

    /**
     * Load floor/wall images from the sprite sheet.
     */
    private void initializeImages() {
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
            System.err.println("Error loading images: " + e.getMessage());
            e.printStackTrace();

            // Fallback images if loading fails
            floorImage = createFallbackImage();
            horizontalWallImage = createFallbackImage();
            leftVerticalWallImage = createFallbackImage();
            rightVerticalWallImage = createFallbackImage();
        }
    }

    /**
     * Create the Finish button and the sidebar panel for objects.
     */
    private void initializeUI() {
        finishButton = new JButton("Finish");
        finishButton.setBounds(1400, 800, 150, 50);
        finishButton.addActionListener(e -> startPlayMode());
        add(finishButton);

        objectColumnPanel = new JPanel();
        objectColumnPanel.setBounds(1300, 0, 200, 900);
        objectColumnPanel.setLayout(new BoxLayout(objectColumnPanel, BoxLayout.Y_AXIS));
        objectColumnPanel.setBorder(BorderFactory.createTitledBorder("Available Objects"));
        objectColumnPanel.setBackground(new Color(240, 240, 240));
        add(objectColumnPanel);
    }

    /**
     * Load objects from the sprite sheet into the sidebar (e.g. pillars, boxes).
     */
    private void loadObjects() {
        try {
            URL resourceUrl = getClass().getClassLoader().getResource(AssetPaths.SPRITESHEET.substring(1));
            if (resourceUrl == null) {
                System.err.println("Spritesheet not found!");
                return;
            }

            BufferedImage spriteSheet = ImageIO.read(resourceUrl);

            // Each entry: { int[] coords, boolean isDouble }
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

                BufferedImage objImage = spriteSheet.getSubimage(
                        coords[0], coords[1],
                        coords[2], coords[3]
                );

                availableObjects.add(objImage);
                isDoubleHeight.add(dbl);

                // Create a panel in the sidebar
                JPanel panel = createObjectPanel(objImage, dbl, i);
                objectPanels.add(panel);

                // Add to the UI
                objectColumnPanel.add(panel);
                objectColumnPanel.add(Box.createVerticalStrut(10));
            }

        } catch (IOException e) {
            System.err.println("Error loading spritesheet: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Create a fallback tile if an image fails to load.
     */
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

    /**
     * Create a clickable panel in the sidebar for a specific object image.
     */
    private JPanel createObjectPanel(BufferedImage objectImage, boolean isDouble, int index) {
        JPanel objectPanel = new JPanel(new BorderLayout());
        objectPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // If it's double-height, it occupies 2 cells tall visually
        int height = isDouble ? (cellSize * 2) : cellSize;
        Image scaledImage = objectImage.getScaledInstance(cellSize, height, Image.SCALE_SMOOTH);

        JLabel objectLabel = new JLabel(new ImageIcon(scaledImage));
        objectPanel.add(objectLabel, BorderLayout.CENTER);

        objectPanel.setMaximumSize(new Dimension(190, height + 10));
        objectPanel.setPreferredSize(new Dimension(190, height + 10));

        // Mouse to select/deselect
        objectPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (selectedObjectIndex == index) {
                    // Already selected => deselect
                    deselectCurrentObject();
                } else {
                    selectObject(index);
                }
            }
        });

        return objectPanel;
    }

    /**
     * Paint everything: floor first, then walls, then objects.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw floor & walls
        drawBoard(g);

        // (Optional) faint grid lines
        drawGridLines(g);

        // Draw the placed objects
        drawPlacedObjects(g);
    }

    /**
     * Always draw floor in each cell, then overlay the wall image if needed.
     */
    private void drawBoard(Graphics g) {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                int x = c * cellSize;
                int y = r * cellSize;

                // Always draw floor
                g.drawImage(floorImage, x, y, cellSize, cellSize, null);

                // If it's a wall, overlay a wall image
                if (grid[r][c] == CellType.WALL) {
                    // Left column
                    if (c == 0) {
                        g.drawImage(leftVerticalWallImage, x, y, cellSize, cellSize, null);
                    }
                    // Right column
                    else if (c == GRID_COLS - 1) {
                        g.drawImage(rightVerticalWallImage, x, y, cellSize, cellSize, null);
                    }
                    // Otherwise horizontal wall (top row inside, row=1 or row=11, etc.)
                    else {
                        g.drawImage(horizontalWallImage, x, y, cellSize, cellSize, null);
                    }
                }
            }
        }
    }

    /**
     * Draw faint grid lines for clarity.
     */
    private void drawGridLines(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(new Color(0, 0, 0, 30));

        // Horizontal lines
        for (int row = 0; row <= GRID_ROWS; row++) {
            int y = row * cellSize;
            g2d.drawLine(0, y, GRID_COLS * cellSize, y);
        }
        // Vertical lines
        for (int col = 0; col <= GRID_COLS; col++) {
            int x = col * cellSize;
            g2d.drawLine(x, 0, x, GRID_ROWS * cellSize);
        }

        g2d.dispose();
    }

    /**
     * Draw placed objects by scanning placedObjectsGrid.
     */
    private void drawPlacedObjects(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                PlacedObject obj = placedObjectsGrid[r][c];
                if (obj != null) {
                    int drawX = c * cellSize;
                    int drawY = r * cellSize;

                    int drawWidth = cellSize;
                    int drawHeight = obj.isDouble ? (cellSize * 2) : cellSize;

                    // Shift up if it's double-height
                    if (obj.isDouble) {
                        drawY -= (drawHeight - cellSize);
                    }

                    g2d.drawImage(obj.image, drawX, drawY, drawWidth, drawHeight, null);
                }
            }
        }
    }

    /**
     * Transition to Play Mode in a new window and close this BuildMode panel.
     */
    private void startPlayMode() {
        JFrame playModeFrame = new JFrame("Play Mode");
        playModeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        playModeFrame.setSize(1600, 900);
        playModeFrame.setLocationRelativeTo(null);

        JPanel emptyPanel = new JPanel();
        playModeFrame.add(emptyPanel);

        playModeFrame.setVisible(true);

        SwingUtilities.getWindowAncestor(this).dispose();
    }

    /**
     * Select an object in the sidebar (and highlight it).
     */
    private void selectObject(int index) {
        // Remove highlight from previous
        if (selectedObjectIndex >= 0 && selectedObjectIndex < objectPanels.size()) {
            JPanel prevPanel = objectPanels.get(selectedObjectIndex);
            prevPanel.setBackground(null);
            prevPanel.setOpaque(false);
        }

        selectedObjectIndex = index;

        // Highlight new panel
        JPanel newPanel = objectPanels.get(index);
        newPanel.setBackground(new Color(0, 0, 139));
        newPanel.setOpaque(true);
        newPanel.repaint();
    }

    /**
     * Deselect the current object (remove highlight).
     */
    private void deselectCurrentObject() {
        if (selectedObjectIndex >= 0 && selectedObjectIndex < objectPanels.size()) {
            JPanel current = objectPanels.get(selectedObjectIndex);
            current.setBackground(null);
            current.setOpaque(false);
            current.repaint();
        }
        selectedObjectIndex = -1;
    }

    /**
     * Data class for placed objects.
     */
    private static class PlacedObject {
        final BufferedImage image;
        final int gridRow;
        final int gridCol;
        final boolean isDouble;

        public PlacedObject(BufferedImage image, int gridRow, int gridCol, boolean isDouble) {
            this.image = image;
            this.gridRow = gridRow;
            this.gridCol = gridCol;
            this.isDouble = isDouble;
        }
    }
}
