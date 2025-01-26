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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GameController {

    private Hall hall;
    private GamePanel gamePanel;
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

    private static int gamesCompleted = 0;

    private int currentHallNumber;

    public GameState gameState;

    private static final String[] COMPLETED_IMAGES = {
            AssetPaths.COMPLETED1,
            AssetPaths.COMPLETED2,
            AssetPaths.COMPLETED3,
            AssetPaths.COMPLETED4
    };

    private static final int[] MIN_OBJECTS = {6, 9, 13, 17};

    /**
     * Constructs a GameController for the specified hall, initializing fields and inventory tracking.
     * @param hall the Hall object that this controller manages
     */
    public GameController(Hall hall) {
        this.hall = hall;
        this.currentHallNumber = gamesCompleted + 1;
        this.isPaused = false;
        this.timeRemaining = 0;
        this.gameState = new GameState();

        if (persistentInventory == null) {
            persistentInventory = new Inventory();
        }
    }

    /**
     * Retrieves the current domain-level GameTimer used by this controller.
     * @return the GameTimer instance
     */
    public GameTimer getGameTimer() {
        return this.gameTimer;
    }

    /**
     * Called upon finishing build mode to transition into play mode, given the constructed map and objects.
     * @param grid the 2D CellType array representing the board layout
     * @param placedObjects the 2D array of placed objects
     * @param previousInventory the Inventory carried over from a previous hall, if any
     */
    public void onBuildModeFinished(BuildModePanel.CellType[][] grid,
                                    BuildModePanel.PlacedObject[][] placedObjects,
                                    Inventory previousInventory) {
        int placedObjectCount = 0;
        for (int r = 0; r < placedObjects.length; r++) {
            for (int c = 0; c < placedObjects[0].length; c++) {
                if (placedObjects[r][c] != null) {
                    placedObjectCount++;
                }
            }
        }
        timeRemaining = placedObjectCount * TIME_PER_OBJECT;

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
            return;
        }

        startingTime = timeRemaining;
        startPlayMode(grid, placedObjects, startingTime, previousInventory);
    }

    /**
     * Generates a descriptive name for the hall based on its number (1-4).
     * @param hallNumber the numeric index of the hall
     * @return a descriptive String for the hall
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
     * Calculates a starting time by multiplying total objects with a base time per object.
     * @param totalObjects the total number of objects placed in the hall
     * @return the initial time in seconds for the hero to complete the hall
     */
    private int calculateStartingTime(int totalObjects) {
        int baseTimePerObject = 5;
        return totalObjects * baseTimePerObject;
    }

    /**
     * Sets up and launches the play mode, creating a new GamePanel and attaching a GameTimer to it.
     * @param grid the 2D CellType array for the grid
     * @param placedObjects the 2D array of placed objects
     * @param startingTime the initial time in seconds
     * @param previousInventory the Inventory carried over, if any
     */
    private void startPlayMode(BuildModePanel.CellType[][] grid,
                               BuildModePanel.PlacedObject[][] placedObjects,
                               int startingTime,
                               Inventory previousInventory) {
        closeGame();

        playModeFrame = new JFrame("Play Mode - " + getHallDescriptor(currentHallNumber));
        playModeFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        playModeFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        playModeFrame.setLocationRelativeTo(null);

        Hero.reset();
        Hero newHero = Hero.getInstance(0, 0, 64, 64);

        if (gamesCompleted == 0) {
            newHero.getInventory().clearEnchantments();
        } else if (previousInventory != null) {
            newHero.getInventory().setEnchantments(
                    new ArrayList<>(previousInventory.getCollectedEnchantments())
            );
        }

        gamePanel = new GamePanel(grid, placedObjects, this);

        gameTimer = new GameTimer(startingTime);
        gameTimer.start(
                () -> {
                    this.timeRemaining = gameTimer.getTimeRemaining();
                    gamePanel.updateTime(gameTimer.getTimeRemaining());
                },
                () -> gamePanel.triggerGameOver()
        );

        playModeFrame.add(gamePanel, BorderLayout.CENTER);
        playModeFrame.setVisible(true);

        pausePopup = new PausePopUp(playModeFrame);
    }

    /**
     * Invoked by the GamePanel when the hero successfully escapes the current hall.
     * Saves inventory, closes the play mode, increments the completed hall count, and proceeds accordingly.
     */
    public void onHeroEscaped() {
        if (gamePanel != null && gamePanel.getHero() != null) {
            persistentInventory = gamePanel.getHero().getInventory();
        }
        if (playModeFrame != null) {
            playModeFrame.dispose();
        }
        gamesCompleted++;
        showCompletionScreen();
    }

    /**
     * Displays a short "completed hall" screen specific to the number of halls finished, then proceeds further.
     */
    private void showCompletionScreen() {
        if (gamesCompleted < 1 || gamesCompleted > 4) {
            finishOrExit();
            return;
        }

        final JFrame completionFrame = new JFrame("Hall Completed");
        completionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        completionFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        completionFrame.setUndecorated(true);

        BufferedImage completionImage = loadImage(COMPLETED_IMAGES[gamesCompleted - 1]);
        ResponsiveImagePanel imagePanel = new ResponsiveImagePanel(
                completionImage,
                "Hall " + gamesCompleted + " Completed!"
        );
        completionFrame.add(imagePanel, BorderLayout.CENTER);
        completionFrame.setVisible(true);

        javax.swing.Timer transitionTimer = new javax.swing.Timer(3000, e -> {
            completionFrame.dispose();
            finishOrExit();
        });
        transitionTimer.setRepeats(false);
        transitionTimer.start();
    }

    /**
     * Decides whether to create a new hall (if fewer than 4 completed) or show the "congratulations" screen.
     */
    private void finishOrExit() {
        if (gamesCompleted < 4) {
            final Inventory currentInventory = (gamePanel != null && gamePanel.getHero() != null)
                    ? new Inventory()
                    : null;

            if (currentInventory != null) {
                currentInventory.setEnchantments(
                        new ArrayList<>(gamePanel.getHero().getInventory().getCollectedEnchantments())
                );
            }

            int nextMin = MIN_OBJECTS[gamesCompleted];
            String namehall;
            switch (gamesCompleted + 1) {
                case 1:  namehall = "Hall of Earth";  break;
                case 2:  namehall = "Hall of Air";    break;
                case 3:  namehall = "Hall of Water";  break;
                case 4:  namehall = "Fire Hall";      break;
                default: namehall = "Unknown Hall";
            }

            Hall nextHall = new Hall(
                    namehall,
                    hall.getRows(),
                    hall.getCols(),
                    nextMin
            );

            GameController nextController = new GameController(nextHall);

            SwingUtilities.invokeLater(() -> {
                new BuildModeController(nextHall, nextController, currentInventory);
            });

        } else {
            showCongratulationsScreen();
        }
    }

    /**
     * Displays the final "congratulations" screen after all four halls are completed, then returns to main menu.
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
            SwingUtilities.invokeLater(() -> {
                JFrame mainMenu = new RokueLikeMainMenu();
                mainMenu.setVisible(true);
            });
        });
        timer.setRepeats(false);
        timer.start();
    }

    /**
     * Loads an image from the specified resource path.
     * @param resourcePath the path to the image resource
     * @return a BufferedImage if loading succeeded, otherwise null
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

    /**
     * Pauses the game by halting the GameTimer and showing the pause pop-up window.
     */
    public void pauseGame() {
        isPaused = true;
        if (gameTimer != null) {
            gameTimer.pause();
        }
        if (pausePopup == null) {
            pausePopup = new PausePopUp(playModeFrame);
        }
        pausePopup.setVisible(true);
    }

    /**
     * Resumes the game by restarting the GameTimer and hiding the pause pop-up window.
     */
    public void resumeGame() {
        isPaused = false;
        if (gameTimer != null) {
            gameTimer.resume();
        }
        if (pausePopup != null) {
            pausePopup.setVisible(false);
        }
    }

    /**
     * Saves the current state of the game (hero, grid, monsters, etc.) using the SaveLoadManager.
     */
    public void saveGame() {
        System.out.println("Saving game...");
        Hero currentHero = gamePanel.getHero();
        System.out.println("Current hero position before saving: x=" + currentHero.getX() +
                ", y=" + currentHero.getY());

        this.gameState = new GameState(
                gamePanel.getGrid(),
                gamePanel.getPlacedObjects(),
                currentHero,
                gamePanel.getMonsters(),
                timeRemaining,
                hall.getName(),
                gamePanel.getEnchantments(),
                currentHero.getInventory()
        );

        System.out.println("Position stored in gameState: x=" +
                gameState.getHeroPixelPosition().x + ", y=" +
                gameState.getHeroPixelPosition().y);

        SaveLoadManager.saveGame(this.gameState, findNextSaveName());
    }

    /**
     * Finds the next available save filename in the format "saveX", where X is an integer that increments from existing saves.
     * @return the generated save filename without extension
     */
    private String findNextSaveName() {
        List<String> saves = SaveLoadManager.listSaves();
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

    /**
     * Loads a GameState from the given object and reconstructs the game environment accordingly.
     * @param gameState the GameState to load
     */
    public void loadGame(GameState gameState) {
        System.out.println("Loading game...");
        System.out.println("Saved hero position: x=" + gameState.getHeroPixelPosition().x +
                ", y=" + gameState.getHeroPixelPosition().y);

        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (playModeFrame != null) {
            playModeFrame.dispose();
        }

        this.hall = new Hall(gameState.getHallName(), 13, 13, 6);

        Hero.reset();

        Point savedPos = gameState.getHeroPixelPosition();
        System.out.println("Loading hero at position: " + savedPos.x + "," + savedPos.y);

        Hero hero = Hero.getInstance(savedPos.x, savedPos.y, 64, 64);
        hero.setHealth(gameState.getHeroHealth());
        System.out.println("Hero position after creation: x=" + hero.getX() +
                ", y=" + hero.getY());

        playModeFrame = new JFrame("Rokue-Like - " + hall.getName());
        playModeFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        playModeFrame.setLocationRelativeTo(null);
        playModeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        gamePanel = new GamePanel(
                gameState.getGrid(),
                gameState.getPlacedObjects(),
                this,
                hero
        );

        System.out.println("Hero position after GamePanel creation: " +
                hero.getX() + "," + hero.getY());

        List<Enchantment> loadedFloorEnchantments = gameState.getFloorEnchantments().stream()
                .map(GameState.EnchantmentState::toEnchantment)
                .collect(Collectors.toList());
        gamePanel.setEnchantments(loadedFloorEnchantments);

        List<Enchantment> loadedInventoryEnchantments = gameState.getHeroEnchantments().stream()
                .map(GameState.EnchantmentState::toEnchantment)
                .collect(Collectors.toList());
        hero.getInventory().setEnchantments(loadedInventoryEnchantments);

        playModeFrame.add(gamePanel);

        this.timeRemaining = gameState.getTimeRemaining();
        gamePanel.updateTime(timeRemaining);

        gamePanel.recreateMonsters(gameState.getMonsterStates());

        pausePopup = new PausePopUp(playModeFrame);
        playModeFrame.setVisible(true);

        System.out.println("Final hero position after load: x=" + hero.getX() +
                ", y=" + hero.getY());

        gameTimer = new GameTimer(timeRemaining);
        gameTimer.start(
                () -> gamePanel.updateTime(gameTimer.getTimeRemaining()),
                () -> gamePanel.triggerGameOver()
        );

        this.gameState = gameState;
    }

    /**
     * Adds additional time to the hero's remaining countdown, updating both local and domain timer values.
     * @param seconds the number of seconds to add
     */
    public void addTime(int seconds) {
        timeRemaining += seconds;
        if (gameTimer != null) {
            gameTimer.addTime(seconds);
        }
        if (gamePanel != null) {
            gamePanel.updateTime(timeRemaining);
        }
    }

    /**
     * Retrieves the current Hall instance.
     * @return the Hall being managed by this controller
     */
    public Hall getHall() {
        return hall;
    }

    /**
     * Returns the starting time that was set when play mode began.
     * @return the initial time in seconds
     */
    public int getStartingTime() {
        return startingTime;
    }

    /**
     * Returns the amount of time still remaining for the hero.
     * @return the current time left in seconds
     */
    public int getTimeRemaining() {
        return timeRemaining;
    }

    /**
     * Updates the remaining time the hero has, reflecting the change in the UI if needed.
     * @param time the new time remaining in seconds
     */
    public void setTimeRemaining(int time) {
        this.timeRemaining = time;
        if (gamePanel != null) {
            gamePanel.updateTime(time);
        }
    }

    /**
     * Checks if the game is currently paused.
     * @return true if paused, false otherwise
     */
    public boolean isPaused() {
        return isPaused;
    }

    /**
     * Stops the current Domain.GameTimer and closes the play mode window, if open.
     */
    public void closeGame() {
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (playModeFrame != null) {
            playModeFrame.dispose();
        }
    }

    /**
     * Closes the current game window and returns to the main menu.
     */
    public void returnToMainMenu() {
        closeGame();
        SwingUtilities.invokeLater(() -> {
            JFrame mainMenu = new RokueLikeMainMenu();
            mainMenu.setVisible(true);
        });
    }

    /**
     * Resets the overall progress (hall completions) and clears any persistent inventory.
     */
    public static void resetProgress() {
        gamesCompleted = 0;
        persistentInventory = null;
    }

    /**
     * A custom JPanel that scales a background image to fit while maintaining its aspect ratio,
     * or displays fallback text if the image is missing.
     */
    private static class ResponsiveImagePanel extends JPanel {
        private BufferedImage image;
        private String fallbackText;

        /**
         * Constructs a ResponsiveImagePanel using the specified image and fallback text.
         * @param image the BufferedImage to display
         * @param fallbackText the text to display if the image is null
         */
        public ResponsiveImagePanel(BufferedImage image, String fallbackText) {
            this.image = image;
            this.fallbackText = fallbackText;
            this.setBackground(Color.BLACK);
        }

        /**
         * Renders the image (if available) centered and scaled within the panel; otherwise, shows fallback text.
         * @param g the Graphics context for drawing
         */
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
