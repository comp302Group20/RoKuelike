package UI;

import Controller.BuildModeController;
import Domain.Hall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

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

        // Load a background image (adjust path if needed)
        ImageIcon backgroundIcon = new ImageIcon("res/rokue-like-assets/mainmenu.png");
        Image backgroundImage = backgroundIcon.getImage();

        // Create the background panel
        BackgroundPanel backgroundPanel = new BackgroundPanel(backgroundImage);
        setContentPane(backgroundPanel);

        // Add title image
        ImageIcon titleIcon = new ImageIcon("res/rokue-like-assets/title2.png");
        Image titleImage = titleIcon.getImage();
        titleLabel = new JLabel(new ImageIcon(titleImage));

        // Buttons
        startGameButton = createButton("res/rokue-like-assets/newgamebutton.png");
        helpButton = createButton("res/rokue-like-assets/helpbutton.png");
        exitButton = createButton("res/rokue-like-assets/exitbutton.png");

        // Add button logic
        startGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
                Hall earthHall = new Hall("Earth Hall", 8, 8, 6);
                new BuildModeController(earthHall);
            }
        });

        helpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                HelpDialog helpDialog = new HelpDialog(RokueLikeMainMenu.this);
                helpDialog.setVisible(true);
            }
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

    private JButton createButton(String imagePath) {
        ImageIcon icon = new ImageIcon(imagePath);
        Image image = icon.getImage();
        JButton button = new JButton(new ImageIcon(image));
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
        titleLabel.setIcon(new ImageIcon(((ImageIcon) titleLabel.getIcon()).getImage().getScaledInstance(titleWidth, titleHeight, Image.SCALE_SMOOTH)));

        // Scale and position buttons
        int buttonWidth = frameWidth / 8;
        int buttonHeight = buttonWidth / 2;

        int buttonX = (frameWidth - buttonWidth) / 2;
        int buttonYStart = frameHeight / 2 - buttonHeight;
        int buttonSpacing = buttonHeight + 20;

        startGameButton.setBounds(buttonX, buttonYStart, buttonWidth, buttonHeight);
        helpButton.setBounds(buttonX, buttonYStart + buttonSpacing, buttonWidth, buttonHeight);
        exitButton.setBounds(buttonX, buttonYStart + 2 * buttonSpacing, buttonWidth, buttonHeight);

        for (JButton button : new JButton[]{startGameButton, helpButton, exitButton}) {
            button.setIcon(new ImageIcon(((ImageIcon) button.getIcon()).getImage().getScaledInstance(buttonWidth, buttonHeight, Image.SCALE_SMOOTH)));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RokueLikeMainMenu menu = new RokueLikeMainMenu();
            menu.setVisible(true);
        });
    }
}
