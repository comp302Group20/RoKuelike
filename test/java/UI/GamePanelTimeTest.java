package UI;

import Domain.Hall;
import Domain.GameTimer;
import Controller.GameController;
import UI.BuildModePanel.CellType;
import UI.BuildModePanel.PlacedObject;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GamePanelTimeTest {
    private GamePanel gamePanel;
    private GameController controller;
    private TestGameTimer testTimer;
    private static final int GRID_ROWS = 13;
    private static final int GRID_COLS = 13;
    private static final int INITIAL_TIME = 45;
    private static final double DELTA = 0.1; // Tolerance for floating point comparison

    private class TestGameTimer extends GameTimer {
        private int currentTime;

        public TestGameTimer(int time) {
            super(time);
            this.currentTime = time;
        }

        @Override
        public int getTimeRemaining() {
            return currentTime;
        }

        public void setTimeRemaining(int time) {
            this.currentTime = time;
        }

        @Override
        public void start(Runnable onTick, Runnable onFinish) {
            // Do nothing - prevent timer from actually starting
        }
    }

    @Before
    public void setUp() {
        // Initialize the grid
        CellType[][] grid = new CellType[GRID_ROWS][GRID_COLS];
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                grid[i][j] = CellType.FLOOR;
            }
        }

        // Initialize placed objects array
        PlacedObject[][] placedObjects = new PlacedObject[GRID_ROWS][GRID_COLS];

        // Create a test hall
        Hall testHall = new Hall("Test Hall", GRID_ROWS, GRID_COLS, 6);

        // Create our test timer
        testTimer = new TestGameTimer(INITIAL_TIME);

        // Create controller with our test timer
        controller = new GameController(testHall) {
            @Override
            public GameTimer getGameTimer() {
                return testTimer;
            }

            @Override
            public int getStartingTime() {
                return INITIAL_TIME;
            }

            @Override
            public int getTimeRemaining() {
                return testTimer.getTimeRemaining();
            }
        };

        // Create GamePanel instance
        gamePanel = new GamePanel(grid, placedObjects, controller);
    }

    /**
     * Test Case 1: Full time remaining
     * Requires: Game just started with full time
     * Effects: Returns ratio of 1.0 for full time
     */
    @Test
    public void testFullTimeRatio() {
        // Ensure we have full time
        testTimer.setTimeRemaining(INITIAL_TIME);

        double ratio = gamePanel.getTimeRatio();
        assertEquals("Time ratio should be 1.0 at start", 1.0, ratio, DELTA);
    }

    /**
     * Test Case 2: Half time remaining
     * Requires: Half of the time has passed
     * Effects: Returns ratio of 0.5 for half time
     */
    @Test
    public void testHalfTimeRatio() {
        // Set exactly half time remaining
        testTimer.setTimeRemaining(INITIAL_TIME / 2);

        double ratio = gamePanel.getTimeRatio();
        assertEquals("Time ratio should be 0.5 at half time", 0.5, ratio, DELTA);
    }

    /**
     * Test Case 3: No time remaining
     * Requires: Time has run out
     * Effects: Returns ratio of 0.0 for no time
     */
    @Test
    public void testNoTimeRatio() {
        // Set time to zero
        testTimer.setTimeRemaining(0);

        double ratio = gamePanel.getTimeRatio();
        assertEquals("Time ratio should be 0.0 when time is up", 0.0, ratio, DELTA);
    }
}