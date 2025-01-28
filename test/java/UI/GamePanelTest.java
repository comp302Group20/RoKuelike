package UI;

import Domain.Hall;
import Domain.Hero;
import Domain.GameTimer;
import Controller.GameController;
import UI.BuildModePanel.CellType;
import UI.BuildModePanel.PlacedObject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.image.BufferedImage;

public class GamePanelTest {
    private GamePanel gamePanel;
    private CellType[][] grid;
    private PlacedObject[][] placedObjects;
    private static final int GRID_ROWS = 13;
    private static final int GRID_COLS = 13;
    private static final int CELL_SIZE = 64;

    @Before
    public void setUp() {
        // Initialize the grid with all FLOOR cells
        grid = new CellType[GRID_ROWS][GRID_COLS];
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                grid[i][j] = CellType.FLOOR;
            }
        }

        // Initialize placed objects array
        placedObjects = new PlacedObject[GRID_ROWS][GRID_COLS];

        // Create a test hall and controller
        Hall testHall = new Hall("Test Hall", GRID_ROWS, GRID_COLS, 6);
        GameController controller = new GameController(testHall) {
            @Override
            public GameTimer getGameTimer() {
                return new GameTimer(45); // Return a dummy timer
            }
        };

        // Create a dummy image for placed objects
        BufferedImage dummyImage = new BufferedImage(CELL_SIZE, CELL_SIZE, BufferedImage.TYPE_INT_ARGB);

        // Place test objects
        // Single height object at (5,5)
        placedObjects[5][5] = new PlacedObject(dummyImage, 5, 5, false);

        // Double height object at (7,7)
        placedObjects[7][7] = new PlacedObject(dummyImage, 7, 7, true);

        // Create GamePanel instance
        gamePanel = new GamePanel(grid, placedObjects, controller);
    }

    /**
     * Test Case 1: Click on single-height object with hero in range
     */
    @Test
    public void testClickSingleHeightObjectInRange() {
        // Get hero reference and move it next to the object
        Hero hero = gamePanel.getHero();
        hero.setPosition(5 * CELL_SIZE, 4 * CELL_SIZE); // Position hero one cell above object

        // Simulate click in middle of object
        int clickX = 5 * CELL_SIZE + CELL_SIZE/2;
        int clickY = 5 * CELL_SIZE + CELL_SIZE/2;

        // Get clicked object
        PlacedObject clicked = gamePanel.getClickedObject(clickX, clickY);

        // Verify correct object was returned
        assertNotNull("Should return object when clicked in range", clicked);
        assertFalse("Should be single-height object", clicked.isDouble);
    }

    /**
     * Test Case 2: Click on single-height object with hero out of range
     */
    @Test
    public void testClickSingleHeightObjectOutOfRange() {
        // Get hero reference and move it far from object
        Hero hero = gamePanel.getHero();
        hero.setPosition(1 * CELL_SIZE, 1 * CELL_SIZE); // Position hero far from object

        // Simulate click in middle of object
        int clickX = 5 * CELL_SIZE + CELL_SIZE/2;
        int clickY = 5 * CELL_SIZE + CELL_SIZE/2;

        // Get clicked object
        PlacedObject clicked = gamePanel.getClickedObject(clickX, clickY);

        // Verify null was returned due to being out of range
        assertNull("Should return null when clicked out of range", clicked);
    }

    /**
     * Test Case 3: Click on double-height object with hero in range
     */
    @Test
    public void testClickDoubleHeightObjectInRange() {
        // Get hero reference and move it next to the object
        Hero hero = gamePanel.getHero();
        hero.setPosition(7 * CELL_SIZE, 6 * CELL_SIZE); // Position hero one cell above object

        // Simulate click in upper portion of double-height object
        int clickX = 7 * CELL_SIZE + CELL_SIZE/2;
        int clickY = 7 * CELL_SIZE - CELL_SIZE/2; // Click in upper half

        // Get clicked object
        PlacedObject clicked = gamePanel.getClickedObject(clickX, clickY);

        // Verify correct object was returned
        assertNotNull("Should return object when clicked in range", clicked);
        assertTrue("Should be double-height object", clicked.isDouble);
    }

    /**
     * Test Case 4: Click on empty space
     */
    @Test
    public void testClickEmptySpace() {
        // Get hero reference and position it
        Hero hero = gamePanel.getHero();
        hero.setPosition(3 * CELL_SIZE, 3 * CELL_SIZE);

        // Simulate click on empty cell next to hero
        int clickX = 4 * CELL_SIZE + CELL_SIZE/2;
        int clickY = 3 * CELL_SIZE + CELL_SIZE/2;

        // Get clicked object
        PlacedObject clicked = gamePanel.getClickedObject(clickX, clickY);

        // Verify null was returned for empty space
        assertNull("Should return null when clicking empty space", clicked);
    }
}