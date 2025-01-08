package Domain;

import UI.BuildModePanel;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;
import java.awt.geom.AffineTransform;

/**
 * Abstract class representing a generic monster in the game.
 */
public abstract class Monster {
    protected int x, y;
    protected int width = 64;
    protected int height = 64;
    protected BufferedImage monsterImage;
    protected BufferedImage mirroredImage; // Mirrored image for left-facing
    protected Hero hero;
    protected BuildModePanel.CellType[][] mapGrid;
    protected static final int CELL_SIZE = 64;

    protected boolean facingLeft = false;

    /**
     * Constructor for Monster.
     *
     * @param startX    Starting x-coordinate in pixels.
     * @param startY    Starting y-coordinate in pixels.
     * @param imagePath Path to the monster's image.
     * @param h         Reference to the Hero.
     * @param mapGrid   The game grid.
     */
    public Monster(int startX, int startY, String imagePath, Hero h, BuildModePanel.CellType[][] mapGrid) {
        x = startX;
        y = startY;
        this.hero = h;
        this.mapGrid = mapGrid;
        loadImage(imagePath);
    }

    /**
     * Loads the monster image and creates a mirrored version for left-facing.
     *
     * @param path Path to the image.
     */
    private void loadImage(String path) {
        try {
            String p = path.startsWith("/") ? path.substring(1) : path;
            URL u = getClass().getClassLoader().getResource(p);
            if (u == null) throw new IOException("Image not found: " + p);
            BufferedImage i = ImageIO.read(u);
            monsterImage = i;
            mirroredImage = mirrorImage(monsterImage);
        } catch (IOException e) {
            monsterImage = fallback();
            mirroredImage = fallback();
            System.err.println("Failed to load monster image: " + e.getMessage());
        }
    }

    /**
     * Creates a mirrored version of the given image.
     *
     * @param original The original image.
     * @return Mirrored image.
     */
    private BufferedImage mirrorImage(BufferedImage original) {
        AffineTransform transform = AffineTransform.getScaleInstance(-1, 1);
        transform.translate(-original.getWidth(), 0);

        BufferedImage mirrored = new BufferedImage(
                original.getWidth(),
                original.getHeight(),
                original.getType()
        );

        Graphics2D g = mirrored.createGraphics();
        g.drawImage(original, transform, null);
        g.dispose();

        return mirrored;
    }

    /**
     * Fallback image in case the original image fails to load.
     *
     * @return Fallback BufferedImage.
     */
    private BufferedImage fallback() {
        BufferedImage f = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = f.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return f;
    }

    /**
     * Updates the monster's facing direction based on the hero's x-position.
     * If the hero is to the left, the monster faces left; if to the right, it faces right.
     * If on the same x-axis position, the monster retains its current facing direction.
     */
    public void updateFacingDirection() {
        int heroColumn = hero.getX() / CELL_SIZE;
        int monsterColumn = this.x / CELL_SIZE;

        if (heroColumn < monsterColumn) {
            facingLeft = true;
        } else if (heroColumn > monsterColumn) {
            facingLeft = false;
        }
        // If heroColumn == monsterColumn, do not change facingLeft
    }

    /**
     * Abstract update method to be implemented by subclasses.
     */
    public abstract void update();

    /**
     * Draws the monster on the screen, facing the correct direction.
     *
     * @param g Graphics context.
     */
    public void draw(Graphics g) {
        BufferedImage imgToDraw = (facingLeft && mirroredImage != null) ? mirroredImage : monsterImage;
        g.drawImage(imgToDraw, x, y, width, height, null);
    }

    // Getters and setters
    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int nx, int ny) { x = nx; y = ny; }
}
