package Domain;

import Utils.AssetPaths;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;

public class Hero {
    private static Hero instance = null;
    private int x;
    private int y;
    private int width;
    private int height;
    private BufferedImage heroImage;
    private BufferedImage damageHeroImage;
    private int health = 3;

    private boolean showingDamageEffect = false;
    private long damageEffectStartTime = 0;
    private static final int DAMAGE_EFFECT_DURATION = 50;

    // Private constructor
    public Hero(int sx, int sy, int w, int h) {
        x = sx;
        y = sy;
        width = w;
        height = h;
        try {
            URL url = getClass().getResource(AssetPaths.HERO);
            heroImage = ImageIO.read(url);
        } catch (IOException e) {
            heroImage = null;
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

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public void draw(Graphics g) {
        if (heroImage != null) {
            BufferedImage imgToDraw = heroImage;
            if (showingDamageEffect) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - damageEffectStartTime < DAMAGE_EFFECT_DURATION) {
                    imgToDraw = damageHeroImage;
                } else {
                    showingDamageEffect = false;
                }
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
        damageHeroImage = tintImage(heroImage, new Color(255, 0, 0, 100));
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
}