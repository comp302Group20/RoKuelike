package Controller;

import Domain.Hall;
import UI.BuildModePanel;
import UI.GamePanel;
import Domain.GameTimer;
import Utils.AssetPaths;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GameController {
    private Hall hall;
    private GameTimer gameTimer;

    // Keep track of how many halls have been completed
    private static int gamesCompleted = 0;

    // Reference to the playModeFrame so we can dispose it
    private JFrame playModeFrame;

    // Array of the "completed" images to display
    private static final String[] COMPLETED_IMAGES = {
            AssetPaths.COMPLETED1,
            AssetPaths.COMPLETED2,
            AssetPaths.COMPLETED3,
            AssetPaths.COMPLETED4
    };

    public GameController(Hall hall) {
        this.hall = hall;
        new BuildModeController(hall, this);
    }

    public void onBuildModeFinished(BuildModePanel.CellType[][] grid, BuildModePanel.PlacedObject[][] placedObjects) {
        List<BuildModePanel.PlacedObject> allObjects = new ArrayList<>();
        for (int r = 0; r < placedObjects.length; r++) {
            for (int c = 0; c < placedObjects[0].length; c++) {
                if (placedObjects[r][c] != null) {
                    allObjects.add(placedObjects[r][c]);
                }
            }
        }

        // Optionally hide the rune in a random object
        if (!allObjects.isEmpty()) {
            int idx = (int) (Math.random() * allObjects.size());
            allObjects.get(idx).hasRune = true;
        }

        int totalObjects = allObjects.size();
        int startingTime = calculateStartingTime(totalObjects);

        startPlayMode(grid, placedObjects, startingTime);
    }

    private int calculateStartingTime(int totalObjects) {
        // For example, 5 seconds per object
        int baseTimePerObject = 5;
        return totalObjects * baseTimePerObject;
    }

    private void startPlayMode(BuildModePanel.CellType[][] grid,
                               BuildModePanel.PlacedObject[][] placedObjects,
                               int startingTime) {
        playModeFrame = new JFrame("Play Mode - " + hall.getName());
        playModeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        playModeFrame.setSize(1600, 900);
        playModeFrame.setLocationRelativeTo(null);

        GamePanel gamePanel = new GamePanel(grid, placedObjects, this);
        gameTimer = new GameTimer(startingTime);

        gameTimer.start(
                () -> gamePanel.updateTime(gameTimer.getTimeRemaining()),  // UI update
                () -> gamePanel.triggerGameOver()                          // Time ran out
        );

        playModeFrame.add(gamePanel, BorderLayout.CENTER);
        playModeFrame.setVisible(true);
    }

    /**
     * Called by GamePanel when the hero escapes successfully.
     * After each hall, show the "completedN.png" for 3 seconds.
     * Then, if it’s the 4th hall, also show "congratulations.png" before exiting.
     */
    public void onHeroEscaped() {
        // Close the old game frame
        if (playModeFrame != null) {
            playModeFrame.dispose();
        }

        // Increment count of halls completed
        gamesCompleted++;

        // Show the corresponding "completed" image for 3 seconds, then move on
        showCompletionScreen();
    }

    /**
     * Displays the correct "completedX.png" image in a window for 3 seconds,
     * then calls finishOrExit().
     */
    private void showCompletionScreen() {
        // If we've exceeded 4 for some reason, just skip to final logic
        if (gamesCompleted < 1 || gamesCompleted > 4) {
            finishOrExit();
            return;
        }

        // Create a pop-up frame with responsive behavior
        final JFrame completionFrame = new JFrame("Hall Completed");
        completionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        completionFrame.setSize(80, 60); // Initial size; will be maximized
        completionFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize to full screen
        completionFrame.setUndecorated(true); // Remove window borders for full-screen effect

        // Load the correct completed image from resources
        BufferedImage completionImage = loadImage(COMPLETED_IMAGES[gamesCompleted - 1]);

        // Create a responsive image panel
        ResponsiveImagePanel imagePanel = new ResponsiveImagePanel(completionImage, "Hall " + gamesCompleted + " Completed!");
        completionFrame.add(imagePanel, BorderLayout.CENTER);
        completionFrame.setVisible(true);

        // After 3 seconds, dispose of completion frame & proceed
        Timer transitionTimer = new Timer(3000, e -> {
            completionFrame.dispose();
            finishOrExit();
        });
        transitionTimer.setRepeats(false);
        transitionTimer.start();
    }

    /**
     * After showing completed1-3, proceed to next hall.
     * After completed4, display "congratulations.png" instead of a text message, then exit.
     */
    private void finishOrExit() {
        if (gamesCompleted < 4) {
            // Create a new Hall using the same dimensions, different name
            Hall nextHall = new Hall(
                    "Hall Run #" + (gamesCompleted + 1),
                    hall.getRows(),
                    hall.getCols(),
                    hall.getMinObjectCount()
            );
            new GameController(nextHall);

        } else {
            // We've completed the 4th hall; show the congratulations.png
            showCongratulationsScreen();
        }
    }

    /**
     * Displays "congratulations.png" for 3 seconds, then exits the game.
     */
    private void showCongratulationsScreen() {
        final JFrame congratsFrame = new JFrame("Congratulations");
        congratsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        congratsFrame.setSize(80, 60); // Initial size; will be maximized
        congratsFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Maximize to full screen
        congratsFrame.setUndecorated(true); // Remove window borders for full-screen effect

        BufferedImage congratsImage = loadImage(AssetPaths.CONGRATULATIONS);
        ResponsiveImagePanel congratsPanel = new ResponsiveImagePanel(congratsImage, "Congratulations!");
        congratsFrame.add(congratsPanel, BorderLayout.CENTER);
        congratsFrame.setVisible(true);

        Timer timer = new Timer(3000, e -> {
            congratsFrame.dispose();
            // Finally, end the program
            System.exit(0);
        });
        timer.setRepeats(false);
        timer.start();
    }

    // Helper method for loading a BufferedImage from a resource path
    private BufferedImage loadImage(String resourcePath) {
        BufferedImage image = null;
        try {
            URL url = getClass().getResource(resourcePath);
            if (url != null) {
                image = ImageIO.read(url);
            } else {
                System.err.println("Resource not found: " + resourcePath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    // Timer Pause/Resume
    public void pauseGame() {
        if (gameTimer != null) {
            gameTimer.pause();
        }
    }

    public void resumeGame() {
        if (gameTimer != null) {
            gameTimer.resume();
        }
    }

    /**
     * Custom JPanel that scales the image to fit the panel while maintaining aspect ratio.
     */
    private static class ResponsiveImagePanel extends JPanel {
        private BufferedImage image;
        private String fallbackText;

        public ResponsiveImagePanel(BufferedImage image, String fallbackText) {
            this.image = image;
            this.fallbackText = fallbackText;
            this.setBackground(Color.BLACK); // Optional: set background color
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                // Calculate the new size while maintaining aspect ratio
                int panelWidth = this.getWidth();
                int panelHeight = this.getHeight();

                double imageAspect = (double) image.getWidth() / image.getHeight();
                double panelAspect = (double) panelWidth / panelHeight;

                int drawWidth, drawHeight;

                if (panelAspect > imageAspect) {
                    // Panel is wider relative to image
                    drawHeight = panelHeight;
                    drawWidth = (int) (drawHeight * imageAspect);
                } else {
                    // Panel is taller relative to image
                    drawWidth = panelWidth;
                    drawHeight = (int) (drawWidth / imageAspect);
                }

                // Draw the scaled image centered
                int x = (panelWidth - drawWidth) / 2;
                int y = (panelHeight - drawHeight) / 2;
                g.drawImage(image, x, y, drawWidth, drawHeight, this);
            } else {
                // If image is missing, display fallback text
                g.setColor(Color.WHITE);
                g.setFont(new Font("Arial", Font.BOLD, 36));
                FontMetrics fm = g.getFontMetrics();
                int textWidth = fm.stringWidth(fallbackText);
                int textHeight = fm.getHeight();
                int x = (getWidth() - textWidth) / 2;
                int y = (getHeight() + textHeight) / 2;
                g.drawString(fallbackText, x, y);
            }
        }
    }
}
