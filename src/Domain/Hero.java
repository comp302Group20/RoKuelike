package Domain;

import Utils.AssetPaths;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;
import java.awt.AlphaComposite;

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

    // ---------------------------------------------------------
    // Fields to enable the red "damage" flash effect
    // ---------------------------------------------------------
    private transient BufferedImage damageHeroImage;
    private boolean showingDamageEffect = false;
    private long damageEffectStartTime = 0;
    private static final int DAMAGE_EFFECT_DURATION = 50; // ~50ms quick flash

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
            // Important: Actually update the position
            instance.x = x;
            instance.y = y;
            instance.width = width;
            instance.height = height;
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
    //  MOVEMENT & HEALTH-CHANGE LOGIC
    // ---------------------------------------------------------

    /**
     * Moves the hero by dx, dy pixels.
     */
    public void move(int dx, int dy) {
        // Decide facing direction
        if (dx < 0) {
            facingLeft = true;
        } else if (dx > 0) {
            facingLeft = false;
        }
        this.x += dx;
        this.y += dy;
    }

    /**
     * If health is lowered, trigger the quick red-flash effect.
     */
    public void setHealth(int newHealth) {
        // If the new health is less than the current health, hero took damage
        if (newHealth < this.health) {
            startDamageEffect();
        }
        this.health = newHealth;
    }

    // ---------------------------------------------------------
    //              DAMAGE-EFFECT UTILS
    // ---------------------------------------------------------

    private void startDamageEffect() {
        showingDamageEffect = true;
        damageEffectStartTime = System.currentTimeMillis();

        // If hero is facing left, tint the mirrored image, otherwise tint the normal hero image
        if (facingLeft && mirroredHeroImage != null) {
            damageHeroImage = tintImage(mirroredHeroImage, new Color(255, 0, 0, 100));
        } else {
            damageHeroImage = tintImage(heroImage, new Color(255, 0, 0, 100));
        }
    }

    private BufferedImage tintImage(BufferedImage src, Color color) {
        if (src == null) return null;
        BufferedImage tinted = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tinted.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.setComposite(AlphaComposite.SrcAtop);
        g.setColor(color);
        g.fillRect(0, 0, src.getWidth(), src.getHeight());
        g.dispose();
        return tinted;
    }

    // ---------------------------------------------------------
    //          IMAGE LOADING & MIRRORING
    // ---------------------------------------------------------
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

    private BufferedImage mirrorImage(BufferedImage original) {
        if (original == null) return null;
        BufferedImage mirrored = new BufferedImage(original.getWidth(), original.getHeight(), original.getType());
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

    private BufferedImage fallback() {
        BufferedImage fb = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = fb.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return fb;
    }

    /**
     * Custom method called during deserialization. Reloads transient images.
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();    // 1) Deserialize normal fields
        loadImage(imagePath);      // 2) Reload the hero image from file
    }

    // ---------------------------------------------------------
    //                   DRAWING LOGIC
    // ---------------------------------------------------------
    public void draw(Graphics g) {
        if (heroImage == null) {
            // fallback
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
            return;
        }

        // Decide which image to draw
        BufferedImage imgToDraw;
        if (showingDamageEffect) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - damageEffectStartTime < DAMAGE_EFFECT_DURATION) {
                // Still within the damage flash window
                imgToDraw = (damageHeroImage != null ? damageHeroImage : heroImage);
            } else {
                // Damage effect expired, revert to normal
                showingDamageEffect = false;
                imgToDraw = facingLeft && mirroredHeroImage != null ? mirroredHeroImage : heroImage;
            }
        } else {
            // Normal rendering
            imgToDraw = facingLeft && mirroredHeroImage != null ? mirroredHeroImage : heroImage;
        }

        g.drawImage(imgToDraw, x, y, width, height, null);
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
