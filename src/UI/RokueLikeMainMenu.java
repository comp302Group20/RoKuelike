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
