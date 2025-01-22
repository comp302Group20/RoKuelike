package Utils;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;

public class GameFonts {
    public static Font pixelFont;

    static {
        try {
            InputStream is = GameFonts.class.getResourceAsStream("/resources/Fonts/pixelFont.ttf");
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(pixelFont);
            pixelFont = pixelFont.deriveFont(24f);

        } catch (Exception e) {
            System.out.println("Error loading font: " + e.getMessage());
            pixelFont = new Font("Arial", Font.BOLD, 24);
        }
    }
}