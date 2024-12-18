package Controller;

import Domain.Hall;
import Domain.GameObject;
import Domain.SpriteLocation;
import UI.BuildModePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class BuildModeController {
    private JFrame frame;
    private BuildModePanel buildPanel;
    private Hall hall;

    public BuildModeController(Hall hall) {
        this.hall = hall;
        initializeUI();
    }

    private void initializeUI() {
        frame = new JFrame("Build Mode - " + hall.getName());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1024, 640);
        frame.setLocationRelativeTo(null);

        buildPanel = new BuildModePanel(hall);

        JPanel sidePanel = createSidePanel();

        frame.setLayout(new BorderLayout());
        frame.add(buildPanel, BorderLayout.CENTER);
        frame.add(sidePanel, BorderLayout.EAST);

        JButton finishButton = new JButton("Finish Build");
        finishButton.addActionListener(e -> {
            if (hall.validateObjectCount()) {
                JOptionPane.showMessageDialog(frame,
                        "You have placed enough objects. Build Mode complete!");
                frame.dispose();
            } else {
                JOptionPane.showMessageDialog(frame,
                        "Not enough objects in " + hall.getName() + "!");
            }
        });
        frame.add(finishButton, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private JPanel createSidePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setPreferredSize(new Dimension(140, 600));
        panel.setOpaque(false);

        // Object data format: Name, SpriteSheet Path, x, y, width, height
        Object[][] objectsData = {
                {"Pillar", "/rokue-like-assets/spritesheet.png", 32, 96, 32, 32},
                {"Barrel", "/rokue-like-assets/spritesheet.png", 64, 96, 32, 32},
                {"Chest", "/rokue-like-assets/spritesheet.png", 96, 96, 32, 32},
                {"DoubleChest", "/rokue-like-assets/spritesheet.png", 128, 96, 64, 32},
                {"Skull", "/rokue-like-assets/spritesheet.png", 192, 96, 32, 32},
                {"Potion", "/rokue-like-assets/spritesheet.png", 224, 96, 32, 32}
        };

        JLabel buildModeLabel = new JLabel("BuildMode", SwingConstants.CENTER);
        buildModeLabel.setFont(new Font("Serif", Font.BOLD, 18));
        panel.add(Box.createVerticalStrut(10));
        panel.add(buildModeLabel);
        panel.add(Box.createVerticalStrut(20));

        for (Object[] objInfo : objectsData) {
            String objectName = (String) objInfo[0];
            String imagePath = (String) objInfo[1];
            int x = (Integer) objInfo[2];
            int y = (Integer) objInfo[3];
            int width = (Integer) objInfo[4];
            int height = (Integer) objInfo[5];

            JButton objButton = new JButton(objectName);
            objButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            try {
                Image spriteSheet = new ImageIcon(getClass().getResource(imagePath)).getImage();
                BufferedImage fullImage = new BufferedImage(
                        spriteSheet.getWidth(null),
                        spriteSheet.getHeight(null),
                        BufferedImage.TYPE_INT_ARGB
                );
                Graphics2D g = fullImage.createGraphics();
                g.drawImage(spriteSheet, 0, 0, null);
                g.dispose();

                BufferedImage subImage = fullImage.getSubimage(x, y, width, height);
                Image scaled = subImage.getScaledInstance(32, 32, Image.SCALE_SMOOTH);
                objButton.setIcon(new ImageIcon(scaled));
            } catch (Exception e) {
                System.err.println("Error loading sprite for: " + objectName);
                e.printStackTrace();
            }

            objButton.addActionListener(e -> {
                SpriteLocation location = new SpriteLocation(x, y, width, height);
                buildPanel.setSelectedObject(new GameObject(objectName, imagePath, location));
            });

            panel.add(objButton);
            panel.add(Box.createVerticalStrut(15));
        }

        return panel;
    }
}