package UI;

import javax.swing.*;
import java.awt.*;

/**
 * A simple JPanel that stretches a background image to fill itself.
 */
public class BackgroundPanel extends JPanel {
    private Image backgroundImage;

    /**
     * Constructs a BackgroundPanel with the specified image.
     * @param backgroundImage the image to use as background
     */
    public BackgroundPanel(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
        setLayout(new BorderLayout());
    }

    /**
     * Paints the component by drawing the background image scaled to fill this panel.
     * @param g the Graphics context for painting
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
