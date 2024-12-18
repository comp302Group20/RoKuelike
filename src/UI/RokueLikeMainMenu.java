package UI;

import Controller.BuildModeController;
import Controller.GameController;
import Domain.Hall;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RokueLikeMainMenu extends JFrame {

    public RokueLikeMainMenu() {
        setTitle("Rokue-Like - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        setLocationRelativeTo(null);

        // Load a background image (adjust path if needed)
        ImageIcon backgroundIcon = new ImageIcon("res/rokue-like-assets/Rokue-like logo 4.png");
        Image backgroundImage = backgroundIcon.getImage();

        // Create the background panel
        BackgroundPanel backgroundPanel = new BackgroundPanel(backgroundImage);
        setContentPane(backgroundPanel);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        // Buttons
        JButton startGameButton = new JButton("Start a New Game");
        JButton helpButton = new JButton("Help");
        JButton exitButton = new JButton("Exit");

        // Center alignment
        startGameButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        helpButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Spacing
        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(startGameButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(helpButton);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(exitButton);
        buttonPanel.add(Box.createVerticalGlue());

        Dimension buttonSize = new Dimension(150, 40);
        startGameButton.setMaximumSize(buttonSize);
        helpButton.setMaximumSize(buttonSize);
        exitButton.setMaximumSize(buttonSize);

        // Add button logic
        startGameButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Dispose main menu window before starting the game
                dispose();

                // Launch GameController with a new Earth Hall
                // Adjust hall size and minObjectCount as needed
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

        // Optional title
        JLabel titleLabel = new JLabel("Rokue-Like", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10));
        titleLabel.setOpaque(false);

        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.add(titleLabel, BorderLayout.NORTH);
        backgroundPanel.add(buttonPanel, BorderLayout.CENTER);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            RokueLikeMainMenu menu = new RokueLikeMainMenu();
            menu.setVisible(true);
        });
    }
}
