package Domain;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Inventory {
    private List<Enchantment> collectedEnchantments;
    public static final int SLOTS_X = 3;
    public static final int SLOTS_Y = 2;
    public static final int SLOT_SIZE = 64;
    public static final int SPACING = 10;

    public Inventory() {
        this.collectedEnchantments = new ArrayList<>();
    }

    public void addEnchantment(Enchantment e) {
        if (collectedEnchantments.size() < SLOTS_X * SLOTS_Y) {
            collectedEnchantments.add(e);
        }
    }

    public void draw(Graphics g, int startX, int startY) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(255, 255, 224, 180));  // Light yellow background
        int totalWidth = (SLOTS_X * SLOT_SIZE) + ((SLOTS_X - 1) * SPACING);
        int totalHeight = (SLOTS_Y * SLOT_SIZE) + ((SLOTS_Y - 1) * SPACING);
        g2d.fillRect(startX - 10, startY - 10, totalWidth + 20, totalHeight + 20);

        // Draw inventory title with pixel font
        g2d.setFont(Utils.GameFonts.pixelFont.deriveFont(20f));  // Add this line
        g2d.setColor(Color.WHITE);
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth("Inventory");
        int textX = startX + (totalWidth - textWidth) / 2;
        g2d.drawString("Inventory", textX, startY - 15);

        // Draw slots and items
        for (int row = 0; row < SLOTS_Y; row++) {
            for (int col = 0; col < SLOTS_X; col++) {
                int x = startX + col * (SLOT_SIZE + SPACING);
                int y = startY + row * (SLOT_SIZE + SPACING);

                // Draw slot background (keeping original gray color)
                g2d.setColor(new Color(70, 70, 70, 200));
                g2d.fillRect(x, y, SLOT_SIZE, SLOT_SIZE);

                // Draw slot border
                g2d.setColor(new Color(200, 200, 200));
                g2d.drawRect(x, y, SLOT_SIZE, SLOT_SIZE);

                // Draw enchantment if exists
                int index = row * SLOTS_X + col;
                if (index < collectedEnchantments.size()) {
                    Enchantment e = collectedEnchantments.get(index);
                    BufferedImage img = e.getImage();
                    if (img != null) {
                        g2d.drawImage(img, x + SLOT_SIZE/4, y + SLOT_SIZE/4,
                                SLOT_SIZE/2, SLOT_SIZE/2, null);
                    }
                }
            }
        }
    }

    public List<Enchantment> getCollectedEnchantments() {
        return collectedEnchantments;
    }

    public boolean isFull() {
        return collectedEnchantments.size() >= SLOTS_X * SLOTS_Y;
    }

    public void setEnchantments(List<Enchantment> loadedEnchantments) {
        this.collectedEnchantments = new ArrayList<>(loadedEnchantments);
    }

    public void clearEnchantments() {
        this.collectedEnchantments.clear();
    }
}