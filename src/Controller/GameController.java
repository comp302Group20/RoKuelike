package Controller;

import Domain.*;
import UI.BuildModePanel;
import UI.GamePanel;
import Utils.AssetPaths;
import UI.PausePopUp;
import UI.RokueLikeMainMenu;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Uses a Swing Timer for short transitions (e.g., 3-second "Completed" screens),
 * but uses Domain.GameTimer for in-game countdown logic.
 */
public class GameController {

    private Hall hall;
    private GamePanel gamePanel;
    // Use the Domain-specific GameTimer, not java.util.Timer:
    private GameTimer gameTimer;

    private int startingTime;
    private int timeRemaining;
    private boolean isPaused;
    private PausePopUp pausePopup;

    private JFrame playModeFrame;

    private static final int WINDOW_WIDTH = 1100;
    private static final int WINDOW_HEIGHT = 900;
    private static final int TIME_PER_OBJECT = 5; // seconds
    private static Inventory persistentInventory = null;

    // Keep track of how many halls have been completed (0 to 4).
    private static int gamesCompleted = 0;

    // This reflects the "current" hall we are building/playing (1-based).
    private int currentHallNumber;

    public GameState gameState;

    // Array of the "completed" images to display (1..4)
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
        this.currentHallNumber = gamesCompleted + 1;
        this.isPaused = false;
        this.timeRemaining = 0;
        this.gameState = new GameState();

