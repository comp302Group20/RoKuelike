package Domain;

import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Image;

public class Hero {
    private int x;
    private int y;
    private int width = 20;
    private int height = 20;
    private Image heroImage;

    public Hero(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        try {
            heroImage = new ImageIcon(getClass().getResource("/rokue-like-assets/player.png")).getImage();
        } catch (Exception e) {
            System.out.println("Hero image not found!");
        }
    }

    public void move(int dx, int dy) {
        x += dx;
        y += dy;
    }

    public void draw(Graphics g) {
        g.drawImage(heroImage, x, y, width, height, null);
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
