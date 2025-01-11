package UI;

import Utils.AssetPaths;

import javax.swing.*;
import java.awt.*;

public class PausePopUp extends JPanel {

    public PausePopUp() {
        setLayout(new BorderLayout());
        setOpaque(false); // Allow transparency for the overlay effect

        // Load the PNG image using AssetPaths
        String imagePath = AssetPaths.PAUSED_INDICATOR; // Replace with the appropriate path
        ImageIcon originalIcon = new ImageIcon(getClass().getResource(imagePath));

        if (originalIcon.getImage() == null) {
            System.err.println("ERROR: Failed to load image from " + imagePath);
        }

        // Scale the image (optional)
        Image scaledImage = originalIcon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
        ImageIcon scaledIcon = new ImageIcon(scaledImage);

        // Add the scaled image to a JLabel
        JLabel imageLabel = new JLabel(scaledIcon);
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(imageLabel, BorderLayout.CENTER);
        revalidate(); 
        repaint();  
        
    }

    @Override
    protected void paintComponent(Graphics g) {
        // Call super to ensure children (like labels) are drawn correctly
        super.paintComponent(g);

        // Create a new graphics context so we don't mess up the original
        Graphics2D g2d = (Graphics2D) g.create();

        // Set a partially transparent black color
        // The alpha value can be from 0 (fully transparent) to 255 (fully opaque)
        g2d.setColor(new Color(0, 0, 0, 150));

        // Fill the entire panel with this color
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // Dispose of the graphics context to free resources
        g2d.dispose();
    }
}
