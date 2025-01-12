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

    // Keep track of how many halls have been completed (0 to 4).
    private static int gamesCompleted = 0;

    // This reflects the "current" hall we are building/playing (1-based).
    private int currentHallNumber;

    // Reference to the playModeFrame so we can dispose it
    private JFrame playModeFrame;

    // Array of the "completed" images to display
    private static final String[] COMPLETED_IMAGES = {
            AssetPaths.COMPLETED1,
            AssetPaths.COMPLETED2,
            AssetPaths.COMPLETED3,
            AssetPaths.COMPLETED4
    };

    /**
     * Minimum objects required for each hall:
     * 1st hall -> 6, 2nd -> 9, 3rd -> 13, 4th -> 17
     */
    private static final int[] MIN_OBJECTS = {6, 9, 13, 17};

    public GameController(Hall hall) {
        this.hall = hall;
        // currentHallNumber is always gamesCompleted + 1
        // e.g., if 0 halls completed, we’re on hall #1
        this.currentHallNumber = gamesCompleted + 1;

        new BuildModeController(hall, this);
    }

    /**
     * Called after the user presses "Finish Building" in build mode.
     * We must enforce the min object count before starting the play mode.
     */

    public GameTimer getGameTimer() {
        return this.gameTimer;
    }

    public void onBuildModeFinished(BuildModePanel.CellType[][] grid, BuildModePanel.PlacedObject[][] placedObjects) {
        // Count how many objects were placed
        int placedObjectCount = 0;
        for (int r = 0; r < placedObjects.length; r++) {
            for (int c = 0; c < placedObjects[0].length; c++) {
                if (placedObjects[r][c] != null) {
                    placedObjectCount++;
                }
            }
        }

        // If not enough objects, show "Not enough objects in Earth/Air/Water/Fire Hall"
        if (!hall.validateObjectCount(placedObjectCount)) {
            String namehall="";
            if(currentHallNumber==1){
                namehall="Earth Hall";
            }
            else if(currentHallNumber==2){
                namehall="Air Hall";
            }
            else if(currentHallNumber==3){
                namehall="Water Hall";
            }
            else if(currentHallNumber==4){
                namehall="Fire Hall";
            }
            else {
                namehall="Unknown Hall";
            }


            JOptionPane.showMessageDialog(null,

                    "Not enough objects in " + namehall + "!\n" +
                            "You need at least " + hall.getMinObjectCount() + " objects.");
            return; // Do not proceed to play mode
        }

        int startingTime = calculateStartingTime(placedObjectCount);
        startPlayMode(grid, placedObjects, startingTime);
    }

    /**
     * Returns the proper name for each hall number:
     *  1 -> Earth Hall
     *  2 -> Air Hall
     *  3 -> Water Hall
     *  4 -> Fire Hall
     */
    private String getHallDescriptor(int hallNumber) {
        switch (hallNumber) {
            case 1: return "Earth Hall";
            case 2: return "Air Hall";
            case 3: return "Water Hall";
            case 4: return "Fire Hall";
            default: return "??? Hall";
        }
    }

    private int calculateStartingTime(int totalObjects) {
        // For example, 5 seconds per object
        int baseTimePerObject = 5;
        return totalObjects * baseTimePerObject;
    }

    private void startPlayMode(BuildModePanel.CellType[][] grid,
                               BuildModePanel.PlacedObject[][] placedObjects,
                               int startingTime) {
        playModeFrame = new JFrame("Play Mode - " + getHallDescriptor(currentHallNumber));
        playModeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        playModeFrame.setSize(1600, 900);
        playModeFrame.setLocationRelativeTo(null);

        GamePanel gamePanel = new GamePanel(grid, placedObjects, this);
        gameTimer = new GameTimer(startingTime);

        gameTimer.start(
                () -> gamePanel.updateTime(gameTimer.getTimeRemaining()),  // UI update callback
                () -> gamePanel.triggerGameOver()                          // Time ran out callback
        );

        playModeFrame.add(gamePanel, BorderLayout.CENTER);
        playModeFrame.setVisible(true);
    }

    /**
     * Called by GamePanel when the hero escapes successfully.
     * After each hall, show the "completedN.png" for 3 seconds,
     * then proceed to the next hall or final screen.
     */
    public void onHeroEscaped() {
        // Close the old game frame
        if (playModeFrame != null) {
            playModeFrame.dispose();
        }

        // Increment count of halls completed (1..4)
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
        ResponsiveImagePanel imagePanel = new ResponsiveImagePanel(
                completionImage,
                "Hall " + gamesCompleted + " Completed!"
        );
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
     * After completed4, display "congratulations.png" then exit.
     */
    private void finishOrExit() {
        if (gamesCompleted < 4) {
            // Prepare the next hall with the correct min object count
            int nextMin = MIN_OBJECTS[gamesCompleted]; // gamesCompleted is now 1..3 for next hall
            String namehall="";
            if(gamesCompleted+1==1){
                namehall="Earth Hall";
            }
            else if(gamesCompleted+1==2){
                namehall="Air Hall";
            }
            else if(gamesCompleted+1==3){
                namehall="Water Hall";
            }
            else if(gamesCompleted+1==4){
                namehall="Fire Hall";
            }
            else {
                namehall="Unknown Hall";
            }
            Hall nextHall = new Hall(
                    // e.g. "Hall Run #2", "Hall Run #3", etc.
                    namehall,
                    hall.getRows(),
                    hall.getCols(),
                    nextMin
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
