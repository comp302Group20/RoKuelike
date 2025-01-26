package Domain;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Represents a projectile arrow fired by an ArcherMonster.
 */
public class Arrow {
    private Point2D.Double position;
    private Point2D.Double velocity;
    private double angle;
    private boolean active = true;
    private static final int ARROW_LENGTH = 30;
    private static final int ARROW_HEAD_SIZE = 8;
    private Color arrowColor = new Color(200, 200, 200);
    private Point2D.Double startPosition;

    /**
     * Constructs an Arrow instance with a start position and a target position.
     * @param startX the x-coordinate from where the arrow is fired
     * @param startY the y-coordinate from where the arrow is fired
     * @param targetX the x-coordinate toward which the arrow is aimed
     * @param targetY the y-coordinate toward which the arrow is aimed
     */
    public Arrow(double startX, double startY, double targetX, double targetY) {
        position = new Point2D.Double(startX, startY);
        startPosition = new Point2D.Double(startX, startY);
        double dx = targetX - startX;
        double dy = targetY - startY;
        angle = Math.atan2(dy, dx);

        double speed = 60.0;
        velocity = new Point2D.Double(
                Math.cos(angle) * speed,
                Math.sin(angle) * speed
        );
    }

    /**
     * Updates the arrow's position based on its velocity.
     */
    public void update() {
        position.x += velocity.x;
        position.y += velocity.y;
    }

    /**
     * Draws the arrow at its current position and orientation.
     * @param g2d the Graphics2D context for rendering
     */
    public void draw(Graphics2D g2d) {
        g2d.setColor(arrowColor);
        drawArrowAt(g2d, position.x, position.y);
    }

    /**
     * Helper method to transform and render the arrow shape at a given position.
     * @param g2d the Graphics2D context
     * @param x the x-coordinate
     * @param y the y-coordinate
     */
    private void drawArrowAt(Graphics2D g2d, double x, double y) {
        AffineTransform old = g2d.getTransform();
        g2d.translate(x, y);
        g2d.rotate(angle);

        g2d.setStroke(new BasicStroke(2));
        g2d.drawLine(0, 0, ARROW_LENGTH, 0);

        int[] xPoints = {ARROW_LENGTH, ARROW_LENGTH - ARROW_HEAD_SIZE, ARROW_LENGTH - ARROW_HEAD_SIZE};
        int[] yPoints = {0, -4, 4};
        g2d.fillPolygon(xPoints, yPoints, 3);

        g2d.setTransform(old);
    }

    /**
     * Checks if the arrow has traveled beyond a certain distance and should be removed.
     * @return true if the arrow is out of range, false otherwise
     */
    public boolean isOutOfRange() {
        double dx = position.x - startPosition.x;
        double dy = position.y - startPosition.y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance > 3 * 64;
    }

    /**
     * Provides the arrow's current 2D position.
     * @return a Point2D.Double indicating the arrow's coordinates
     */
    public Point2D.Double getPosition() {
        return position;
    }

    /**
     * Returns the arrow's angle in radians.
     * @return the current angle
     */
    public double getAngle() {
        return angle;
    }
}
