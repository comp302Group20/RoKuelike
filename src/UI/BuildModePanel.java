package UI;

import Controller.GameController;
import Domain.Hall;
import Utils.AssetPaths;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class BuildModePanel extends JPanel {
    public enum CellType {
        FLOOR,
        WALL
    }

    private Hall currentHall;
    private static final int GRID_ROWS = 13;
    private static final int GRID_COLS = 13;
    private final int cellSize = 64;
    private final CellType[][] grid;
    private final PlacedObject[][] placedObjectsGrid;
    private BufferedImage floorImage;
    private BufferedImage horizontalWallImage;
    private BufferedImage leftVerticalWallImage;
    private BufferedImage rightVerticalWallImage;
    private JPanel objectColumnPanel;
    private final List<BufferedImage> availableObjects = new ArrayList<>();
    private final List<Boolean> isDoubleHeight = new ArrayList<>();
    private final List<JPanel> objectPanels = new ArrayList<>();
    private int selectedObjectIndex = -1;
    private GameController gameController; // Add this field

    private JFrame parentFrame; // Add this field

    public BuildModePanel(Hall hall, GameController controller, JFrame frame) {
        currentHall = hall;
        this.gameController = controller;
        this.parentFrame = frame;

        grid = new CellType[GRID_ROWS][GRID_COLS];
        placedObjectsGrid = new PlacedObject[GRID_ROWS][GRID_COLS];
        setPreferredSize(new Dimension(1000, 900));
        setLayout(null);

        // Add this line to set the background color
        setBackground(new Color(255, 255, 255));

        initializeGrid();
        initializeImages();
        initializeUI();
        loadObjects();
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != MouseEvent.BUTTON1) return; // Only respond to left-click

                int mouseX = e.getX();
                int mouseY = e.getY();

                boolean insideGrid = mouseX >= 0 && mouseX < GRID_COLS * cellSize &&
                        mouseY >= 0 && mouseY < GRID_ROWS * cellSize;

                if (insideGrid) {
                    int gridCol = mouseX / cellSize;
                    int gridRow = mouseY / cellSize;

                    // Prevent interactions with walls
                    if (isWallCell(gridRow, gridCol)) {
                        return;
                    }

                    if (selectedObjectIndex >= 0 && selectedObjectIndex < availableObjects.size()) {
                        // Placement Mode: Place or Remove Object
                        PlacedObject existingObj = placedObjectsGrid[gridRow][gridCol];
                        if (existingObj != null) {
                            // Remove the object
                            removePlacedObject(gridRow, gridCol);
                        } else {
                            // Place the selected object
                            placeObject(gridRow, gridCol);
                        }
                        repaint();
                    } else {
                        // Non-Placement Mode: Remove Object if Exists
                        PlacedObject existingObj = placedObjectsGrid[gridRow][gridCol];
                        if (existingObj != null) {
                            removePlacedObject(gridRow, gridCol);
                            repaint();
                        }
                        // Else, do nothing
                    }
                } else {
                    // Clicked outside the grid; exit placement mode
                    deselectCurrentObject();
                }
            }
        });
    }

    /**
     * Returns the 2D array of cell types (Floor or Wall).
     */
    public CellType[][] getGrid() {
        return grid;
    }

    /**
     * Returns the 2D array of placed objects.
     */
    public PlacedObject[][] getPlacedObjectsGrid() {
        return placedObjectsGrid;
    }

    /**
     * Returns how many objects have been placed on the grid.
     */
    public int getNumberOfPlacedObjects() {
        int count = 0;
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                if (placedObjectsGrid[r][c] != null) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Initialize all cells as FLOOR and set walls around the perimeter
     * (based on the original code/logic for walls).
     */
    private void initializeGrid() {
        // Initialize all cells as FLOOR
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                grid[r][c] = CellType.FLOOR;
            }
        }
        // Set vertical walls
        for (int r = 1; r <= 11; r++) {
            grid[r][0] = CellType.WALL;
            grid[r][GRID_COLS - 1] = CellType.WALL;
        }
        // Set horizontal walls
        for (int c = 1; c <= 11; c++) {
            grid[1][c] = CellType.WALL;
            grid[11][c] = CellType.WALL;
        }
    }

    private void initializeImages() {
        try {
            URL resourceUrl = getClass().getClassLoader().getResource(AssetPaths.SPRITESHEET.substring(1));
            if (resourceUrl == null) {
                throw new IOException("Spritesheet not found.");
            }
            BufferedImage spriteSheet = ImageIO.read(resourceUrl);
            int[] floorCoords = AssetPaths.FLOOR_TILE;
            floorImage = spriteSheet.getSubimage(floorCoords[0], floorCoords[1], floorCoords[2], floorCoords[3]);
            horizontalWallImage = spriteSheet.getSubimage(17 * 16, 16, 16, 16);
            leftVerticalWallImage = spriteSheet.getSubimage(16 * 16, 16, 16, 16);
            rightVerticalWallImage = spriteSheet.getSubimage(18 * 16, 16, 16, 16);
        } catch (IOException e) {
            floorImage = fallbackImage();
            horizontalWallImage = fallbackImage();
            leftVerticalWallImage = fallbackImage();
            rightVerticalWallImage = fallbackImage();
        }
    }

    private BufferedImage fallbackImage() {
        BufferedImage fb = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = fb.createGraphics();
        g2d.setColor(Color.GRAY);
        g2d.fillRect(0, 0, 16, 16);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, 15, 15);
        g2d.dispose();
        return fb;
    }

    private void initializeUI() {
        objectColumnPanel = new JPanel();
        objectColumnPanel.setBounds(832 + 10, 0, 150, 900);
        objectColumnPanel.setLayout(new BoxLayout(objectColumnPanel, BoxLayout.Y_AXIS));
        objectColumnPanel.setBorder(BorderFactory.createTitledBorder("Available Objects"));
        // Change this line to match the new background color
        objectColumnPanel.setBackground(new Color(255, 165, 0));
        add(objectColumnPanel);
    }

    private void loadObjects() {
        try {
            URL resourceUrl = getClass().getClassLoader().getResource(AssetPaths.SPRITESHEET.substring(1));
            if (resourceUrl == null) {
                return;
            }
            BufferedImage spriteSheet = ImageIO.read(resourceUrl);
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
                JPanel panel = createObjectPanel(objImage, dbl, i);
                objectPanels.add(panel);
                objectColumnPanel.add(panel);
                objectColumnPanel.add(Box.createVerticalStrut(10));
            }
        } catch (IOException e) {
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
        objectPanel.setMaximumSize(new Dimension(230, h + 10));  // Adjusted width
        objectPanel.setPreferredSize(new Dimension(230, h + 10));
        objectPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        objectPanel.setBackground(null);
        objectPanel.setOpaque(false);

        objectPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (selectedObjectIndex == index) {
                    deselectCurrentObject();
                } else {
                    selectObject(index);
                }
            }
        });
        return objectPanel;
    }

    private void selectObject(int index) {
        if (selectedObjectIndex >= 0 && selectedObjectIndex < objectPanels.size()) {
            JPanel prev = objectPanels.get(selectedObjectIndex);
            prev.setBackground(null);
            prev.setOpaque(false);
            prev.repaint();
        }
        selectedObjectIndex = index;
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

    private boolean isWallCell(int r, int c) {
        return grid[r][c] == CellType.WALL;
    }

    /**
     * Place the selected object in the grid cell. Double-height objects only occupy this one cell logically.
     */
    private void placeObject(int gridRow, int gridCol) {
        BufferedImage selImage = availableObjects.get(selectedObjectIndex);
        boolean selDouble = isDoubleHeight.get(selectedObjectIndex);

        // If it's a double-height object, ensure there's space above for it visually,
        // but do not mark the upper cell as occupied.
        if (selDouble) {
            if (gridRow <= 0) {
                JOptionPane.showMessageDialog(BuildModePanel.this,
                        "Not enough space to place a double-height object here.",
                        "Placement Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (isWallCell(gridRow - 1, gridCol)) {
                JOptionPane.showMessageDialog(BuildModePanel.this,
                        "Cannot place double-height object here. Space above is a wall.",
                        "Placement Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        // Check if the target cell is already occupied
        if (placedObjectsGrid[gridRow][gridCol] != null) {
            JOptionPane.showMessageDialog(BuildModePanel.this,
                    "Cannot place object here. Space is already occupied.",
                    "Placement Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PlacedObject newObject = new PlacedObject(selImage, gridRow, gridCol, selDouble);

        // Define object types and their coordinates
        Object[][] objectDefinitions = {
                {AssetPaths.PILLAR, "PILLAR", true},
                {AssetPaths.LADDER, "LADDER", false},
                {AssetPaths.BOX, "BOX", false},
                {AssetPaths.DOUBLE_BOX, "DOUBLE_BOX", true},
                {AssetPaths.TORCH, "TORCH", false},
                {AssetPaths.SKULL, "SKULL", false},
                {AssetPaths.CHEST, "CHEST", false},
                {AssetPaths.POTION, "POTION", false}
        };

        // Set the coordinates and type for the selected object
        if (selectedObjectIndex >= 0 && selectedObjectIndex < objectDefinitions.length) {
            int[] coords = (int[]) objectDefinitions[selectedObjectIndex][0];
            String type = (String) objectDefinitions[selectedObjectIndex][1];
            newObject.setImageCoords(coords, type);
        }

        placedObjectsGrid[gridRow][gridCol] = newObject;
    }

    /**
     * Remove the object from the grid cell, regardless of double-height.
     */
    private void removePlacedObject(int gridRow, int gridCol) {
        PlacedObject obj = placedObjectsGrid[gridRow][gridCol];
        if (obj == null) return;
        placedObjectsGrid[gridRow][gridCol] = null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);  // This line is important for the background color
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

    /**
     * Draws the placed objects. Double-height objects appear taller but occupy one grid cell logically.
     */
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
                        // Position so that the base is in the current cell
                        drawY -= (drawH - cellSize);
                    }

                    g.drawImage(obj.image, drawX, drawY, drawW, drawH, null);
                }
            }
        }
    }

    public void closeBuildMode() {
        if (parentFrame != null) {
            parentFrame.dispose();
        }
    }

    public static class PlacedObject implements Serializable {
        private static final long serialVersionUID = 1L;
        public transient BufferedImage image; // transient because BufferedImage isn't serializable
        public final int gridRow;
        public final int gridCol;
        public final boolean isDouble;
        public boolean hasRune = false;
        public boolean runeVisible = false;
        private int[] imageCoords; // Store the coordinates from the spritesheet
        private String objectType; // Add this to store the type of object

        public PlacedObject(BufferedImage image, int gridRow, int gridCol, boolean isDouble) {
            this.image = image;
            this.gridRow = gridRow;
            this.gridCol = gridCol;
            this.isDouble = isDouble;
        }

        public void setImageCoords(int[] coords, String type) {
            this.imageCoords = coords;
            this.objectType = type;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            try {
                URL resourceUrl = getClass().getClassLoader().getResource(AssetPaths.SPRITESHEET.substring(1));
                if (resourceUrl != null && imageCoords != null) {
                    BufferedImage spriteSheet = ImageIO.read(resourceUrl);
                    this.image = spriteSheet.getSubimage(
                            imageCoords[0], imageCoords[1], imageCoords[2], imageCoords[3]
                    );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
