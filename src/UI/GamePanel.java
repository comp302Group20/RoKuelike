package UI;

import Domain.Hero;
import Domain.Monster;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

public class GamePanel extends JPanel {
    private Hero hero;
    private java.util.List<Monster> monsters;

    public GamePanel() {
        setBackground(new Color(37, 0, 0));
        this.hero = new Hero(50, 50);    // Hero starting coordinates
        this.monsters = new ArrayList<>(); // Potentially add monsters

        // Example: Add monsters
        // monsters.add(new ArcherMonster(200, 200)); // if you implement ArcherMonster

        // Setup key controls for hero movement
        setFocusable(true);
        requestFocusInWindow();
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int step = 10;
                switch (e.getKeyCode()) {
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
                repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw hero
        hero.draw(g);

        // Draw monsters
        for (Monster monster : monsters) {
            monster.update();
            monster.draw(g);
        }
    }
}
