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

    /**
     * Constructs a BuildModePanel, initializing the grid, loading images and objects, and setting up UI components.
     * @param hall the current Hall instance
     * @param controller the GameController managing this panel
     * @param frame the parent JFrame containing this panel
     */
    public BuildModePanel(Hall hall, GameController controller, JFrame frame) {
        currentHall = hall;
        this.gameController = controller;
        this.parentFrame = frame;

        grid = new CellType[GRID_ROWS][GRID_COLS];
        placedObjectsGrid = new PlacedObject[GRID_ROWS][GRID_COLS];
        setPreferredSize(new Dimension(1000, 900));
        setLayout(null);

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
                    }
                } else {
                    // Clicked outside the grid; exit placement mode
                    deselectCurrentObject();
                }
            }
        });
    }

    /**
     * Retrieves the 2D array of cell types (FLOOR or WALL).
     * @return a 2D array representing the cell layout
     */
    public CellType[][] getGrid() {
        return grid;
    }

    /**
     * Provides the 2D array of placed objects within the grid.
     * @return a 2D array of PlacedObject instances
     */
    public PlacedObject[][] getPlacedObjectsGrid() {
        return placedObjectsGrid;
    }

    /**
     * Counts and returns how many objects have been placed on the grid.
     * @return the number of placed objects
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
     * Initializes the grid, setting all cells to FLOOR and placing WALL cells around the perimeter.
     */
    private void initializeGrid() {
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                grid[r][c] = CellType.FLOOR;
            }
        }
        for (int r = 1; r <= 11; r++) {
            grid[r][0] = CellType.WALL;
            grid[r][GRID_COLS - 1] = CellType.WALL;
        }
        for (int c = 1; c <= 11; c++) {
            grid[1][c] = CellType.WALL;
            grid[11][c] = CellType.WALL;
        }
    }

    /**
     * Loads and extracts the required images for floor and wall tiles from the sprite sheet.
     */
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

    /**
     * Creates a fallback image for display if loading any of the required images fails.
     * @return a default BufferedImage
     */
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

    /**
     * Initializes the UI components, including the panel that displays available objects for placement.
     */
    private void initializeUI() {
        objectColumnPanel = new JPanel();
        objectColumnPanel.setBounds(832 + 10, 0, 150, 900);
        objectColumnPanel.setLayout(new BoxLayout(objectColumnPanel, BoxLayout.Y_AXIS));
        objectColumnPanel.setBorder(BorderFactory.createTitledBorder("Available Objects"));
        objectColumnPanel.setBackground(new Color(255, 165, 0));
        add(objectColumnPanel);
    }

    /**
     * Loads predefined objects from the sprite sheet and populates the side panel with them.
     */
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

    /**
     * Creates a panel to display each available object. Sets up a click listener to handle selection or deselection.
     * @param objectImage the BufferedImage of the object
     * @param isDouble whether the object is double height
     * @param index the index used to reference this object's details
     * @return a JPanel containing the object's scaled image
     */
    private JPanel createObjectPanel(BufferedImage objectImage, boolean isDouble, int index) {
        JPanel objectPanel = new JPanel(new BorderLayout());
        objectPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        int h = isDouble ? (cellSize * 2) : cellSize;
        Image scaledImage = objectImage.getScaledInstance(cellSize, h, Image.SCALE_SMOOTH);
        JLabel objectLabel = new JLabel(new ImageIcon(scaledImage));
        objectPanel.add(objectLabel, BorderLayout.CENTER);
        objectPanel.setMaximumSize(new Dimension(230, h + 10));
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

    /**
     * Selects an object for placement by updating the UI to highlight it.
     * @param index the index in the objects list corresponding to the chosen object
     */
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

    /**
     * Deselects any currently selected object for placement, resetting the UI highlight.
     */
    private void deselectCurrentObject() {
        if (selectedObjectIndex >= 0 && selectedObjectIndex < objectPanels.size()) {
            JPanel curr = objectPanels.get(selectedObjectIndex);
            curr.setBackground(null);
            curr.setOpaque(false);
            curr.repaint();
        }
        selectedObjectIndex = -1;
    }

    /**
     * Checks if the specified cell is a wall.
     * @param r the row index
     * @param c the column index
     * @return true if the cell is a wall, false otherwise
     */
    private boolean isWallCell(int r, int c) {
        return grid[r][c] == CellType.WALL;
    }

    /**
     * Places the currently selected object into the grid at the specified cell, if possible.
     * @param gridRow the row index for placement
     * @param gridCol the column index for placement
     */
    private void placeObject(int gridRow, int gridCol) {
        BufferedImage selImage = availableObjects.get(selectedObjectIndex);
        boolean selDouble = isDoubleHeight.get(selectedObjectIndex);

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

        if (placedObjectsGrid[gridRow][gridCol] != null) {
            JOptionPane.showMessageDialog(BuildModePanel.this,
                    "Cannot place object here. Space is already occupied.",
                    "Placement Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PlacedObject newObject = new PlacedObject(selImage, gridRow, gridCol, selDouble);

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

        if (selectedObjectIndex >= 0 && selectedObjectIndex < objectDefinitions.length) {
            int[] coords = (int[]) objectDefinitions[selectedObjectIndex][0];
            String type = (String) objectDefinitions[selectedObjectIndex][1];
            newObject.setImageCoords(coords, type);
        }

        placedObjectsGrid[gridRow][gridCol] = newObject;
    }

    /**
     * Removes any placed object found at the specified grid cell.
     * @param gridRow the row index of the object
     * @param gridCol the column index of the object
     */
    private void removePlacedObject(int gridRow, int gridCol) {
        PlacedObject obj = placedObjectsGrid[gridRow][gridCol];
        if (obj == null) return;
        placedObjectsGrid[gridRow][gridCol] = null;
    }

    /**
     * Paints the panel by first drawing the board tiles, then the grid lines, and finally the placed objects.
     * @param g the Graphics context used for drawing
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawBoard(g);
        drawGridLines(g);
        drawPlacedObjects(g);
    }

    /**
     * Draws the floor and wall tiles for each cell in the grid.
     * @param g the Graphics context used for drawing
     */
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

    /**
     * Draws faint grid lines over the board to help visualize individual cells.
     * @param g the Graphics context used for drawing
     */
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
     * Draws all placed objects, accommodating double-height objects' visual positioning.
     * @param g the Graphics context used for drawing
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
                        drawY -= (drawH - cellSize);
                    }
                    g.drawImage(obj.image, drawX, drawY, drawW, drawH, null);
                }
            }
        }
    }

    /**
     * Closes the BuildMode by disposing of the parent frame if it exists.
     */
    public void closeBuildMode() {
        if (parentFrame != null) {
            parentFrame.dispose();
        }
    }

    /**
     * A nested class representing a placed object on the grid, containing image data and positioning info.
     */
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

        /**
         * Constructs a PlacedObject with the given image, grid position, and whether it's double height.
         * @param image the BufferedImage for this object
         * @param gridRow the row index in the grid
         * @param gridCol the column index in the grid
         * @param isDouble whether the object is double-height
         */
        public PlacedObject(BufferedImage image, int gridRow, int gridCol, boolean isDouble) {
            this.image = image;
            this.gridRow = gridRow;
            this.gridCol = gridCol;
            this.isDouble = isDouble;
        }

        /**
         * Assigns the sprite sheet coordinates and object type for reloading post-serialization.
         * @param coords the array of coordinates in the sprite sheet
         * @param type the type of object
         */
        public void setImageCoords(int[] coords, String type) {
            this.imageCoords = coords;
            this.objectType = type;
        }

        /**
         * Custom serialization logic for this object.
         * @param out the output stream for serialization
         * @throws IOException if an I/O error occurs
         */
        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
        }

        /**
         * Custom deserialization logic to reload the image from the sprite sheet.
         * @param in the input stream for deserialization
         * @throws IOException if an I/O error occurs
         * @throws ClassNotFoundException if a class cannot be located
         */
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
