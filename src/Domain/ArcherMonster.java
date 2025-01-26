package Domain;

import UI.BuildModePanel;
import Utils.AssetPaths;
import UI.GamePanel;
import Utils.SoundPlayer;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A monster that shoots arrows at the hero when in range.
 */
public class ArcherMonster extends Monster implements Serializable {
    private static final long serialVersionUID = 1L;
    private long lastShot;
    private transient GamePanel gamePanel;
    private transient List<Arrow> activeArrows;
    private List<StuckArrow> stuckArrows;
    private static final int SHOOT_DELAY = 1000;

    /**
     * Constructs an ArcherMonster at the specified position with references to the hero and game panel.
     * @param sx the initial x-coordinate in pixels
     * @param sy the initial y-coordinate in pixels
     * @param h the hero instance
     * @param mg the 2D map grid
     * @param gp the GamePanel for interactions
     */
    public ArcherMonster(int sx, int sy, Hero h, BuildModePanel.CellType[][] mg, GamePanel gp) {
        super(sx, sy, AssetPaths.ARCHER, h, mg);
        lastShot = System.currentTimeMillis();
        gamePanel = gp;
        activeArrows = new ArrayList<>();
        stuckArrows = new ArrayList<>();
    }

    /**
     * Updates the ArcherMonster's behavior (aim, shoot arrows, handle arrow collisions).
     */
    @Override
    public void update() {
        updateFacingDirection();

        if (gamePanel != null && gamePanel.isCloakActive()) {
            System.out.println("Hero is cloaked - Archer cannot detect them!");
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastShot >= SHOOT_DELAY) {
            int mr = y / CELL_SIZE;
            int mc = x / CELL_SIZE;
            int hr = hero.getY() / CELL_SIZE;
            int hc = hero.getX() / CELL_SIZE;
            double distance = Math.sqrt((mr - hr) * (mr - hr) + (mc - hc) * (mc - hc));

            if (distance <= 3 && distance != 2 * Math.sqrt(2)) {
                shootArrow();
                lastShot = now;
            }
        }

        if (activeArrows != null) {
            Iterator<Arrow> it = activeArrows.iterator();
            while (it.hasNext()) {
                Arrow arrow = it.next();
                arrow.update();

                if (arrow.isOutOfRange()) {
                    it.remove();
                    continue;
                }

                Point2D.Double pos = arrow.getPosition();
                if (checkHeroCollision(pos.x, pos.y)) {
                    double heroCenterX = hero.getX() + hero.getWidth() / 2.0;
                    double heroCenterY = hero.getY() + hero.getHeight() / 2.0;

                    double dx = pos.x - heroCenterX;
                    double dy = pos.y - heroCenterY;
                    double length = Math.sqrt(dx * dx + dy * dy);
                    if (length > 0) {
                        dx /= length;
                        dy /= length;
                    }

                    double stickX = heroCenterX + dx * (hero.getWidth() / 2.0);
                    double stickY = heroCenterY + dy * (hero.getHeight() / 2.0);

                    StuckArrow stuckArrow = new StuckArrow(
                            stickX - hero.getX(),
                            stickY - hero.getY(),
                            arrow.getAngle()
                    );
                    stuckArrows.add(stuckArrow);

                    hero.setHealth(hero.getHealth() - 1);
                    SoundPlayer.playSound("/resources/sounds/hurt.wav");
                    System.out.println("Hero hit by ArcherMonster! Health: " + hero.getHealth());
                    it.remove();
                    continue;
                }

                if (!isValidPosition(pos.x, pos.y)) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Checks if the arrow coordinates collide with the hero's bounding box.
     * @param arrowX the arrow's x-position
     * @param arrowY the arrow's y-position
     * @return true if collision occurs, false otherwise
     */
    private boolean checkHeroCollision(double arrowX, double arrowY) {
        Rectangle heroRect = new Rectangle(
                hero.getX() - 5,
                hero.getY() - 5,
                hero.getWidth() + 10,
                hero.getHeight() + 10
        );
        return heroRect.contains(arrowX, arrowY);
    }

    /**
     * Creates a new arrow directed at the hero's position and adds it to the active arrows.
     */
    private void shootArrow() {
        double startX = x + width / 2.0;
        double startY = y + height / 2.0;
        double targetX = hero.getX() + hero.getWidth() / 2.0;
        double targetY = hero.getY() + hero.getHeight() / 2.0;

        Arrow arrow = new Arrow(startX, startY, targetX, targetY);
        if (activeArrows == null) {
            activeArrows = new ArrayList<>();
        }
        activeArrows.add(arrow);
    }

    /**
     * Checks if the given coordinates are valid for arrow flight (i.e., not colliding with walls or out of bounds).
     * @param x the x-coordinate in pixels
     * @param y the y-coordinate in pixels
     * @return true if valid, false otherwise
     */
    private boolean isValidPosition(double x, double y) {
        int gridX = (int) x / CELL_SIZE;
        int gridY = (int) y / CELL_SIZE;

        if (gridX < 0 || gridY < 0 || gridX >= mapGrid[0].length || gridY >= mapGrid.length) {
            return false;
        }
        return mapGrid[gridY][gridX] != BuildModePanel.CellType.WALL;
    }

    /**
     * Renders the ArcherMonster, its active arrows, and any arrows stuck in the hero.
     * @param g the Graphics context for drawing
     */
    @Override
    public void draw(Graphics g) {
        super.draw(g);

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        if (activeArrows != null) {
            for (Arrow arrow : activeArrows) {
                arrow.draw(g2d);
            }
        }

        for (StuckArrow stuckArrow : stuckArrows) {
            stuckArrow.draw(g2d, hero);
        }

        g2d.dispose();
    }

    /**
     * A helper class representing an arrow stuck in the hero's body, storing its relative position and angle.
     */
    private static class StuckArrow implements Serializable {
        private static final long serialVersionUID = 1L;
        private double relativeX;
        private double relativeY;
        private double angle;
        private static final int ARROW_LENGTH = 30;
        private static final int ARROW_HEAD_SIZE = 8;
        private Color arrowColor = new Color(150, 0, 0);

        /**
         * Constructs a StuckArrow with relative position and angle for rendering.
         * @param relX relative x-position to the hero
         * @param relY relative y-position to the hero
         * @param angle the arrow's direction angle in radians
         */
        public StuckArrow(double relX, double relY, double angle) {
            this.relativeX = relX;
            this.relativeY = relY;
            this.angle = angle;
        }

        /**
         * Draws the stuck arrow, transforming according to the hero's current position.
         * @param g2d the Graphics2D context
         * @param hero the hero in which the arrow is stuck
         */
        public void draw(Graphics2D g2d, Hero hero) {
            double absX = hero.getX() + relativeX;
            double absY = hero.getY() + relativeY;

            AffineTransform old = g2d.getTransform();
            g2d.translate(absX, absY);
            g2d.rotate(angle);

            g2d.setColor(arrowColor);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(0, 0, ARROW_LENGTH, 0);

            int[] xPoints = {ARROW_LENGTH, ARROW_LENGTH - ARROW_HEAD_SIZE, ARROW_LENGTH - ARROW_HEAD_SIZE};
            int[] yPoints = {0, -4, 4};
            g2d.fillPolygon(xPoints, yPoints, 3);

            g2d.setTransform(old);
        }
    }

    /**
     * Custom deserialization logic to ensure transient fields are reinitialized.
     * @param in the ObjectInputStream
     * @throws IOException if an I/O error occurs
     * @throws ClassNotFoundException if a class can't be found
     */
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        activeArrows = new ArrayList<>();
    }
}
