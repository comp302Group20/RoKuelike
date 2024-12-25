package Domain;

/**
 * Represents one Hall (or one “room”).
 * Has a grid with row × col and a minimum object count requirement.
 */
public class Hall {
    private final String name;
    private final int rows, cols;
    private GridCell[][] grid;
    private final int minObjectCount;
    private int currentObjectCount;

    public Hall(String name, int rows, int cols, int minObjectCount) {
        this.name = name;
        this.rows = rows;
        this.cols = cols;
        this.minObjectCount = minObjectCount;
        this.currentObjectCount = 0;

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
     */
    public boolean addObject(int row, int col, GameObject object) {
        // Check boundaries
        if (row < 0 || row >= rows || col < 0 || col >= cols) return false;

        // Check if cell is occupied
        if (grid[row][col].isOccupied()) return false;

        // Place object
        grid[row][col].placeObject(object);
        currentObjectCount++;
        return true;
    }

    public boolean validateObjectCount() {
        return true;
    }

    public String getName() {
        return name;
    }

    public GridCell[][] getGrid() {
        return grid;
    }

    public int getRows() { return rows; }
    public int getCols() { return cols; }
    public int getMinObjectCount() { return minObjectCount; }
}
