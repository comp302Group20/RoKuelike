package Domain;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Abstract Monster class providing basic monster fields and rendering.
 * Subclasses (ArcherMonster, FighterMonster, WizardMonster, etc.) override update().
 */
public abstract class Monster {
    protected int x, y;
    protected int width = 64;   // Adjust to match cellSize in GamePanel
    protected int height = 64;  // Adjust to match cellSize in GamePanel
    protected Image monsterImage;

    public Monster(int startX, int startY, String imagePath) {
        this.x = startX;
        this.y = startY;
        loadImage(imagePath);
    }

    private void loadImage(String imagePath) {
        try {
            // Because imagePath might start with "/", strip it if needed:
            String pathForResource = imagePath.startsWith("/") ? imagePath.substring(1) : imagePath;
            URL resourceUrl = getClass().getClassLoader().getResource(pathForResource);
            if (resourceUrl == null) {
                throw new IOException("Monster image not found: " + imagePath);
            }
            BufferedImage loadedImage = ImageIO.read(resourceUrl);
            monsterImage = loadedImage;
        } catch (IOException e) {
            System.err.println("Error loading monster image: " + e.getMessage());
            monsterImage = createFallbackImage();
        }
    }

    /**
     * Subclasses must define how they update, e.g. movement logic, attacking, etc.
     */
    public abstract void update();

    /**
     * Draw the monster at (x, y).
     */
    public void draw(Graphics g) {
        g.drawImage(monsterImage, x, y, width, height, null);
    }

    /**
     * Creates a simple red fallback image if loading fails.
     */
    private BufferedImage createFallbackImage() {
        BufferedImage fallback = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = fallback.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return fallback;
    }

    // Getters & Setters
    public int getX() { return x; }
    public int getY() { return y; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
