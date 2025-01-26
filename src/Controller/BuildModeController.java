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
    /**
     * Constructs a BuildModeController for editing the specified hall, with a reference to the parent controller and an existing inventory.
     * @param hall the Hall object to be edited
     * @param parentController the main GameController instance
     * @param previousInventory the Inventory to carry over, if any
     */
    public BuildModeController(Hall hall, GameController parentController, Inventory previousInventory) {
        this.hall = hall;
        this.parentController = parentController;
        this.previousInventory = previousInventory;
        initializeUI();
    }

    // Keep the old constructor for compatibility
    /**
     * Constructs a BuildModeController for editing the specified hall, with a reference to the parent controller.
     * @param hall the Hall object to be edited
     * @param parentController the main GameController instance
     */
    public BuildModeController(Hall hall, GameController parentController) {
        this(hall, parentController, null);
    }

    /**
     * Sets up the UI components and lays out the build panel along with its bottom panel buttons.
     */
    private void initializeUI() {
        frame = new JFrame("Build Mode - " + hall.getName());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1000, 900);
        frame.setLocationRelativeTo(null);
        buildPanel = new BuildModePanel(hall, parentController, frame);
        frame.setLayout(new BorderLayout());
        frame.add(buildPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(255, 255, 255));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton finishBuildBtn = new JButton("Finish Building");
        finishBuildBtn.setFont(Utils.GameFonts.pixelFont.deriveFont(16f));
        finishBuildBtn.setPreferredSize(new Dimension(150, 40));
        finishBuildBtn.setBackground(new Color(34, 139, 34));  // Forest Green
        finishBuildBtn.setForeground(Color.WHITE);
        finishBuildBtn.setFocusPainted(false);
        finishBuildBtn.setBorderPainted(false);
        finishBuildBtn.setOpaque(true);

        finishBuildBtn.addActionListener(e -> {
            int numberOfObjects = buildPanel.getNumberOfPlacedObjects();

            if (hall.validateObjectCount(numberOfObjects)) {
                parentController.onBuildModeFinished(
                        buildPanel.getGrid(),
                        buildPanel.getPlacedObjectsGrid(),
                        previousInventory
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

    /**
     * Retrieves the current BuildModePanel instance used by this controller.
     * @return the BuildModePanel object
     */
    public BuildModePanel getBuildPanel() {
        return buildPanel;
    }

    /**
     * Closes the build mode window if it is open and disposes of its resources.
     */
    public void closeBuildMode() {
        if (frame != null) {
            frame.setVisible(false);
            frame.dispose();
        }
    }
}
