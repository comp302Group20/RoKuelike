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
}
