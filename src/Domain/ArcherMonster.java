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

public class ArcherMonster extends Monster implements Serializable {
    private static final long serialVersionUID = 1L;
    private long lastShot;
    private transient GamePanel gamePanel;
    private transient List<Arrow> activeArrows;  // Make transient since Arrow isn't serializable
    private List<StuckArrow> stuckArrows;
    private static final int SHOOT_DELAY = 1000;

    public ArcherMonster(int sx, int sy, Hero h, BuildModePanel.CellType[][] mg, GamePanel gp) {
        super(sx, sy, AssetPaths.ARCHER, h, mg);
        lastShot = System.currentTimeMillis();
        gamePanel = gp;
        activeArrows = new ArrayList<>();  // Initialize here
        stuckArrows = new ArrayList<>();
    }

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
                    // Calculate the actual collision point on hero's boundary
                    double heroCenterX = hero.getX() + hero.getWidth() / 2.0;
                    double heroCenterY = hero.getY() + hero.getHeight() / 2.0;

                    // Calculate direction from hero center to arrow
                    double dx = pos.x - heroCenterX;
                    double dy = pos.y - heroCenterY;

                    // Normalize the direction
                    double length = Math.sqrt(dx * dx + dy * dy);
                    if (length > 0) {
                        dx /= length;
                        dy /= length;
                    }

                    // Move the stick point to the hero's boundary
                    double stickX = heroCenterX + dx * (hero.getWidth() / 2.0);
                    double stickY = heroCenterY + dy * (hero.getHeight() / 2.0);

                    // Create stuck arrow at collision point
                    StuckArrow stuckArrow = new StuckArrow(
                            stickX - hero.getX(),  // Relative to hero's position
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

    private boolean checkHeroCollision(double arrowX, double arrowY) {
        Rectangle heroRect = new Rectangle(
                hero.getX() - 5,
                hero.getY() - 5,
                hero.getWidth() + 10,
                hero.getHeight() + 10
        );
        return heroRect.contains(arrowX, arrowY);
    }


    private void shootArrow() {
        double startX = x + width/2.0;
        double startY = y + height/2.0;
        double targetX = hero.getX() + hero.getWidth()/2.0;
        double targetY = hero.getY() + hero.getHeight()/2.0;

        Arrow arrow = new Arrow(startX, startY, targetX, targetY);
        if (activeArrows == null) {  // Reinitialize if null after deserialization
            activeArrows = new ArrayList<>();
        }
        activeArrows.add(arrow);
    }


    private boolean isValidPosition(double x, double y) {
        int gridX = (int)x / CELL_SIZE;
        int gridY = (int)y / CELL_SIZE;

        if (gridX < 0 || gridY < 0 || gridX >= mapGrid[0].length || gridY >= mapGrid.length) {
            return false;
        }

        return mapGrid[gridY][gridX] != BuildModePanel.CellType.WALL;
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g);

        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        if (activeArrows != null) {  // Check since it's transient
            for (Arrow arrow : activeArrows) {
                arrow.draw(g2d);
            }
        }

        for (StuckArrow stuckArrow : stuckArrows) {
            stuckArrow.draw(g2d, hero);
        }

        g2d.dispose();
    }

    private static class StuckArrow implements Serializable {
        private static final long serialVersionUID = 1L;
        private double relativeX;
        private double relativeY;
        private double angle;
        private static final int ARROW_LENGTH = 30;  // Match Arrow class
        private static final int ARROW_HEAD_SIZE = 8;  // Match Arrow class
        private Color arrowColor = new Color(150, 0, 0);

        public StuckArrow(double relX, double relY, double angle) {
            this.relativeX = relX;
            this.relativeY = relY;
            this.angle = angle;
        }

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

    // Add this method to handle deserialization
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        activeArrows = new ArrayList<>();  // Reinitialize transient field
    }
}