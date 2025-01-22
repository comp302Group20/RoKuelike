package Domain;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * Generic game object that can be placed on the grid (e.g. chests, boxes).
 */
public class GameObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String imagePath;
    private transient Image image;
    private final SpriteLocation spriteLocation;

    public GameObject(String name, String imagePath) {
        this(name, imagePath, null);
    }

    public GameObject(String name, String imagePath, SpriteLocation spriteLocation) {
        this.name = name;
        this.imagePath = imagePath;
        this.spriteLocation = spriteLocation;
        loadImage();
    }

    private void loadImage() {
        try {
            if (spriteLocation != null) {
                // Load from a sprite sheet
                Image spriteSheet = new ImageIcon(getClass().getResource(imagePath)).getImage();
                BufferedImage fullImage = new BufferedImage(
                        spriteSheet.getWidth(null),
                        spriteSheet.getHeight(null),
                        BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D g = fullImage.createGraphics();
                g.drawImage(spriteSheet, 0, 0, null);
                g.dispose();

                image = fullImage.getSubimage(
                        spriteLocation.getX(),
                        spriteLocation.getY(),
                        spriteLocation.getWidth(),
                        spriteLocation.getHeight()
                );
            } else {
                // Load a standalone image
                image = new ImageIcon(getClass().getResource(imagePath)).getImage();
            }
        } catch (Exception e) {
            System.err.println("Failed to load image for " + name + " from " + imagePath);
            e.printStackTrace();
            image = null;
        }
    }

    public String getName() {
        return name;
    }

    public String getImagePath() {
        return imagePath;
    }

    public Image getImage() {
        return image;
    }

    public SpriteLocation getSpriteLocation() {
        return spriteLocation;
    }
}
