package UI;

import javax.swing.*;
import java.awt.*;

public class HelpDialog extends JDialog {

    public HelpDialog(Frame owner) {
        super(owner, "Help", true);

        // Set dialog size and location
        setSize(500, 400);
        setLocationRelativeTo(owner);

        // Create a JPanel for content with modern aesthetic styling
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(new Color(34, 40, 49)); // Dark background color
        contentPanel.setLayout(new BorderLayout(15, 15)); // Padding between elements
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title Label
        JLabel titleLabel = new JLabel("Rokue-Like Help", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Roboto", Font.BOLD, 24));
        titleLabel.setForeground(new Color(255, 211, 105)); // Soft yellow
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        // Help Text Area
        JTextArea helpText = new JTextArea();
        helpText.setEditable(false);
        helpText.setFont(new Font("Roboto", Font.PLAIN, 16));
        helpText.setForeground(Color.WHITE);
        helpText.setBackground(new Color(57, 62, 70)); // Darker gray background
        helpText.setText(
                "- In Build Mode, place objects by selecting them \n" +
                        "  and clicking on the grid.\n" +
                        "- In Play Mode, move the hero with arrow keys.\n" +
                        "- Collect runes to proceed.\n" +
                        "- Avoid or distract monsters.\n" +
                        "- Find all runes before time runs out."
        );
        helpText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 211, 105), 2),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Scroll Pane for the Text Area
        JScrollPane scrollPane = new JScrollPane(helpText);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        // Add components to the content panel
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(scrollPane, BorderLayout.CENTER);

        // Add content panel to the dialog
        add(contentPanel);

        // Close button with modern style
        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Roboto", Font.PLAIN, 16));
        closeButton.setBackground(new Color(255, 211, 105)); // Matching soft yellow
        closeButton.setForeground(new Color(34, 40, 49)); // Dark text color
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(57, 62, 70), 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(34, 40, 49));
        buttonPanel.add(closeButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }
}