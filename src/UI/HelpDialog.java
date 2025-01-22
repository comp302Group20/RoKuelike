package UI;

import Utils.GameFonts;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyledDocument;
import java.awt.*;

public class HelpDialog extends JDialog {

    public HelpDialog(Frame owner) {
        super(owner, "Help", true);

        setSize(1200, 500);
        setLocationRelativeTo(owner);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBackground(new Color(28, 26, 37));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("HELP", SwingConstants.CENTER);
        titleLabel.setFont(GameFonts.pixelFont.deriveFont(28f));
        titleLabel.setForeground(new Color(225, 200, 160));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        String helpContent = String.join("\n\n",
                "Use arrow keys to move the hero: UP to move up, DOWN to move down, LEFT to move left, and RIGHT to move right.",
                "Click on objects with the mouse to search for the rune. You can only search objects next to the hero.",
                "Collect enchantments by clicking on them before they disappear. No need to be next to them.",
                "Press 'R' to use the Reveal enchantment if available, highlighting a region where the rune may be.",
                "Press 'P' to use the Cloak of Protection, which hides the hero from certain dangers for 20 seconds.",
                "Press 'B' and then a direction key (A, D, W, S) to throw the Luring Gem in the desired direction.",
                "Watch the timer and make sure to find the rune before time runs out.",
                "Check the hero's bag on the screen to see available enchantments and their quantities.",
                "Use the Pause button to pause/resume the game.",
                "Use the Exit button to return to the Main Menu."
        );

        JTextPane helpText = new JTextPane();
        helpText.setEditable(false);
        helpText.setFont(GameFonts.pixelFont.deriveFont(18f));
        helpText.setForeground(new Color(225, 180, 160));
        helpText.setBackground(new Color(28, 26, 37));
        helpText.setText(helpContent);
        helpText.setBorder(BorderFactory.createLineBorder(new Color(225, 200, 160), 2));

        // Center align the text
        StyledDocument doc = helpText.getStyledDocument();
        SimpleAttributeSet center = new SimpleAttributeSet();
        javax.swing.text.StyleConstants.setAlignment(center, javax.swing.text.StyleConstants.ALIGN_CENTER);
        doc.setParagraphAttributes(0, doc.getLength(), center, false);

        // Add padding
        helpText.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane textScroll = new JScrollPane(helpText);
        textScroll.setBorder(null);
        textScroll.getVerticalScrollBar().setUnitIncrement(16);

        JButton closeButton = new JButton("CLOSE");
        closeButton.setFont(GameFonts.pixelFont.deriveFont(20f));
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
}