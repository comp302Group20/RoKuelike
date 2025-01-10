package Domain;

/**
 * Represents one Hall (or one “room”).
 * Has a grid with row × col and a minimum object count requirement.
 */
public class Hall {
    private final String name;
    private final int rows, cols;
    private GridCell[][] grid;

    /**
     * The minimum number of objects that must be placed in this hall.
     */
    private final int minObjectCount;

    public Hall(String name, int rows, int cols, int minObjectCount) {
        this.name = name;
        this.rows = rows;
        this.cols = cols;
        this.minObjectCount = minObjectCount;

        // Initialize the grid
        grid = new GridCell[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c] = new GridCell(r, c);
            }
        }
    }

    /**
     * Attempt to add an object at (row, col).
     * Note: This is not currently called by the build panel,
     * but is an example of how you'd increment object count if integrated.
     */
    public boolean addObject(int row, int col, GameObject object) {
        // Check boundaries
        if (row < 0 || row >= rows || col < 0 || col >= cols) return false;

        // Check if cell is occupied
        if (grid[row][col].isOccupied()) return false;

        // Place object
        grid[row][col].placeObject(object);
        return true;
    }

    /**
     * Validates that the hall has at least the required number of objects.
     * @param placedObjects the number of objects currently placed
     * @return true if placedObjects >= minObjectCount; otherwise false
     */
    public boolean validateObjectCount(int placedObjects) {
        return placedObjects >= minObjectCount;
    }

    public String getName() {
        return name;
    }

    public GridCell[][] getGrid() {
        return grid;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public int getMinObjectCount() {
        return minObjectCount;
    }
}
