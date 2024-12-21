package UI;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class HelpDialog extends JDialog {

    public HelpDialog(Frame owner) {
        super(owner, "Help", true);

        // Load pixel-style font
        Font pixelFont = null;
        try {
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, new File("resources/fonts/pixelFont.ttf")).deriveFont(22f);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            pixelFont = new Font("Monospaced", Font.BOLD, 22); // Fallback font
        }

        // Set dialog size and location
        setSize(600, 400); // Increased size to fit content
        setLocationRelativeTo(owner);

        // Create a JPanel for content with medieval aesthetic styling
        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(new Color(28, 26, 37)); // Dark stone-like background
        contentPanel.setLayout(new BorderLayout(10, 10)); // Padding between elements
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Title Label
        JLabel titleLabel = new JLabel("HELP", SwingConstants.CENTER);
        titleLabel.setFont(pixelFont.deriveFont(28f)); // Slightly larger for the title
        titleLabel.setForeground(new Color(225, 200, 160)); // Warm torchlight yellow
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        // Help Text Area
        JTextArea helpText = new JTextArea();
        helpText.setEditable(false);
        helpText.setFont(pixelFont.deriveFont(18f)); // Smaller pixel-style font for content
        helpText.setForeground(new Color(225, 180, 160)); // Warm parchment tone
        helpText.setBackground(new Color(28, 26, 37)); // Stone-like background
        helpText.setText(
                "- In Build Mode, place objects by selecting them\n" +
                        "  and clicking on the grid.\n" +
                        "- In Play Mode, move the hero with arrow keys.\n" +
                        "- Collect runes to proceed.\n" +
                        "- Avoid or distract monsters.\n" +
                        "- Find all runes before time runs out."
        );
        helpText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 200, 160), 2), // Torchlight yellow border
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        // Center panel to fit content
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(28, 26, 37)); // Match background
        centerPanel.add(helpText, BorderLayout.CENTER);

        // Close Button
        JButton closeButton = new JButton("CLOSE");
        closeButton.setFont(pixelFont.deriveFont(20f)); // Pixel-style button font
        closeButton.setBackground(new Color(225, 200, 160)); // Torchlight yellow
        closeButton.setForeground(new Color(28, 26, 37)); // Dark stone text color
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(45, 41, 56), 2), // Stone-like border
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        closeButton.addActionListener(e -> dispose());

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(28, 26, 37)); // Match background
        buttonPanel.add(closeButton);

        // Add components to the content panel
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(centerPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add content panel to dialog
        add(contentPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            HelpDialog dialog = new HelpDialog(frame);
            dialog.setVisible(true);
        });
    }
}