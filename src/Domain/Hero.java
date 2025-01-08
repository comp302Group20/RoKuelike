package Domain;

import Utils.AssetPaths;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.awt.geom.AffineTransform;

public class Hero {
    private static Hero instance = null;
    private int x;
    private int y;
    private int width;
    private int height;
    private BufferedImage heroImage;
    private BufferedImage mirroredHeroImage; // Mirrored image for left-facing
    private BufferedImage damageHeroImage;
    private int health = 3;

    private boolean showingDamageEffect = false;
    private long damageEffectStartTime = 0;
    private static final int DAMAGE_EFFECT_DURATION = 50;

    private boolean facingLeft = false; // Direction state

    // Private constructor
    public Hero(int sx, int sy, int w, int h) {
        x = sx;
        y = sy;
        width = w;
        height = h;
        try {
            URL url = getClass().getResource(AssetPaths.HERO);
            heroImage = ImageIO.read(url);
            createMirroredHeroImage(); // Initialize mirrored image
        } catch (IOException e) {
            heroImage = null;
            mirroredHeroImage = null;
        }
    }

    // Public getInstance method with lazy initialization
    public static Hero getInstance(int sx, int sy, int w, int h) {
        if (instance == null) {
            instance = new Hero(sx, sy, w, h);
        }
        return instance;
    }

    // Alternative getInstance if hero already exists
    public static Hero getInstance() {
        if (instance == null) {
            throw new IllegalStateException("Hero not initialized. Call getInstance(sx, sy, w, h) first.");
        }
        return instance;
    }

    // Create a mirrored version of the hero image for left-facing
    private void createMirroredHeroImage() {
        if (heroImage == null) return;
        int w = heroImage.getWidth();
        int h = heroImage.getHeight();
        mirroredHeroImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = mirroredHeroImage.createGraphics();
        AffineTransform at = AffineTransform.getScaleInstance(-1, 1);
        at.translate(-w, 0);
        g2d.drawImage(heroImage, at, null);
        g2d.dispose();
    }

    public void move(int dx, int dy) {
        if (dx < 0) {
            facingLeft = true; // Moving left
        } else if (dx > 0) {
            facingLeft = false; // Moving right
        }
        x += dx;
        y += dy;
    }

    public void draw(Graphics g) {
        if (heroImage != null) {
            BufferedImage imgToDraw;

            if (showingDamageEffect) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - damageEffectStartTime < DAMAGE_EFFECT_DURATION) {
                    imgToDraw = damageHeroImage;
                } else {
                    showingDamageEffect = false;
                    imgToDraw = facingLeft && mirroredHeroImage != null ? mirroredHeroImage : heroImage;
                }
            } else {
                imgToDraw = facingLeft && mirroredHeroImage != null ? mirroredHeroImage : heroImage;
            }

            g.drawImage(imgToDraw, x, y, width, height, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
        }
    }

    private BufferedImage tintImage(BufferedImage src, Color color) {
        BufferedImage tintedImage = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = tintedImage.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.setComposite(AlphaComposite.SrcAtop);
        g.setColor(color);
        g.fillRect(0, 0, src.getWidth(), src.getHeight());
        g.dispose();
        return tintedImage;
    }

    private void startDamageEffect() {
        showingDamageEffect = true;
        damageEffectStartTime = System.currentTimeMillis();

        // **Modification Start: Create damage image based on facing direction**
        if (facingLeft && mirroredHeroImage != null) {
            damageHeroImage = tintImage(mirroredHeroImage, new Color(255, 0, 0, 100));
        } else {
            damageHeroImage = tintImage(heroImage, new Color(255, 0, 0, 100));
        }
        // **Modification End**
    }

    public void setHealth(int h) {
        if (h < this.health) {
            startDamageEffect();
        }
        this.health = h;
    }

    public void setPosition(int nx, int ny) {
        x = nx;
        y = ny;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getHealth() { return health; }

    public void takeDamage(int amount) {
        setHealth(this.health - amount);
    }

    // Add reset method for testing or restarting game
    public static void reset() {
        instance = null;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }
}
