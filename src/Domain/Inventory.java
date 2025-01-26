package Domain;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * A storage for enchantments collected by the hero, supporting up to 6 items.
 */
public class Inventory {
    private List<Enchantment> collectedEnchantments;
    public static final int SLOTS_X = 3;
    public static final int SLOTS_Y = 2;
    public static final int SLOT_SIZE = 64;
    public static final int SPACING = 10;

    /**
     * Constructs an Inventory with an empty list of enchantments.
     */
    public Inventory() {
        this.collectedEnchantments = new ArrayList<>();
    }

    /**
     * Attempts to add an enchantment if there is space in the inventory.
     * @param e the enchantment to store
     */
    public void addEnchantment(Enchantment e) {
        if (collectedEnchantments.size() < SLOTS_X * SLOTS_Y) {
            collectedEnchantments.add(e);
        }
    }

    /**
     * Renders the inventory slots and their contained enchantments at the specified coordinates.
     * @param g the Graphics context for drawing
     * @param startX the top-left x-position for drawing
     * @param startY the top-left y-position for drawing
     */
    public void draw(Graphics g, int startX, int startY) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(255, 255, 224, 180));
        int totalWidth = (SLOTS_X * SLOT_SIZE) + ((SLOTS_X - 1) * SPACING);
        int totalHeight = (SLOTS_Y * SLOT_SIZE) + ((SLOTS_Y - 1) * SPACING);
        g2d.fillRect(startX - 10, startY - 10, totalWidth + 20, totalHeight + 20);

        g2d.setFont(Utils.GameFonts.pixelFont.deriveFont(20f));
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth("Inventory");
        int textX = startX + (totalWidth - textWidth) / 2;
        g2d.drawString("Inventory", textX, startY - 15);

        for (int row = 0; row < SLOTS_Y; row++) {
            for (int col = 0; col < SLOTS_X; col++) {
                int x = startX + col * (SLOT_SIZE + SPACING);
                int y = startY + row * (SLOT_SIZE + SPACING);

                g2d.setColor(new Color(70, 70, 70, 200));
                g2d.fillRect(x, y, SLOT_SIZE, SLOT_SIZE);

                g2d.setColor(new Color(200, 200, 200));
                g2d.drawRect(x, y, SLOT_SIZE, SLOT_SIZE);

                int index = row * SLOTS_X + col;
                if (index < collectedEnchantments.size()) {
                    Enchantment e = collectedEnchantments.get(index);
                    BufferedImage img = e.getImage();
                    if (img != null) {
                        g2d.drawImage(img, x + SLOT_SIZE / 4, y + SLOT_SIZE / 4,
                                SLOT_SIZE / 2, SLOT_SIZE / 2, null);
                    }
                }
            }
        }
    }

    /**
     * Retrieves the current list of stored enchantments.
     * @return a list of Enchantment objects
     */
    public List<Enchantment> getCollectedEnchantments() {
        return collectedEnchantments;
    }

    /**
     * Checks whether the inventory is full.
     * @return true if no more enchantments can be added, false otherwise
     */
    public boolean isFull() {
        return collectedEnchantments.size() >= SLOTS_X * SLOTS_Y;
    }

    /**
     * Sets the inventory's collection of enchantments to the provided list.
     * @param loadedEnchantments a list of Enchantment objects
     */
    public void setEnchantments(List<Enchantment> loadedEnchantments) {
        this.collectedEnchantments = new ArrayList<>(loadedEnchantments);
    }

    /**
     * Removes all enchantments from the inventory.
     */
    public void clearEnchantments() {
        this.collectedEnchantments.clear();
    }
}
