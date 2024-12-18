package UI;

import Domain.Hall;
import Domain.GridCell;
import Domain.GameObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class BuildModePanel extends JPanel {
    private Hall currentHall;
    private GameObject selectedObject;
    private Image hallBackground;
    private int cellSize = 64;

    public BuildModePanel(Hall hall) {
        this.currentHall = hall;
        setPreferredSize(new Dimension(512, 512));
        setLayout(null);

        hallBackground = new ImageIcon("res/rokue-like-assets/build-mode.png").getImage();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = e.getX() / cellSize;
                int row = e.getY() / cellSize;

                if (row < 0 || row >= currentHall.getRows() ||
                        col < 0 || col >= currentHall.getCols()) {
                    return;
                }

                if (selectedObject != null) {
                    GameObject newObject = selectedObject.getSpriteLocation() != null ?
                            new GameObject(selectedObject.getName(),
                                    selectedObject.getImagePath(),
                                    selectedObject.getSpriteLocation()) :
                            new GameObject(selectedObject.getName(),
                                    selectedObject.getImagePath());

                    boolean success = currentHall.addObject(row, col, newObject);
                    if (success) {
                        repaint();
                    } else {
                        JOptionPane.showMessageDialog(BuildModePanel.this,
                                "Cell occupied or out of bounds. Try another spot.");
                    }
                }
            }
        });
    }

    public void setSelectedObject(GameObject object) {
        this.selectedObject = object;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (hallBackground != null) {
            g.drawImage(hallBackground, 0, 0, getWidth(), getHeight(), this);
        }

        drawPlacedObjects(g);
    }

    private void drawPlacedObjects(Graphics g) {
        GridCell[][] grid = currentHall.getGrid();
        for (int r = 0; r < currentHall.getRows(); r++) {
            for (int c = 0; c < currentHall.getCols(); c++) {
                if (grid[r][c].isOccupied()) {
                    GameObject obj = grid[r][c].getObject();
                    Image objImage = obj.getImage();
                    int drawX = c * cellSize;
                    int drawY = r * cellSize;
                    g.drawImage(objImage, drawX, drawY, cellSize, cellSize, null);
                }
            }
        }
    }
}