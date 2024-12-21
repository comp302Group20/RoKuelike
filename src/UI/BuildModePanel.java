package UI;

import Domain.Hall;
import Utils.AssetPaths;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.List;

public class BuildModePanel extends JPanel {
    private Hall currentHall;
    private int cellSize = 64;
    private BufferedImage floorImage;
    private JButton finishButton;
    private JPanel objectColumnPanel;
    private boolean resourcesLoaded = false;

    public BuildModePanel(Hall hall) {
        this.currentHall = hall;
        setPreferredSize(new Dimension(1600, 900));
        setLayout(null);

        initializeFloorImage();
        initializeUI();
        loadObjects();
    }

    private void initializeFloorImage() {
        try {
            // Use the ClassLoader to get the resource
            URL resourceUrl = getClass().getClassLoader().getResource(AssetPaths.SPRITESHEET.substring(1));
            System.out.println("Sprite sheet Resource URL: " + resourceUrl);

            if (resourceUrl == null) {
                System.err.println("Could not find sprite sheet at: " + AssetPaths.SPRITESHEET);
                throw new IOException("Sprite sheet resource not found");
            }

            BufferedImage spriteSheet = ImageIO.read(resourceUrl);
            if (spriteSheet == null) {
                throw new IOException("Failed to load sprite sheet: " + AssetPaths.SPRITESHEET);
            }

            floorImage = spriteSheet.getSubimage(32, 48, 16, 16);
            resourcesLoaded = true;
        } catch (IOException e) {
            System.err.println("Error loading sprite sheet: " + e.getMessage());
            e.printStackTrace();
            floorImage = createFallbackImage();
        }
    }

    private BufferedImage createFallbackImage() {
        BufferedImage fallback = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = fallback.createGraphics();
        g2d.setColor(Color.GRAY);
        g2d.fillRect(0, 0, 16, 16);
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, 15, 15);
        g2d.dispose();
        return fallback;
    }

    private void loadObjects() {
        List<String> objectImagePaths = List.of(
                AssetPaths.SPRITESHEET  // Example - replace with actual object paths
        );

        for (String imagePath : objectImagePaths) {
            try {
                // Remove leading slash and use ClassLoader
                URL resourceUrl = getClass().getClassLoader().getResource(imagePath.substring(1));
                System.out.println("Loading object from: " + resourceUrl);

                if (resourceUrl == null) {
                    System.err.println("Resource not found: " + imagePath);
                    continue;
                }

                BufferedImage objectImage = ImageIO.read(resourceUrl);
                if (objectImage == null) {
                    System.err.println("Failed to load image: " + imagePath);
                    continue;
                }

                JPanel objectPanel = createObjectPanel(objectImage);
                objectColumnPanel.add(objectPanel);
                objectColumnPanel.add(Box.createVerticalStrut(10));

            } catch (IOException e) {
                System.err.println("Error loading image " + imagePath + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private JPanel createObjectPanel(BufferedImage objectImage) {
        JPanel objectPanel = new JPanel();
        objectPanel.setLayout(new BorderLayout());
        objectPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        Image scaledImage = objectImage.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
        JLabel objectLabel = new JLabel(new ImageIcon(scaledImage));

        objectPanel.add(objectLabel, BorderLayout.CENTER);
        objectPanel.setMaximumSize(new Dimension(190, 80));
        makeObjectDraggable(objectPanel);

        return objectPanel;
    }

    private void initializeUI() {
        // Initialize Finish button
        finishButton = new JButton("Finish");
        finishButton.setBounds(1400, 800, 150, 50);
        finishButton.addActionListener(e -> startPlayMode());
        add(finishButton);

        // Initialize object column panel
        objectColumnPanel = new JPanel();
        objectColumnPanel.setBounds(1400, 0, 200, 800);
        objectColumnPanel.setLayout(new BoxLayout(objectColumnPanel, BoxLayout.Y_AXIS));
        objectColumnPanel.setBorder(BorderFactory.createTitledBorder("Available Objects"));
        objectColumnPanel.setBackground(new Color(240, 240, 240));
        add(objectColumnPanel);
    }

    private void makeObjectDraggable(JPanel objectPanel) {
        MouseAdapter dragAdapter = new MouseAdapter() {
            Point offset;

            @Override
            public void mousePressed(MouseEvent e) {
                offset = e.getPoint();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (offset != null) {
                    Point newLocation = objectPanel.getLocation();
                    newLocation.translate(e.getX() - offset.x, e.getY() - offset.y);
                    objectPanel.setLocation(newLocation);
                }
            }
        };

        objectPanel.addMouseListener(dragAdapter);
        objectPanel.addMouseMotionListener(dragAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        drawGrid(g);
        drawGridLines(g);
    }

    private void drawGrid(Graphics g) {
        if (!resourcesLoaded) {
            g.setColor(Color.RED);
            g.drawString("Error: Resources not loaded properly", 50, 50);
            return;
        }

        for (int r = 0; r < 13; r++) {
            for (int c = 0; c < 13; c++) {
                int drawX = c * cellSize;
                int drawY = r * cellSize;
                g.drawImage(floorImage, drawX, drawY, cellSize, cellSize, null);
            }
        }
    }

    private void drawGridLines(Graphics g) {
        g.setColor(new Color(200, 200, 200, 100));

        for (int x = 0; x <= 13; x++) {
            g.drawLine(x * cellSize, 0, x * cellSize, 13 * cellSize);
        }

        for (int y = 0; y <= 13; y++) {
            g.drawLine(0, y * cellSize, 13 * cellSize, y * cellSize);
        }
    }

    private void startPlayMode() {
        JFrame playModeFrame = new JFrame("Play Mode");
        playModeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        playModeFrame.setSize(1600, 900);
        playModeFrame.setLocationRelativeTo(null);

        JPanel emptyPanel = new JPanel();
        playModeFrame.add(emptyPanel);

        playModeFrame.setVisible(true);
        SwingUtilities.getWindowAncestor(this).dispose();
    }
}