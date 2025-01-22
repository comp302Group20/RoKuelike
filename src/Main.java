import UI.RokueLikeMainMenu;
import Utils.SoundPlayer;

/**
 * Entry point for the entire application.
 */
public class Main {
    public static void main(String[] args) {
        // Simply launch the main menu
        new RokueLikeMainMenu().setVisible(true);

        // Play the theme sound in a loop
        SoundPlayer.playSoundLoop("/resources/sounds/theme.wav");
    }
}