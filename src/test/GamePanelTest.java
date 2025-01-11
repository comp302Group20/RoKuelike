package test;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

public class GamePanelTest {

    private GamePanel gamePanel;
    private BuildModePanel.CellType[][] testGrid;
    private PlacedObject[][] testPlacedObjects;
    private GameController mockController;

    @Before
    public void setUp() {
        // Create a small grid (3x3) for testing; fill it with FLOOR except for one wall
        testGrid = new BuildModePanel.CellType[3][3];
        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                testGrid[r][c] = BuildModePanel.CellType.FLOOR;
            }
        }
        // Place a wall in the center
        testGrid[1][1] = BuildModePanel.CellType.WALL;

        // No placed objects for simplicity (except the door cell, which is optional here)
        testPlacedObjects = new PlacedObject[3][3];

        // We can mock or just instantiate a trivial GameController
        mockController = new GameController();
        
        // Create the GamePanel with the small grid
        // (The row/col counts in GamePanel are 13, but for a test we can adapt or mock necessary parts)
        gamePanel = new GamePanel(testGrid, testPlacedObjects, mockController);
        
        // Clear out the actual 13x13 references if needed, or rely on the partial setup.
        // For the sake of demonstration, we assume the constructor won't break with a 3x3.
        // Alternatively, you could override the GamePanel fields directly in test.
        
        // Reset the monster list to empty
        gamePanel.monsters = new ArrayList<>();
        // Optionally position the hero at (0,0)
        gamePanel.hero.setPosition(0, 0);
    }

    @Test
    public void testMonsterCannotMoveIntoWall() {
        // Suppose we have a monster at (0, 64) (just below top-left cell).
        Monster testMonster = new FighterMonster(0, 64, gamePanel.hero, testGrid, gamePanel);
        gamePanel.monsters.add(testMonster);

        // Attempt to move into the wall cell (1,1) => in pixel coords: (64, 64)
        boolean canMove = gamePanel.canMonsterMove(testMonster, 64, 64);

        assertFalse("Monster should NOT move into a wall cell", canMove);
    }

    @Test
    public void testMonsterCannotMoveIntoHeroLocation() {
        // Suppose we have a monster at (64,0)
        Monster testMonster = new ArcherMonster(64, 0, gamePanel.hero, testGrid, gamePanel);
        gamePanel.monsters.add(testMonster);

        // The hero is at (0,0); check if monster can move there => should be false
        boolean canMove = gamePanel.canMonsterMove(testMonster, gamePanel.hero.getX(), gamePanel.hero.getY());

        assertFalse("Monster should NOT move onto hero's cell", canMove);
    }

    @Test
    public void testMonsterCanMoveValidFloor() {
        // Place monster at top-left cell. The right cell is floor, not a wall, not occupied.
        Monster testMonster = new WizardMonster(0, 0, gamePanel.hero, testGrid, gamePanel);
        gamePanel.monsters.add(testMonster);

        // Move from (0,0) to (64,0) in pixel coordinates => grid cell (0,1)
        boolean canMove = gamePanel.canMonsterMove(testMonster, 64, 0);

        assertTrue("Monster should be able to move to an empty floor cell", canMove);
    }
}
