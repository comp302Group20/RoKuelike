package Domain;

import javax.swing.ImageIcon;
import java.awt.Graphics;
import java.awt.Image;

public abstract class Monster {
    protected int x, y;
    protected int width = 20;
    protected int height = 20;
    protected Image monsterImage;

    public Monster(int startX, int startY, String imagePath) {
        this.x = startX;
        this.y = startY;
        try {
            monsterImage = new ImageIcon(getClass().getResource(imagePath)).getImage();
        } catch (Exception e) {
            System.out.println("Monster image not found!");
        }
    }

    public abstract void update(); // Different monsters will implement different behaviors

    public void draw(Graphics g) {
        g.drawImage(monsterImage, x, y, width, height, null);
    }

    // Getters and setters
    public int getX() { return x; }
    public int getY() { return y; }
    public void setPosition(int x, int y) {
        this.x = x; this.y = y;
    }
}