        // Don't reset persistent inventory if it exists
        if (persistentInventory == null) {
            persistentInventory = new Inventory();
        }
    }

    public GameTimer getGameTimer() {
        return this.gameTimer;
    }

    /**
     * Called after the user presses "Finish Building" in build mode.
     * We must enforce the min object count before starting the play mode.
     */
    public void onBuildModeFinished(BuildModePanel.CellType[][] grid,
                                    BuildModePanel.PlacedObject[][] placedObjects,
                                    Inventory previousInventory) {
        // Count how many objects were placed
        int placedObjectCount = 0;
        for (int r = 0; r < placedObjects.length; r++) {
            for (int c = 0; c < placedObjects[0].length; c++) {
                if (placedObjects[r][c] != null) {
                    placedObjectCount++;
                }
            }
        }

        // Calculate initial time based on number of objects
        timeRemaining = placedObjectCount * TIME_PER_OBJECT;

        // If not enough objects, show a warning and do not proceed
        if (!hall.validateObjectCount(placedObjectCount)) {
            String namehall;
            switch (currentHallNumber) {
                case 1:  namehall = "Earth Hall";  break;
                case 2:  namehall = "Air Hall";    break;
                case 3:  namehall = "Water Hall";  break;
                case 4:  namehall = "Fire Hall";   break;
                default: namehall = "Unknown Hall";
            }
            JOptionPane.showMessageDialog(
                    null,
                    "Not enough objects in " + namehall + "!\n"
                            + "You need at least " + hall.getMinObjectCount() + " objects."
            );
            return; // Stop -- do not start play mode
        }

        startingTime = timeRemaining;

        // Start the actual play mode with the previous inventory
        startPlayMode(grid, placedObjects, startingTime, previousInventory);
    }

    /**
     * Returns the proper name for each hall number:
     * 1 -> Earth Hall
     * 2 -> Air Hall
     * 3 -> Water Hall
     * 4 -> Fire Hall
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

    /**
     * For demonstration, simple calculation: totalObjects * 5 seconds each.
     */
    private int calculateStartingTime(int totalObjects) {
        int baseTimePerObject = 5;
        return totalObjects * baseTimePerObject;
    }

    /**
     * Creates the Play Mode window, initializes the Domain.GameTimer,
     * and starts the countdown.
     */
    private void startPlayMode(BuildModePanel.CellType[][] grid,
                               BuildModePanel.PlacedObject[][] placedObjects,
                               int startingTime,
                               Inventory previousInventory) {  // Add this parameter
        // Close any existing window (in case there's one open)
        closeGame();

        playModeFrame = new JFrame("Play Mode - " + getHallDescriptor(currentHallNumber));
        playModeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        playModeFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        playModeFrame.setLocationRelativeTo(null);

        // Reset hero
        Hero.reset();
        Hero newHero = Hero.getInstance(0, 0, 64, 64);

        // Set the inventory based on whether this is a new game or continuing
        if (gamesCompleted == 0) {
            // New game - clear inventory
            newHero.getInventory().clearEnchantments();
        } else if (previousInventory != null) {
            // Continuing game - use previous inventory
            newHero.getInventory().setEnchantments(
                    new ArrayList<>(previousInventory.getCollectedEnchantments())
            );
        }

        // Create new GamePanel with the hero
        gamePanel = new GamePanel(grid, placedObjects, this);

        gameTimer = new GameTimer(startingTime);
        gameTimer.start(
                () -> {
                    // Update both GamePanel and Controller's time
                    this.timeRemaining = gameTimer.getTimeRemaining();  // Add this line
                    gamePanel.updateTime(gameTimer.getTimeRemaining());
                },
                () -> gamePanel.triggerGameOver()
        );

        playModeFrame.add(gamePanel, BorderLayout.CENTER);
        playModeFrame.setVisible(true);

        // Initialize pause pop-up (for pausing) once the frame is ready
        pausePopup = new PausePopUp(playModeFrame);
    }

    /**
     * Called by GamePanel when the hero escapes successfully.
     * After each hall, show the "completedN.png" for 3 seconds,
     * then proceed to the next hall or final screen.
     */
    public void onHeroEscaped() {
        // Save the current inventory before closing
        if (gamePanel != null && gamePanel.getHero() != null) {
            persistentInventory = gamePanel.getHero().getInventory();
        }

        // Close the old play mode frame
        if (playModeFrame != null) {
            playModeFrame.dispose();
        }

        // Increment the count of halls completed (1..4)
        gamesCompleted++;

        // Show the corresponding "completed" image for 3 seconds, then move on
        showCompletionScreen();
    }
    /**
     * Displays the correct "completedX.png" image in a window for 3 seconds,
     * then calls finishOrExit().
     */
    private void showCompletionScreen() {
        // If we've exceeded 4, just skip to final logic
        if (gamesCompleted < 1 || gamesCompleted > 4) {
            finishOrExit();
            return;
        }

        final JFrame completionFrame = new JFrame("Hall Completed");
        completionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        completionFrame.setExtendedState(JFrame.MAXIMIZED_BOTH); // Full screen
        completionFrame.setUndecorated(true); // No borders

        BufferedImage completionImage = loadImage(COMPLETED_IMAGES[gamesCompleted - 1]);
        ResponsiveImagePanel imagePanel = new ResponsiveImagePanel(
                completionImage,
                "Hall " + gamesCompleted + " Completed!"
        );
        completionFrame.add(imagePanel, BorderLayout.CENTER);
        completionFrame.setVisible(true);

        // Use a Swing Timer to close after 3 seconds
        javax.swing.Timer transitionTimer = new javax.swing.Timer(3000, e -> {
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
            // Get the current inventory before transitioning
            // Make it final
            final Inventory currentInventory = gamePanel != null && gamePanel.getHero() != null
                    ? new Inventory()
                    : null;

            // Copy the enchantments if we have a current inventory
            if (currentInventory != null) {
                currentInventory.setEnchantments(
                        new ArrayList<>(gamePanel.getHero().getInventory().getCollectedEnchantments())
                );
            }

            // Prepare the next hall with the correct min object count
            int nextMin = MIN_OBJECTS[gamesCompleted];
            String namehall;
            switch (gamesCompleted + 1) {
                case 1:  namehall = "Hall of Earth";  break;
                case 2:  namehall = "Hall of Air";    break;
                case 3:  namehall = "Hall of Water";  break;
                case 4:  namehall = "Fire Hall";   break;
                default: namehall = "Unknown Hall";
            }

            Hall nextHall = new Hall(
                    namehall,
                    hall.getRows(),
                    hall.getCols(),
                    nextMin
            );

            // Create new GameController
            GameController nextController = new GameController(nextHall);

            // Use the final currentInventory in lambda
            SwingUtilities.invokeLater(() -> {
                new BuildModeController(nextHall, nextController, currentInventory);
            });

        } else {
            showCongratulationsScreen();
        }
    }

    /**
     * Displays "congratulations.png" for 3 seconds, then exits.
     */
    private void showCongratulationsScreen() {
        final JFrame congratsFrame = new JFrame("Congratulations");
        congratsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        congratsFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        congratsFrame.setUndecorated(true);

        BufferedImage congratsImage = loadImage(AssetPaths.CONGRATULATIONS);
        ResponsiveImagePanel congratsPanel = new ResponsiveImagePanel(congratsImage, "Congratulations!");
        congratsFrame.add(congratsPanel, BorderLayout.CENTER);
        congratsFrame.setVisible(true);

        javax.swing.Timer timer = new javax.swing.Timer(3000, e -> {
            congratsFrame.dispose();
            // Return to main menu instead of exiting
            SwingUtilities.invokeLater(() -> {
                JFrame mainMenu = new RokueLikeMainMenu();
                mainMenu.setVisible(true);
            });
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Helper method for loading a BufferedImage from a resource path.
     */
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

    // ---------------------- Timer Pause/Resume ----------------------
    public void pauseGame() {
        isPaused = true;
        // Also pause the domain timer
        if (gameTimer != null) {
            gameTimer.pause();
        }
        if (pausePopup == null) {
            pausePopup = new PausePopUp(playModeFrame);
        }
        pausePopup.setVisible(true);
    }

    public void resumeGame() {
        isPaused = false;
        // Also resume the domain timer
        if (gameTimer != null) {
            gameTimer.resume();
        }
        if (pausePopup != null) {
            pausePopup.setVisible(false);
        }
    }

    public void saveGame() {
        System.out.println("Saving game...");
        Hero currentHero = gamePanel.getHero();

        // Log the current hero position before saving
        System.out.println("Current hero position before saving: x=" + currentHero.getX() +
                ", y=" + currentHero.getY());

        // Create new GameState with current game data
        this.gameState = new GameState(
                gamePanel.getGrid(),
                gamePanel.getPlacedObjects(),
                currentHero,  // Pass the current hero instance
                gamePanel.getMonsters(),
                timeRemaining,
                hall.getName(),
                gamePanel.getEnchantments(),
                currentHero.getInventory()
        );

        // Verify the position was stored correctly in gameState
        System.out.println("Position stored in gameState: x=" +
                gameState.getHeroPixelPosition().x + ", y=" +
                gameState.getHeroPixelPosition().y);

        // Save the gameState
        SaveLoadManager.saveGame(this.gameState, findNextSaveName());
    }

    /**
     * Find the next save name "saveX" where X = 1 + max existing integer among saves.
     */
    private String findNextSaveName() {
        List<String> saves = SaveLoadManager.listSaves(); // e.g. [save1, save2, custom]
        int maxNum = 0;
        for (String s : saves) {
            if (s.matches("save\\d+")) {
                int num = Integer.parseInt(s.substring(4));
                if (num > maxNum) {
                    maxNum = num;
                }
            }
        }
        return "save" + (maxNum + 1);
    }

    public void loadGame(GameState gameState) {
        System.out.println("Loading game...");
        System.out.println("Saved hero position: x=" + gameState.getHeroPixelPosition().x +
                ", y=" + gameState.getHeroPixelPosition().y);

        // Stop current domain timer if it exists
        if (gameTimer != null) {
            gameTimer.stop();
        }
        // Close existing window
        if (playModeFrame != null) {
            playModeFrame.dispose();
        }

        // Update the hall
        this.hall = new Hall(gameState.getHallName(), 13, 13, 6);

        // Reset hero instance first
        Hero.reset();

        // Get saved position
        Point savedPos = gameState.getHeroPixelPosition();
        System.out.println("Loading hero at position: " + savedPos.x + "," + savedPos.y);

        // Create hero with exact saved position
        Hero hero = Hero.getInstance(savedPos.x, savedPos.y, 64, 64);
        hero.setHealth(gameState.getHeroHealth());

        System.out.println("Hero position after creation: x=" + hero.getX() +
                ", y=" + hero.getY());

        // Create new playModeFrame
        playModeFrame = new JFrame("Rokue-Like - " + hall.getName());
        playModeFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        playModeFrame.setLocationRelativeTo(null);
        playModeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Create new game panel with all saved state
        gamePanel = new GamePanel(
                gameState.getGrid(),
                gameState.getPlacedObjects(),
                this,
                hero  // Pass the hero instance
        );

        // Verify position after GamePanel creation
        System.out.println("Hero position after GamePanel creation: " +
                hero.getX() + "," + hero.getY());

        // Restore enchantments
        List<Enchantment> loadedFloorEnchantments = gameState.getFloorEnchantments().stream()
                .map(GameState.EnchantmentState::toEnchantment)
                .collect(Collectors.toList());
        gamePanel.setEnchantments(loadedFloorEnchantments);

        // Restore inventory
        List<Enchantment> loadedInventoryEnchantments = gameState.getHeroEnchantments().stream()
                .map(GameState.EnchantmentState::toEnchantment)
                .collect(Collectors.toList());
        hero.getInventory().setEnchantments(loadedInventoryEnchantments);

        playModeFrame.add(gamePanel);

        // Set time remaining
        this.timeRemaining = gameState.getTimeRemaining();
        gamePanel.updateTime(timeRemaining);

        // Recreate monsters
        gamePanel.recreateMonsters(gameState.getMonsterStates());

        // Pause popup
        pausePopup = new PausePopUp(playModeFrame);

        playModeFrame.setVisible(true);

        System.out.println("Final hero position after load: x=" + hero.getX() +
                ", y=" + hero.getY());

        // Start domain timer again with loaded time
        gameTimer = new GameTimer(timeRemaining);
        gameTimer.start(
                () -> gamePanel.updateTime(gameTimer.getTimeRemaining()),
                () -> gamePanel.triggerGameOver()
        );

        // Set the current gameState
        this.gameState = gameState;
    }

    // ---------------------- Utility Methods -------------------------
    public void addTime(int seconds) {
        timeRemaining += seconds;         // Keep our local counter updated
        if (gameTimer != null) {
            gameTimer.addTime(seconds);   // Also add to domain timer
        }
        if (gamePanel != null) {
            gamePanel.updateTime(timeRemaining);
        }
    }

    public Hall getHall() {
        return hall;
    }

    public int getStartingTime() {
        return startingTime;
    }

    public int getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(int time) {
        this.timeRemaining = time;
        if (gamePanel != null) {
            gamePanel.updateTime(time);
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Close the current play mode window (if any), stop the Domain.GameTimer.
     */
    public void closeGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (playModeFrame != null) {
            playModeFrame.dispose();
        }
    }

    public void returnToMainMenu() {
        closeGame();
        SwingUtilities.invokeLater(() -> {
            JFrame mainMenu = new RokueLikeMainMenu();
            mainMenu.setVisible(true);
        });
    }

    public static void resetProgress() {
        gamesCompleted = 0;
        persistentInventory = null;  // Add this line
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
            this.setBackground(Color.BLACK); // Optional
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (image != null) {
                int panelWidth = getWidth();
                int panelHeight = getHeight();

                double imageAspect = (double) image.getWidth() / image.getHeight();
                double panelAspect = (double) panelWidth / panelHeight;

                int drawWidth, drawHeight;
                if (panelAspect > imageAspect) {
                    drawHeight = panelHeight;
                    drawWidth = (int) (drawHeight * imageAspect);
                } else {
                    drawWidth = panelWidth;
                    drawHeight = (int) (drawWidth / imageAspect);
                }

                // Center
                int x = (panelWidth - drawWidth) / 2;
                int y = (panelHeight - drawHeight) / 2;
                g.drawImage(image, x, y, drawWidth, drawHeight, this);
            } else {
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
