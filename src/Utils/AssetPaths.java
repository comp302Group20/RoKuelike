package Utils;

public class AssetPaths {
    private static final String RESOURCE_PREFIX = "/resources";

    private static String getPath(String path) {
        return RESOURCE_PREFIX + path;
    }

    public static final String TITLE_IMAGE = getPath("/Assets/title2.png");
    public static final String SPRITESHEET = getPath("/Assets/spritesheet.png");
    public static final String BUILD_MODE_BACKGROUND = getPath("/Assets/build-mode.png");
    public static final String MAIN_MENU_BACKGROUND = getPath("/Assets/mainmenu.png");
    public static final String NEWGAME_BUTTON = getPath("/Assets/newgamebutton.png");
    public static final String HELP_BUTTON = getPath("/Assets/helpbutton.png");
    public static final String EXIT_BUTTON = getPath("/Assets/exitbutton.png");
}