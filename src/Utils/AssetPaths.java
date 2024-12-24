package Utils;

public class AssetPaths {
    private static final String RESOURCE_PREFIX = "/resources";
    private static final String ASSETS_PREFIX = RESOURCE_PREFIX + "/Assets";

    private static String getPath(String path) {
        return ASSETS_PREFIX + path;
    }

    // UI elements
    public static final String SPRITESHEET = getPath("/spritesheet.png");
    public static final String SPRITESHEET_2 = getPath("/spritesheet_2.png");
    public static final String GAME_TITLE = getPath("/title2.png");
    public static final String MAIN_MENU_BACKGROUND = getPath("/mainmenu.png");
    public static final String NEWGAME_BUTTON = getPath("/newgamebutton.png");
    public static final String HELP_BUTTON = getPath("/helpbutton.png");
    public static final String EXIT_BUTTON = getPath("/exitbutton.png");
   

    // Sprite coordinates for different objects (x, y, width, height)
    public static final int[] FLOOR_TILE = {32, 48, 16, 16};
    public static final int[] WALL = {48, 48, 16, 16};
    public static final int[] DOOR = {64, 48, 16, 16};
    public static final int[] CHEST = {80, 48, 16, 16};
    public static final int[] TORCH = {96, 48, 16, 16};
    public static final int[] PILLAR = {112, 48, 16, 32}; // Double height
    public static final int[] LADDER = {128, 48, 16, 16};
    public static final int[] BOX = {80, 112, 16, 16};
    public static final int[] DOUBLE_BOX = {96, 96, 16, 32}; // Double height
    public static final int[] SKULL = {176, 48, 16, 16};
    public static final int[] POTION = {192, 48, 16, 16};

    public static final String BUILD_MODE_BACKGROUND = getPath("/Assets/build-mode.png");
    public static final String HERO = getPath("/player.png");
    public static final String ARCHER = getPath("/archer.png");
    public static final String WIZARD = getPath("/wizard.png");
    public static final String FIGHTER = getPath("/fighter.png");
    public static final String PAUSE_BUTTON = getPath("/pausegame.png");
    public static final String EXIT_GAME = getPath("/exitgame.png");
    public static final String RESUME_BUTTON = getPath("/resumegame.png");
    public static final String PAUSED_INDICATOR = getPath("/paused2.png");
}