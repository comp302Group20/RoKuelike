package Domain;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * A generic object that can be placed on the grid, such as boxes or chests.
 */
public class GameObject implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String imagePath;
    private transient Image image;
    private final SpriteLocation spriteLocation;

    /**
     * Constructs a GameObject with a name and an image path, optionally without sprite sheet coordinates.
     * @param name the object's name
     * @param imagePath the path to the object's image resource
     */
    public GameObject(String name, String imagePath) {
        this(name, imagePath, null);
    }

    /**
     * Constructs a GameObject with a name, image path, and sprite sheet location for partial image extraction.
     * @param name the object's name
     * @param imagePath the path to the sprite sheet or image
     * @param spriteLocation the sub-area of the sprite sheet to use
     */
    public GameObject(String name, String imagePath, SpriteLocation spriteLocation) {
        this.name = name;
        this.imagePath = imagePath;
        this.spriteLocation = spriteLocation;
        loadImage();
    }

    /**
     * Loads the object's image, either from a sprite sheet sub-region or a standalone image.
     */
    private void loadImage() {
        try {
            if (spriteLocation != null) {
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
                image = new ImageIcon(getClass().getResource(imagePath)).getImage();
            }
        } catch (Exception e) {
            System.err.println("Failed to load image for " + name + " from " + imagePath);
            e.printStackTrace();
            image = null;
        }
    }

    /**
     * Retrieves the name of this game object.
     * @return the object's name
     */
    public String getName() {
        return name;
    }

    /**
     * Provides the path to the object's image resource.
     * @return a String representing the resource path
     */
    public String getImagePath() {
        return imagePath;
    }

    /**
     * Gets the loaded image for this object (or null if failed).
     * @return the Image object
     */
    public Image getImage() {
        return image;
    }

    /**
     * Returns the sub-region information if this object is sourced from a sprite sheet.
     * @return a SpriteLocation instance or null if not applicable
     */
    public SpriteLocation getSpriteLocation() {
        return spriteLocation;
    }
}
