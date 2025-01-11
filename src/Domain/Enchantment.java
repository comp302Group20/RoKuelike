package Domain;

import Utils.AssetPaths;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Represents a single enchantment that appears on the board.
 * For now, it just draws itself. No collection or timing logic.
 */
public class Enchantment {

    private int x, y;
    private int width, height;
    private EnchantmentType type;
    private BufferedImage image;

    public Enchantment(int x, int y, int width, int height, EnchantmentType type) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.type = type;
        loadImage();
    }

    private void loadImage() {
        // Load the correct image based on the enchantment type
        String path;
        switch (type) {
            case REVEAL:       path = AssetPaths.REVEAL_ENCH;       break;
            case CLOAK:        path = AssetPaths.CLOAK_ENCH;        break;
            case LURINGGEM:    path = AssetPaths.LURING_ENCH;       break;
            case EXTRATIME:    path = AssetPaths.EXTRATIME_ENCH;    break;
            case EXTRALIFE:    path = AssetPaths.EXTRALIFE_ENCH;    break;
            default:           path = AssetPaths.REVEAL_ENCH;       break;
        }

        try {
            URL url = getClass().getResource(path);
            if (url != null) {
                image = ImageIO.read(url);
            }
        } catch (IOException e) {
            image = null;
        }
    }

    public void draw(Graphics g) {
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        } else {
            // Fallback if image not found
            g.setColor(Color.MAGENTA);
            g.fillRect(x, y, width, height);
        }
    }

    // Potentially add getters/setters if needed later
}
