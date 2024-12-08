package Domain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class HeroMovementExample extends JFrame {

    private GamePanel gamePanel;
    private Hero hero;

    public HeroMovementExample() {
        setTitle("2D Hero Movement Example");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        hero = new Hero(50, 50); // Starting position of hero

        gamePanel = new GamePanel(hero);
        add(gamePanel);

        // Key listener to move the hero
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                // Speed or step size
                int step = 10;

                switch (key) {
                    case KeyEvent.VK_LEFT:
                        hero.move(-step, 0);
                        break;
                    case KeyEvent.VK_RIGHT:
                        hero.move(step, 0);
                        break;
                    case KeyEvent.VK_UP:
                        hero.move(0, -step);
                        break;
                    case KeyEvent.VK_DOWN:
                        hero.move(0, step);
                        break;
                }

                // Repaint the panel to reflect hero’s new position
                gamePanel.repaint();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            HeroMovementExample example = new HeroMovementExample();
            example.setVisible(true);
        });
    }

    // Inner class for hero representation
    static class Hero {
        private int x;
        private int y;
        private int width = 20;   // Adjust these if needed for your image size
        private int height = 20;  // Adjust these if needed for your image size
        private Image heroImage;

        public Hero(int startX, int startY) {
            this.x = startX;
            this.y = startY;

            // Load hero.png from resources
            
            heroImage = new ImageIcon(getClass().getResource("/rokue-like-assets/player4x.png")).getImage();
            
          
        }

        // Move hero by dx, dy
        public void move(int dx, int dy) {
            x += dx;
            y += dy;
        }

        public void draw(Graphics g) {
            // Draw the hero image
            g.drawImage(heroImage, x, y, width, height, null);
        }

        public int getX() { return x; }
        public int getY() { return y; }
    }

    // Panel for rendering the game
    static class GamePanel extends JPanel {
        private Hero hero;

        public GamePanel(Hero hero) {
            this.hero = hero;
            setBackground(new Color(37, 0, 0));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Draw hero
            hero.draw(g);
        }
    }
}
