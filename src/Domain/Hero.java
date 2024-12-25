package Domain;

import javax.swing.ImageIcon;
import java.awt.*;
import java.util.Objects;

/**
 * Represents the hero as an (x, y) pixel position with a certain size and an image.
 * The hero's image path is now internally defined here, rather than passed in.
 */
public class Hero {
    // The hero’s image path is now built-in, rather than relying on an external constant.
    private static final String HERO_IMAGE_PATH = "/resources/Assets/player.png";

    private int x;
    private int y;
    private int width;
    private int height;
    private Image heroImage;

    /**
     * Create a Hero at (startX, startY) with the given width and height,
     * automatically loading the hero’s internal image.
     */
    public Hero(int startX, int startY, int width, int height) {
        this.x = startX;
        this.y = startY;
        this.width = width;
        this.height = height;

        try {
            // heroImage = new ImageIcon(Objects.requireNonNull(getClass().getClassLoader().getResource(HERO_IMAGE_PATH))).getImage();
            heroImage = new ImageIcon(getClass().getResource(HERO_IMAGE_PATH)).getImage();
        } catch (Exception e) {
            System.err.println("Hero image not found! Using fallback.");
            // heroImage = createFallbackImage();
        }
    }

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public void draw(Graphics g) {
        g.drawImage(heroImage, x, y, width, height, null);
    }

    public void setPosition(int newX, int newY) {
        x = newX;
        y = newY;
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    /**
     * Creates a simple fallback red square if loading fails.
     */
    private Image createFallbackImage() {
        Image fallback = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) fallback.getGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return fallback;
    }
}
