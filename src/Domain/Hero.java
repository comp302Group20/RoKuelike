package Domain;

import Utils.AssetPaths;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;

/**
 * Singleton Hero class with serialization logic similar to Monster classes.
 */
public class Hero implements Serializable {
    private static final long serialVersionUID = 1L;

    // -- Singleton instance --
    private static Hero instance;

    // -- Basic properties --
    private int x, y;        // Position in pixels
    private int width, height;
    private int health;

    // -- For reloading images post-serialization --
    private String imagePath = AssetPaths.HERO;  // The path to the hero image
    private transient BufferedImage heroImage;          // The actual hero image
    private transient BufferedImage mirroredHeroImage;  // Mirrored version, if you have facing-left logic

    private boolean facingLeft = false;  // If your hero can face left/right

    private Inventory inventory;

    /**
     * Private constructor for singleton pattern.
     *
     * @param x      Starting x-position in pixels
     * @param y      Starting y-position in pixels
     * @param width  Width of hero sprite
     * @param height Height of hero sprite
     */
    public Hero(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.health = 3; // Or your chosen default
        this.inventory = new Inventory(); // Initialize inventory
        loadImage(imagePath);
    }

    /**
     * Public method to get (or create) the singleton instance.
     */
    public static Hero getInstance(int x, int y, int width, int height) {
        if (instance == null) {
            instance = new Hero(x, y, width, height);
        } else {
            // Update position if getInstance is called with new coordinates
            instance.setPosition(x, y);
        }
        return instance;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
        System.out.println("Hero position set to: x=" + x + ", y=" + y); // Debug print
    }

    /**
     * Overload if you need a no-arg getInstance (optional).
     */
    public static Hero getInstance() {
        if (instance == null) {
            instance = new Hero(0, 0, 64, 64); // Default values
        }
        return instance;
    }

    /**
     * Resets the singleton, useful when loading a new game.
     */
    public static void reset() {
        instance = null;
    }

    // ---------------------------------------------------------
    //             ADD THIS METHOD TO FIX THE ERROR:
    // ---------------------------------------------------------
    /**
     * Moves the hero by dx, dy pixels.
     */
    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }

    // ---------------------------------------------------------
    //              IMAGE LOADING & MIRRORING
    // ---------------------------------------------------------

    /**
     * Loads the hero image and creates a mirrored version for left-facing.
     *
     * @param path Path to the hero's image within your resources.
     */
    private void loadImage(String path) {
        try {
            String normalizedPath = path.startsWith("/") ? path.substring(1) : path;
            URL resource = getClass().getClassLoader().getResource(normalizedPath);
            if (resource == null) {
                throw new IOException("Hero image not found at: " + path);
            }
            heroImage = ImageIO.read(resource);

            mirroredHeroImage = mirrorImage(heroImage);
        } catch (IOException e) {
            heroImage = fallback();
            mirroredHeroImage = fallback();
            System.err.println("Failed to load hero image: " + e.getMessage());
        }
    }

    /**
     * Creates a mirrored version (horizontal flip) of the given image.
     */
    private BufferedImage mirrorImage(BufferedImage original) {
        BufferedImage mirrored = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                original.getType()
        );
        Graphics2D g2d = mirrored.createGraphics();
        // Flip horizontally
        g2d.drawImage(
                original,
                0, 0, original.getWidth(), original.getHeight(),
                original.getWidth(), 0, 0, original.getHeight(),
                null
        );
        g2d.dispose();
        return mirrored;
    }

    /**
     * Fallback image in case loading fails (just a solid-red rectangle).
     */
    private BufferedImage fallback() {
        BufferedImage fb = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = fb.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return fb;
    }

    // ---------------------------------------------------------
    //       CUSTOM DESERIALIZATION (to reload images)
    // ---------------------------------------------------------

    /**
     * Custom method called during deserialization. Reloads transient images.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();    // 1) Deserialize normal fields
        loadImage(imagePath);      // 2) Reload the hero image from file
    }

    // ---------------------------------------------------------
    //                     DRAWING LOGIC
    // ---------------------------------------------------------

    /**
     * Draw the hero on-screen.
     *
     * @param g The Graphics context
     */
    public void draw(java.awt.Graphics g) {
        if (g == null) return;
        BufferedImage toDraw = facingLeft ? mirroredHeroImage : heroImage;
        g.drawImage(toDraw, x, y, width, height, null);
    }

    // ---------------------------------------------------------
    //                 GETTERS AND SETTERS
    // ---------------------------------------------------------

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    public int getHealth() {
        return health;
    }
    public void setHealth(int health) {
        this.health = health;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }
    public void setFacingLeft(boolean facingLeft) {
        this.facingLeft = facingLeft;
    }

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
