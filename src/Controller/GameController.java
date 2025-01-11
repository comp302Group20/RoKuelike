package Controller;

import Domain.Hall;
import UI.BuildModePanel;
import UI.GamePanel;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import Domain.GameTimer;


public class GameController {
    private Hall hall;
    private GameTimer gameTimer;

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

        int totalObjects = allObjects.size();
        int startingTime = calculateStartingTime(totalObjects);

        startPlayMode(grid, placedObjects, startingTime);
    }

    private int calculateStartingTime(int totalObjects) {
        int baseTimePerObject = 5; // Time (in seconds) allocated per object
        return totalObjects * baseTimePerObject;
    }

    private void startPlayMode(BuildModePanel.CellType[][] grid, BuildModePanel.PlacedObject[][] placedObjects, int startingTime) {
        JFrame playModeFrame = new JFrame("Play Mode - " + hall.getName());
        playModeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        playModeFrame.setSize(1600, 900);
        playModeFrame.setLocationRelativeTo(null);

        GamePanel gamePanel = new GamePanel(grid, placedObjects, this);
        gameTimer = new GameTimer(startingTime); // Use dynamic starting time

        gameTimer.start(
                () -> gamePanel.updateTime(gameTimer.getTimeRemaining()), // Update UI
                () -> gamePanel.triggerGameOver() // Trigger game over
        );

        playModeFrame.add(gamePanel, BorderLayout.CENTER);
        playModeFrame.setVisible(true);
    }



    public void pauseGame() {
        if (gameTimer != null) {
            gameTimer.pause();
        }
    }

    public void resumeGame() {
        if (gameTimer != null) {
            gameTimer.resume();
        }
    }
}
