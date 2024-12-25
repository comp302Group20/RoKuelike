package Controller;

import Domain.Hall;
import UI.BuildModePanel;
import UI.GamePanel;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GameController {
    private Hall hall;

    public GameController(Hall hall) {
        this.hall = hall;
        new BuildModeController(hall, this);
    }

    public void onBuildModeFinished(BuildModePanel.CellType[][] grid, BuildModePanel.PlacedObject[][] placedObjects) {
        List<BuildModePanel.PlacedObject> allObjects = new ArrayList<>();
        for (int r = 0; r < placedObjects.length; r++) {
            for (int c = 0; c < placedObjects[0].length; c++) {
                if (placedObjects[r][c] != null) {
                    allObjects.add(placedObjects[r][c]);
                }
            }
        }
        if (!allObjects.isEmpty()) {
            int idx = (int) (Math.random() * allObjects.size());
            allObjects.get(idx).hasRune = true;
        }
        startPlayMode(grid, placedObjects);
    }

    private void startPlayMode(BuildModePanel.CellType[][] grid, BuildModePanel.PlacedObject[][] placedObjects) {
        JFrame playModeFrame = new JFrame("Play Mode - " + hall.getName());
        playModeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        playModeFrame.setSize(1600, 900);
        playModeFrame.setLocationRelativeTo(null);
        GamePanel gamePanel = new GamePanel(grid, placedObjects);
        playModeFrame.add(gamePanel, BorderLayout.CENTER);
        playModeFrame.setVisible(true);
    }
}
