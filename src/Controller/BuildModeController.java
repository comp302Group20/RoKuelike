package Controller;

import Domain.Hall;
import UI.BuildModePanel;

import javax.swing.*;
import java.awt.*;

/**
 * Controls the Build Mode UI for a given Hall.
 * When finished building, it notifies the GameController so that
 * we can switch to Play Mode if the minimum object count is satisfied.
 */
public class BuildModeController {

    private JFrame frame;
    private BuildModePanel buildPanel;
    private Hall hall;
    private GameController parentController; // Reference to the parent (the overall game controller)

    public BuildModeController(Hall hall, GameController parentController) {
        this.hall = hall;
        this.parentController = parentController;
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Build Mode - " + hall.getName());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1600, 900);
        frame.setLocationRelativeTo(null);

        buildPanel = new BuildModePanel(hall);
        frame.setLayout(new BorderLayout());
        frame.add(buildPanel, BorderLayout.CENTER);

        // Button to finish building
        JButton finishBuildBtn = new JButton("Finish Building");
        finishBuildBtn.addActionListener(e -> {
            if (hall.validateObjectCount()) {
                // Close this window
                frame.dispose();
                // Notify the parent controller that Build Mode finished successfully
                parentController.onBuildModeFinished(buildPanel.getGrid(), buildPanel.getPlacedObjectsGrid());
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Not enough objects in " + hall.getName() + "!\n" +
                                "You need at least " + hall.getMinObjectCount());
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(finishBuildBtn);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }
}
