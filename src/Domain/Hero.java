package Domain;

import Utils.AssetPaths;
import java.awt.*;
import javax.swing.ImageIcon;

public class Hero {
    private int x;
    private int y;
    private int width;
    private int height;
    private Image heroImage;
    private int health = 3;

    public Hero(int sx, int sy, int w, int h) {
        x = sx;
        y = sy;
        width = w;
        height = h;
        try {
            heroImage = new ImageIcon(getClass().getResource(AssetPaths.HERO)).getImage();
        } catch (Exception e) {
            heroImage = null;
        }
    }

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public void draw(Graphics g) {
        if (heroImage != null) {
            g.drawImage(heroImage, x, y, width, height, null);
        } else {
            g.setColor(Color.RED);
            g.fillRect(x, y, width, height);
        }
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
    public void setHealth(int h) { health = h; }
}
