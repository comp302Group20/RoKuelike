package UI;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * A simple JDialog that shows instructions for how to play.
 * You could extend or update the text to match your final gameplay logic.
 */
public class HelpDialog extends JDialog {

    public HelpDialog(Frame owner) {
        super(owner, "Help", true);

        // Attempt to load a pixel font (optional)
        Font pixelFont = null;
        try {
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, new File("resources/fonts/pixelFont.ttf")).deriveFont(22f);
        } catch (FontFormatException | IOException e) {
            e.printStackTrace();
            pixelFont = new Font("Monospaced", Font.BOLD, 22);
        }

        setSize(1200, 500);
        setLocationRelativeTo(owner);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(new Color(28, 26, 37));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("HELP", SwingConstants.CENTER);
        titleLabel.setFont(pixelFont.deriveFont(28f));
        titleLabel.setForeground(new Color(225, 200, 160));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        JTextArea helpText = new JTextArea();
        helpText.setEditable(false);
        helpText.setFont(pixelFont.deriveFont(18f));
        helpText.setForeground(new Color(225, 180, 160));
        helpText.setBackground(new Color(28, 26, 37));
        helpText.setText(
                "- Use arrow keys to move the hero: ↑ to move up, ↓ to move down, ← to move left, and → to move right.\n" +
                        "- Click on objects with the mouse to search for the rune. You can only search objects next to the hero.\n" +
                        "- Collect enchantments by clicking on them before they disappear. No need to be next to them.\n" +
                        "- Press 'R' to use the Reveal enchantment if available, highlighting a region where the rune may be.\n" +
                        "- Press 'P' to use the Cloak of Protection, which hides the hero from certain dangers for 20 seconds.\n" +
                        "- Press 'B' and then a direction key (A, D, W, S) to throw the Luring Gem in the desired direction.\n" +
                        "- Watch the timer and make sure to find the rune before time runs out.\n" +
                        "- Check the hero's bag on the screen to see available enchantments and their quantities.\n" +
                        "- Use the Pause button to pause/resume the game.\n" +
                        "- Use the Exit button to return to the Main Menu."
        );
        helpText.setBorder(BorderFactory.createLineBorder(new Color(225, 200, 160), 2));

        JScrollPane textScroll = new JScrollPane(helpText);
        textScroll.setBorder(null);

        JButton closeButton = new JButton("CLOSE");
        closeButton.setFont(pixelFont.deriveFont(20f));
        closeButton.setBackground(new Color(225, 200, 160));
        closeButton.setForeground(new Color(28, 26, 37));
        closeButton.setFocusPainted(false);
        closeButton.addActionListener(e -> dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(28, 26, 37));
        buttonPanel.add(closeButton);

        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(textScroll, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
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
