package Domain;

import Utils.AssetPaths;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Represents a single enchantment that appears on the board.
 */
public class Enchantment {

    private int x, y;
    private int width, height;
    private EnchantmentType type;
    private BufferedImage image;

    // New field to track the time when this enchantment spawned
    private long spawnTime;

    public Enchantment(int x, int y, int width, int height, EnchantmentType type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.spawnTime = System.currentTimeMillis(); // Mark spawn time
        loadImage();
    }

    private void loadImage() {
        // Load the correct image based on the enchantment type
        String path;
        switch (type) {
            case REVEAL:       path = AssetPaths.REVEAL_ENCH;       break;
            case CLOAK:        path = AssetPaths.CLOAK_ENCH;        break;
            case LURINGGEM:    path = AssetPaths.LURING_ENCH;       break;
            case EXTRATIME:    path = AssetPaths.EXTRATIME_ENCH;    break;
            case EXTRALIFE:    path = AssetPaths.EXTRALIFE_ENCH;    break;
            default:           path = AssetPaths.REVEAL_ENCH;       break;
        }

        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                image = ImageIO.read(url);
            }
        } catch (IOException e) {
            image = null;
        }
    }

    /**
     * Checks if this enchantment has expired (older than 6 seconds).
     * @return true if older than 6 seconds, false otherwise.
     */
    public boolean isExpired() {
        return (System.currentTimeMillis() - spawnTime) > 6000; // 6 seconds
    }

    /**
     * Returns the time at which the enchantment was spawned.
     */
    public long getSpawnTime() {
        return spawnTime;
    }

    public void draw(Graphics g) {
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            // Fallback if image not found
            g.setColor(Color.MAGENTA);
            g.fillRect(x, y, width, height);
        }
    }

    // New getter for the image, so inventory can draw it
    public BufferedImage getImage() {
        return image;
    }

    // Getters for bounding box checks
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public EnchantmentType getType() {
        return type;
    }
}
