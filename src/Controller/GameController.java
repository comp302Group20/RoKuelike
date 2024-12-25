package Controller;

import Domain.Hall;
import UI.BuildModePanel;
import UI.GamePanel;

import javax.swing.*;
import java.awt.*;

/**
 * The overall controller that manages both Build Mode and Play Mode for a Hall.
 * You can later expand this to manage multiple Halls or more complex game flows.
 */
public class GameController {

    private Hall hall;

    public GameController(Hall hall) {
        this.hall = hall;
        // Start by launching Build Mode
        new BuildModeController(hall, this);
    }

    /**
     * Called by the BuildModeController after finishing building
     * if the hall meets the minimum object requirement.
     */
    public void onBuildModeFinished(BuildModePanel.CellType[][] grid, BuildModePanel.PlacedObject[][] placedObjects) {
        // Now launch Play Mode in a new window
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
