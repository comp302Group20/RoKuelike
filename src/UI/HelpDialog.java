package UI;

import javax.swing.*;
import java.awt.*;

public class HelpDialog extends JDialog {

    public HelpDialog(Frame owner) {
        super(owner, "Help", true);
        setSize(300, 200);
        setLocationRelativeTo(owner);

        JTextArea helpText = new JTextArea();
        helpText.setEditable(false);
        helpText.setText(
                "Rokue-Like Help:\n\n" +
                        "- In Build Mode, place objects by selecting them \n" +
                        "  and clicking on the grid.\n" +
                        "- In Play Mode, move the hero with arrow keys.\n" +
                        "- Collect runes to proceed. \n" +
                        "- Avoid or distract monsters.\n" +
                        "- Find all runes before time runs out."
        );

        add(new JScrollPane(helpText), BorderLayout.CENTER);
    }
}
