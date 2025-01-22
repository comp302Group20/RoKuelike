package UI;

import Utils.AssetPaths;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class PausePopUp extends JDialog {
    private JPanel mainPanel;
    private static final int DIALOG_WIDTH = 300;
    private static final int DIALOG_HEIGHT = 200;

    public PausePopUp(JFrame owner) {
        super(owner, "Game Paused", true);
        initializeUI();
    }

    private void initializeUI() {
        // Set up the dialog
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(getOwner());
        setUndecorated(true); // Remove window decorations
        setBackground(new Color(0, 0, 0, 0)); // Make dialog background transparent

        // Create main panel with transparency
        mainPanel = new TransparentPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));

        // Load the PNG image using AssetPaths
        String imagePath = AssetPaths.PAUSED_INDICATOR;
        ImageIcon originalIcon = new ImageIcon(getClass().getResource(imagePath));

        if (originalIcon.getImage() == null) {
            System.err.println("ERROR: Failed to load image from " + imagePath);
        }

        // Scale the image
        Image scaledImage = originalIcon.getImage().getScaledInstance(
                DIALOG_WIDTH - 40, // Leave some padding
                DIALOG_HEIGHT - 60, // Leave space for button
                Image.SCALE_SMOOTH
        );
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        // Add the scaled image to a JLabel
        JLabel imageLabel = new JLabel(scaledIcon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(imageLabel, BorderLayout.CENTER);

        // Create resume button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        JButton resumeButton = new JButton("Resume");
        resumeButton.setPreferredSize(new Dimension(100, 30));
        resumeButton.addActionListener(e -> setVisible(false));
        buttonPanel.add(resumeButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Make sure dialog can't be closed with X button
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                setVisible(false);
            }
        });

        // Make sure the dialog is modal
        setModalityType(ModalityType.APPLICATION_MODAL);
    }

    // Inner class for transparent panel with overlay effect
    private class TransparentPanel extends JPanel {
        public TransparentPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();

            // Set up the overlay
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.dispose();
        }
    }
}