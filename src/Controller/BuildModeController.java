package Controller;

import Domain.Hall;
import Domain.Inventory;
import UI.BuildModePanel;
import javax.swing.*;
import java.awt.*;

public class BuildModeController {
    private JFrame frame;
    private BuildModePanel buildPanel;
    private Hall hall;
    private GameController parentController;
    private Inventory previousInventory;  // Add this field

    // Add new constructor that accepts inventory
    public BuildModeController(Hall hall, GameController parentController, Inventory previousInventory) {
        this.hall = hall;
        this.parentController = parentController;
        this.previousInventory = previousInventory;
        initializeUI();
    }

    // Keep the old constructor for compatibility
    public BuildModeController(Hall hall, GameController parentController) {
        this(hall, parentController, null);
    }

    private void initializeUI() {
        frame = new JFrame("Build Mode - " + hall.getName());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1000, 900);
        frame.setLocationRelativeTo(null);
        buildPanel = new BuildModePanel(hall, parentController, frame);
        frame.setLayout(new BorderLayout());
        frame.add(buildPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(240, 240, 240));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton finishBuildBtn = new JButton("Finish Building");
        finishBuildBtn.setFont(new Font("Arial", Font.BOLD, 16));
        finishBuildBtn.setPreferredSize(new Dimension(150, 40));
        finishBuildBtn.setBackground(new Color(50, 205, 50));
        finishBuildBtn.setForeground(Color.WHITE);

        finishBuildBtn.addActionListener(e -> {
            int numberOfObjects = buildPanel.getNumberOfPlacedObjects();

            if (hall.validateObjectCount(numberOfObjects)) {
                // Pass the previous inventory to the controller
                parentController.onBuildModeFinished(
                        buildPanel.getGrid(),
                        buildPanel.getPlacedObjectsGrid(),
                        previousInventory  // Add this parameter
                );

                frame.setVisible(false);
                frame.dispose();
                System.out.println("Closing the Build Frame!");
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Not enough objects in " + hall.getName() + "!\n" +
                                "You need at least " + hall.getMinObjectCount() + " objects.\n" +
                                "Currently placed: " + numberOfObjects + " objects.",
                        "Insufficient Objects",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        bottomPanel.add(finishBuildBtn);
        frame.add(bottomPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    public BuildModePanel getBuildPanel() {
        return buildPanel;
    }

    public void closeBuildMode() {
        if (frame != null) {
            frame.setVisible(false);
            frame.dispose();
        }
    }
}