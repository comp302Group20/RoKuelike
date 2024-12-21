package Controller;

import Domain.Hall;
import UI.BuildModePanel;

import javax.swing.*;
import java.awt.*;

public class BuildModeController {
    private JFrame frame;
    private BuildModePanel buildPanel;
    private Hall hall;

    public BuildModeController(Hall hall) {
        this.hall = hall;
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Build Mode - " + hall.getName());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1600, 900);
        frame.setLocationRelativeTo(null);

        // Add BuildModePanel to the frame
        buildPanel = new BuildModePanel(hall);

        frame.setLayout(new BorderLayout());
        frame.add(buildPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
