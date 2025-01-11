package UI;

import Controller.GameController;
import Domain.Hall;
import Utils.AssetPaths;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * The main menu of the Rokue-Like game.
 * Lets you start a new game, show help, or exit.
 */
public class RokueLikeMainMenu extends JFrame {

    private JLabel titleLabel;
    private JButton startGameButton;
    private JButton helpButton;
    private JButton exitButton;

    public RokueLikeMainMenu() {
        setTitle("Rokue-Like - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setLocationRelativeTo(null);

        // Background
        ImageIcon backgroundIcon = loadImageIcon(AssetPaths.MAIN_MENU_BACKGROUND);
        Image backgroundImage = backgroundIcon.getImage();
        BackgroundPanel backgroundPanel = new BackgroundPanel(backgroundImage);
        setContentPane(backgroundPanel);

        // Title
        ImageIcon titleIcon = loadImageIcon(AssetPaths.GAME_TITLE);
        Image titleImage = titleIcon.getImage();
        titleLabel = new JLabel(new ImageIcon(titleImage));

        // Buttons
        startGameButton = createButton(AssetPaths.NEWGAME_BUTTON);
        helpButton = createButton(AssetPaths.HELP_BUTTON);
        exitButton = createButton(AssetPaths.EXIT_BUTTON);

        // Logic
        startGameButton.addActionListener(e -> {
            dispose();
            // For demonstration, we’ll create a single Hall
            Hall earthHall = new Hall("Earth Hall", 8, 8, 6);
            new GameController(earthHall);
        });
        helpButton.addActionListener(e -> {
            HelpDialog helpDialog = new HelpDialog(RokueLikeMainMenu.this);
            helpDialog.setVisible(true);
        });
        exitButton.addActionListener(e -> System.exit(0));

        backgroundPanel.setLayout(null);
        backgroundPanel.add(titleLabel);
        backgroundPanel.add(startGameButton);
        backgroundPanel.add(helpButton);
        backgroundPanel.add(exitButton);

        // Resize/position
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeComponents();
            }
        });
        resizeComponents();
    }

    private ImageIcon loadImageIcon(String resourcePath) {
        try {
            String pathForResource = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
            URL resourceUrl = getClass().getClassLoader().getResource(pathForResource);
            if (resourceUrl == null) {
                System.err.println("Could not find resource at: " + resourcePath);
                return createFallbackIcon();
            }
            return new ImageIcon(resourceUrl);
        } catch (Exception e) {
            System.err.println("Error loading image from " + resourcePath + ": " + e.getMessage());
            return createFallbackIcon();
        }
    }

    private ImageIcon createFallbackIcon() {
        BufferedImage fallback = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = fallback.createGraphics();
        g2d.setColor(Color.GRAY);
        g2d.fillRect(0, 0, 100, 100);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, 99, 99);
        g2d.dispose();
        return new ImageIcon(fallback);
    }

    private JButton createButton(String imagePath) {
        ImageIcon icon = loadImageIcon(imagePath);
        JButton button = new JButton(icon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        return button;
    }

    private void resizeComponents() {
        int frameWidth = getWidth();
        int frameHeight = getHeight();

        // Title
        int titleWidth = frameWidth / 3;
        int titleHeight = titleWidth / 3;
        titleLabel.setBounds((frameWidth - titleWidth) / 2, frameHeight / 10, titleWidth, titleHeight);
        scaleImageIcon(titleLabel, titleWidth, titleHeight);

        // Buttons
        int buttonWidth = frameWidth / 8;
        int buttonHeight = buttonWidth / 2;
        int buttonX = (frameWidth - buttonWidth) / 2;
        int buttonYStart = frameHeight / 2 - buttonHeight;
        int buttonSpacing = buttonHeight + 20;

        startGameButton.setBounds(buttonX, buttonYStart, buttonWidth, buttonHeight);
        helpButton.setBounds(buttonX, buttonYStart + buttonSpacing, buttonWidth, buttonHeight);
        exitButton.setBounds(buttonX, buttonYStart + 2 * buttonSpacing, buttonWidth, buttonHeight);

        scaleImageIcon(startGameButton, buttonWidth, buttonHeight);
        scaleImageIcon(helpButton, buttonWidth, buttonHeight);
        scaleImageIcon(exitButton, buttonWidth, buttonHeight);
    }

    private void scaleImageIcon(JComponent component, int width, int height) {
        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            if (label.getIcon() != null) {
                Image scaled = ((ImageIcon) label.getIcon()).getImage()
                        .getScaledInstance(width, height, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaled));
            }
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            if (button.getIcon() != null) {
                Image scaled = ((ImageIcon) button.getIcon()).getImage()
                        .getScaledInstance(width, height, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(scaled));
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RokueLikeMainMenu menu = new RokueLikeMainMenu();
            menu.setVisible(true);
        });
    }
}
