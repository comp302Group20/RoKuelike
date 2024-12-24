package Controller;

import Domain.Hall;
import UI.BuildModePanel;

import javax.swing.*;
import java.awt.*;

public class GameController {
    private JFrame frame;
    private BuildModePanel buildModePanel;

    public GameController(Hall hall) {
        // Create a new JFrame for Build Mode
        frame = new JFrame("Rokue-Like - Build Mode: " + hall.getName());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);

        buildModePanel = new BuildModePanel(hall);
        frame.setLayout(new BorderLayout());
        frame.add(buildModePanel, BorderLayout.CENTER);

        // Example "Finish Build" button to proceed (if you want to switch to Play Mode later)
        JButton finishBuildBtn = new JButton("Finish Building");
        finishBuildBtn.addActionListener(e -> {
            if (hall.validateObjectCount()) {
                JOptionPane.showMessageDialog(frame, "Hall is ready! Switch to Play Mode here.");
                // TODO: Play mode implementation here
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Not enough objects in " + hall.getName() + "!");
            }
        });

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(finishBuildBtn);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }
}
