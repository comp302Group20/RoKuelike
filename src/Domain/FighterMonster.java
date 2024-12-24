package Domain;

import java.awt.*;
import java.util.Random;

/**
 * FighterMonster class that is extends Monster
 * This monsterspecific behavior: random movement and attack abilities.
 */
public class FighterMonster extends Monster {
    private int speed;        // Movement speed
    private Random random;    // Random number generator for direction changes
    private int direction;    // 0 = up, 1 = down, 2 = left, 3 = right

    /**
     * Constructor
     *
     * @param startX     Starting X position of the monster.
     * @param startY     Starting Y position of the monster.
     * @param imagePath  Path to the monster's image.
     */
    public FighterMonster(int startX, int startY, String imagePath) {
        super(startX, startY, imagePath);
        this.speed = 2; // Default movement speed
        this.random = new Random();
        this.direction = random.nextInt(4); // Initialize with a random direction
    }

    
      //Updates the monster's position and behavior on each game tick.
     
    @Override
    public void update() {
        // Randomly change direction every few updates
        if (random.nextInt(10) < 2) { // 20% chance to change direction
            direction = random.nextInt(4);
        }

        // Move based on current direction
        switch (direction) {
            case 0: // Move up
                y -= speed;
                break;
            case 1: // Move down
                y += speed;
                break;
            case 2: // Move left
                x -= speed;
                break;
            case 3: // Move right
                x += speed;
                break;
        }

        // Add boundary checking to keep the monster within game bounds
        // Using precise monster dimensions from Monster.java
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x > 832 - 20) x = 832 - 20; // Right boundary based on 832x832 panel
        if (y > 832 - 20) y = 832 - 20; // Bottom boundary based on 832x832 panel
    }

    /**
     * Renders the monster onto the game screen.
     *
     * @param g Graphics object used to draw the monster.
     */
    @Override
    public void draw(Graphics g) {
        super.draw(g);
        // Additional visual logic for FighterMonster can be added here
    }

    /**
     * FighterMonster-specific attack behavior.
     */
    public void attack() {
        System.out.println("FighterMonster attacks with brute force!");
        // Additional attack logic can be implemented here
    }

    /**
     * Sets the speed of the FighterMonster.
     *
     * @param speed The speed value to set.
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * Gets the speed of the FighterMonster.
     *
     * @return The current speed.
     */
    public int getSpeed() {
        return speed;
    }
}