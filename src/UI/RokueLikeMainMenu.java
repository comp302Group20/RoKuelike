package UI;

import Controller.BuildModeController;
import Domain.Hall;
import Utils.AssetPaths;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.net.URL;

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

        // Load background image using resource path
        ImageIcon backgroundIcon = loadImageIcon(AssetPaths.MAIN_MENU_BACKGROUND);
        Image backgroundImage = backgroundIcon.getImage();

        // Create the background panel
        BackgroundPanel backgroundPanel = new BackgroundPanel(backgroundImage);
        setContentPane(backgroundPanel);

        // Add title image
        ImageIcon titleIcon = loadImageIcon(AssetPaths.GAME_TITLE);
        Image titleImage = titleIcon.getImage();
        titleLabel = new JLabel(new ImageIcon(titleImage));

        // Buttons
        startGameButton = createButton(AssetPaths.NEWGAME_BUTTON); // Update with actual button image paths
        helpButton = createButton(AssetPaths.HELP_BUTTON);      // Update with actual button image paths
        exitButton = createButton(AssetPaths.EXIT_BUTTON);      // Update with actual button image paths

        // Add button logic
        startGameButton.addActionListener(e -> {
            dispose();
            Hall earthHall = new Hall("Earth Hall", 8, 8, 6);
            new BuildModeController(earthHall);
        });

        helpButton.addActionListener(e -> {
            HelpDialog helpDialog = new HelpDialog(RokueLikeMainMenu.this);
            helpDialog.setVisible(true);
        });

        exitButton.addActionListener(e -> System.exit(0));

        backgroundPanel.setLayout(null); // Use absolute layout for full control
        backgroundPanel.add(titleLabel);
        backgroundPanel.add(startGameButton);
        backgroundPanel.add(helpButton);
        backgroundPanel.add(exitButton);

        // Add resize listener
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeComponents();
            }
        });

        resizeComponents(); // Initial positioning
    }

    private ImageIcon loadImageIcon(String resourcePath) {
        try {
            URL resourceUrl = getClass().getClassLoader().getResource(resourcePath.substring(1));
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
        // Create a simple colored rectangle as fallback
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
        styleButton(button);
        return button;
    }

    private void styleButton(JButton button) {
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
    }

    private void resizeComponents() {
        int frameWidth = getWidth();
        int frameHeight = getHeight();

        // Scale and position title
        int titleWidth = frameWidth / 3;
        int titleHeight = titleWidth / 3;
        titleLabel.setBounds((frameWidth - titleWidth) / 2, frameHeight / 10, titleWidth, titleHeight);
        scaleImageIcon(titleLabel, titleWidth, titleHeight);

        // Scale and position buttons
        int buttonWidth = frameWidth / 8;
        int buttonHeight = buttonWidth / 2;

        int buttonX = (frameWidth - buttonWidth) / 2;
        int buttonYStart = frameHeight / 2 - buttonHeight;
        int buttonSpacing = buttonHeight + 20;

        startGameButton.setBounds(buttonX, buttonYStart, buttonWidth, buttonHeight);
        helpButton.setBounds(buttonX, buttonYStart + buttonSpacing, buttonWidth, buttonHeight);
        exitButton.setBounds(buttonX, buttonYStart + 2 * buttonSpacing, buttonWidth, buttonHeight);

        // Scale button images
        for (JButton button : new JButton[]{startGameButton, helpButton, exitButton}) {
            scaleImageIcon(button, buttonWidth, buttonHeight);
        }
    }

    private void scaleImageIcon(JComponent component, int width, int height) {
        if (component instanceof JLabel) {
            JLabel label = (JLabel) component;
            if (label.getIcon() != null) {
                Image scaledImage = ((ImageIcon) label.getIcon()).getImage()
                        .getScaledInstance(width, height, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaledImage));
            }
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            if (button.getIcon() != null) {
                Image scaledImage = ((ImageIcon) button.getIcon()).getImage()
                        .getScaledInstance(width, height, Image.SCALE_SMOOTH);
                button.setIcon(new ImageIcon(scaledImage));
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