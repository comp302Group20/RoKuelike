package Domain;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple class to store collected Enchantments and display them.
 */
public class Inventory {
    private List<Enchantment> collectedEnchantments;

    public Inventory() {
        this.collectedEnchantments = new ArrayList<>();
    }

    /**
     * Adds an enchantment to the inventory.
     * @param e the Enchantment to add
     */
    public void addEnchantment(Enchantment e) {
        collectedEnchantments.add(e);
    }

    /**
     * Draws the inventory items (enchantments) at the given starting position.
     * @param g the Graphics context
     * @param startX the x coordinate to start drawing
     * @param startY the y coordinate to start drawing
     */
    public void draw(Graphics g, int startX, int startY) {
        int offset = 0;
        for (Enchantment e : collectedEnchantments) {
            BufferedImage img = e.getImage();
            if (img != null) {
                g.drawImage(img, startX, startY + offset, 40, 40, null);
            }
            offset += 50; // stack them vertically, 50 px apart
        }
    }

    public List<Enchantment> getCollectedEnchantments() {
        return collectedEnchantments;
    }
}
