package Domain;

import UI.BuildModePanel;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public abstract class Monster {
    protected int x, y;
    protected int width = 64;
    protected int height = 64;
    protected Image monsterImage;
    protected Hero hero;
    protected BuildModePanel.CellType[][] mapGrid;

    public Monster(int startX, int startY, String imagePath, Hero hero, BuildModePanel.CellType[][] mapGrid) {
        x = startX;
        y = startY;
        this.hero = hero;
        this.mapGrid = mapGrid;
        loadImage(imagePath);
    }

    private void loadImage(String path) {
        try {
            String p = path.startsWith("/") ? path.substring(1) : path;
            URL u = getClass().getClassLoader().getResource(p);
            if (u == null) throw new IOException();
            BufferedImage i = ImageIO.read(u);
            monsterImage = i;
        } catch (IOException e) {
            monsterImage = fallback();
        }
    }

    private BufferedImage fallback() {
        BufferedImage f = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = f.createGraphics();
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();
        return f;
    }

    public abstract void update();

    public void draw(Graphics g) {
        g.drawImage(monsterImage, x, y, width, height, null);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int nx, int ny) { x = nx; y = ny; }
}
