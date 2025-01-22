package Domain;

import java.io.Serializable;

/**
 * Simple data structure to store row/col and any placed GameObject.
 */
public class GridCell implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int row, col;
    private GameObject object;

    public GridCell(int row, int col) {
        this.row = row;
        this.col = col;
        this.object = null;
    }

    public void placeObject(GameObject obj) {
        this.object = obj;
    }

    public boolean isOccupied() {
        return object != null;
    }

    public GameObject getObject() {
        return object;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
