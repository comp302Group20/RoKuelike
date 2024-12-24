<<<<<<< HEAD
package UI;

import javax.swing.*;
import Domain.HeroMovementExample;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RokueLikeMainMenu extends JFrame {
    public RokueLikeMainMenu() {
        setTitle("Rokue-Like - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        // Load the background image from resources
        ImageIcon backgroundIcon = new ImageIcon(getClass().getResource("/rokue-like-assets/Rokue-like-logo-4.png"));
        Image backgroundImage = backgroundIcon.getImage();

        // Create the background panel with the image
        BackgroundPanel backgroundPanel = new BackgroundPanel(backgroundImage);

        // Set the background panel as the content pane
        setContentPane(backgroundPanel);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        //Use box layout
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        
        // Create buttons
        JButton startGameButton = new JButton("Start a New Game");
        JButton helpButton = new JButton("Help");
        JButton exitButton = new JButton("Exit");

        // Ensure each component is centered
        startGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add them with vertical spacing
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(startGameButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(helpButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(exitButton);
        buttonPanel.add(Box.createVerticalGlue());
        // Set button sizes and align them
        Dimension buttonSize = new Dimension(150, 40);
        startGameButton.setMaximumSize(buttonSize);
        helpButton.setMaximumSize(buttonSize);
        exitButton.setMaximumSize(buttonSize);

        // Add some rigid areas for spacing
        buttonPanel.add(Box.createVerticalStrut(30));
        buttonPanel.add(startGameButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(helpButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(exitButton);
        buttonPanel.add(Box.createVerticalStrut(30));

        
        // Add action listeners to buttons
        startGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HeroMovementExample example = new HeroMovementExample();
                example.setVisible(true);
            }
        });

        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(RokueLikeMainMenu.this,
                        "Rokue-Like Help:\n- Click 'Start a New Game' to begin.\n" +
                                "- Move the hero with arrow keys.\n" +
                                "- Collect runes to proceed.\n" +
                                "- Avoid or distract monsters.\n" +
                                "- Find all runes before time runs out.",
                        "Help", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Add buttons to the panel
        buttonPanel.add(startGameButton);
        buttonPanel.add(helpButton);
        buttonPanel.add(exitButton);

        // Add a title label or logo above the buttons
        JLabel titleLabel = new JLabel("Rokue-Like", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        titleLabel.setOpaque(false);

        // Add components to the background panel
        backgroundPanel.add(titleLabel, BorderLayout.NORTH);
        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                RokueLikeMainMenu menu = new RokueLikeMainMenu();
                menu.setVisible(true);
            }
        });
    }
}
=======
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
>>>>>>> origin/development
