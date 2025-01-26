package Domain;

import Utils.AssetPaths;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;

/**
 * Represents an in-game enchantment that can be collected by the hero or stored on the floor.
 */
public class Enchantment implements Serializable {
    private static final long serialVersionUID = 1L;

    private int x, y;
    private int width, height;
    private EnchantmentType type;
    private transient BufferedImage image;
    private long spawnTime;

    /**
     * Constructs a new Enchantment with its position, size, and type. Also records the spawn time.
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param width the width
     * @param height the height
     * @param type the EnchantmentType
     */
    public Enchantment(int x, int y, int width, int height, EnchantmentType type) {
        this(x, y, width, height, type, System.currentTimeMillis());
    }

    /**
     * Constructs a new Enchantment, providing a specific spawnTime (used when loading from saved data).
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @param width the width
     * @param height the height
     * @param type the EnchantmentType
     * @param spawnTime the time the enchantment was created
     */
    public Enchantment(int x, int y, int width, int height, EnchantmentType type, long spawnTime) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        this.spawnTime = spawnTime;
        loadImage();
    }

    /**
     * Converts this enchantment into a serializable EnchantmentState for saving.
     * @return an EnchantmentState object containing this enchantment's data
     */
    public GameState.EnchantmentState toEnchantmentState() {
        return new GameState.EnchantmentState(
                type.name(),
                x,
                y,
                width,
                height,
                spawnTime
        );
    }

    /**
     * Loads the appropriate image resource for this enchantment type.
     */
    private void loadImage() {
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
     * Checks if this enchantment has existed for more than 6 seconds.
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return (System.currentTimeMillis() - spawnTime) > 6000;
    }

    /**
     * Provides the spawn time for this enchantment.
     * @return the time the enchantment was created in milliseconds
     */
    public long getSpawnTime() {
        return spawnTime;
    }

    /**
     * Draws the enchantment at its specified position and size.
     * @param g the Graphics context
     */
    public void draw(Graphics g) {
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            g.setColor(Color.MAGENTA);
            g.fillRect(x, y, width, height);
        }
    }

    /**
     * Retrieves the image for this enchantment.
     * @return the BufferedImage representing the enchantment
     */
    public BufferedImage getImage() {
        return image;
    }

    /**
     * Returns the x-coordinate of the enchantment.
     * @return an integer x-coordinate
     */
    public int getX() { return x; }

    /**
     * Returns the y-coordinate of the enchantment.
     * @return an integer y-coordinate
     */
    public int getY() { return y; }

    /**
     * Returns the width of the enchantment.
     * @return an integer width
     */
    public int getWidth() { return width; }

    /**
     * Returns the height of the enchantment.
     * @return an integer height
     */
    public int getHeight() { return height; }

    /**
     * Gets the type of enchantment.
     * @return the EnchantmentType
     */
    public EnchantmentType getType() {
        return type;
    }
}
