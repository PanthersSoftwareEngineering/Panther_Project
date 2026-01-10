package view;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.util.Properties;

/**
 * GameAssets
 * - Loads assets from classpath (JAR) or filesystem (IDE).
 * - Loads animated GIFs as ImageIcon ONLY (no scaling) so they stay animated.
 */
public final class GameAssets {

    // =====================================================================
    // ICON
    // =====================================================================
    public static final Image GAME_ICON = loadImageAny("assets/Icon/bomb2.png");

    // =====================================================================
    // THEME
    // =====================================================================
    private static final String THEME_FILE = "theme.properties";
    private static final String KEY_BG = "backgroundKey"; // BG1/BG2/BG3
    private static final String BASIC_FILE = "BasicBack.png";
    private static final String MATCH_FILE = "MatchBack.png";
    private static final String RECT_FILE  = "RectangleBack.png";

    // =====================================================================
    // GIFS 
    // =====================================================================
    public static final String[] WIN_GIF_PATHS = {
            "assets/EndGameGifs/win1.gif",
            "assets/EndGameGifs/win2.gif",
            "assets/EndGameGifs/win3.gif",
            "assets/EndGameGifs/win4.gif"
    };

    public static final String[] LOSE_GIF_PATHS = {
            "assets/EndGameGifs/lose1.gif",
            "assets/EndGameGifs/lose2.gif",
            "assets/EndGameGifs/lose3.gif",
            "assets/EndGameGifs/lose4.gif"
    };

    public static final ImageIcon[] WIN_GIFS;
    public static final ImageIcon[] LOSE_GIFS;

    // =====================================================================
    // BACKGROUNDS 
    // =====================================================================
    public static Image MAIN_BACKGROUND;
    public static Image HISTORY_BACKGROUND;
    public static Image GAME_BACKGROUND;
    public static Image MATCH_BACKGROUND;
    public static Image END_BACKGROUND;

    public static Image PERSON_BACKGROUND;

    // =====================================================================
    // STATIC INIT
    // =====================================================================
    static {
        // Load GIFs 
        WIN_GIFS  = loadGifArray(WIN_GIF_PATHS);
        LOSE_GIFS = loadGifArray(LOSE_GIF_PATHS);

        // Load theme backgrounds 
        reloadThemeBackgrounds();
    }

    private GameAssets() { }

    // =====================================================================
    // PUBLIC THEME API
    // =====================================================================

    /** Read backgroundKey from theme.properties (BG1/BG2/BG3). */
    public static String loadBackgroundKey() {
        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(THEME_FILE)) {
            p.load(fis);
            String v = p.getProperty(KEY_BG, "BG1");
            return (v == null || v.trim().isEmpty()) ? "BG1" : v.trim();
        } catch (Exception ignore) {
            return "BG1";
        }
    }

    /** Save backgroundKey to theme.properties. */
    public static void saveBackgroundKey(String key) {
        if (key == null || key.trim().isEmpty()) return;

        Properties p = new Properties();
        try (FileInputStream fis = new FileInputStream(THEME_FILE)) {
            p.load(fis);
        } catch (Exception ignore) { }

        p.setProperty(KEY_BG, key.trim());

        try (FileOutputStream fos = new FileOutputStream(THEME_FILE)) {
            p.store(fos, "Theme settings");
        } catch (Exception ex) {
            System.err.println("[ASSETS] Failed saving theme: " + ex.getMessage());
        }
    }

    /**
     * Reload themed backgrounds according to theme.properties.
     * Call this after saveBackgroundKey(...) to apply immediately.
     */
    public static synchronized void reloadThemeBackgrounds() {
        String key = loadBackgroundKey();       // BG1/BG2/BG3
        String n = parseThemeNumber(key);       // "1"/"2"/"3"
        String baseFolder = "assets/Background" + n + "/";

        System.out.println("[ASSETS] Background theme: " + key + " -> " + baseFolder);

        Image basic = loadImageOnce(baseFolder + BASIC_FILE);
        Image match = loadImageOnce(baseFolder + MATCH_FILE);
        Image rect  = loadImageOnce(baseFolder + RECT_FILE);

        // Mapping you requested:
        MAIN_BACKGROUND    = basic;
        HISTORY_BACKGROUND = basic;
        GAME_BACKGROUND    = rect;  
        PERSON_BACKGROUND  = basic;
        MATCH_BACKGROUND   = match;
        END_BACKGROUND     = basic;

        // Fallbacks if something missing
        if (MAIN_BACKGROUND == null) MAIN_BACKGROUND = rect;
        if (HISTORY_BACKGROUND == null) HISTORY_BACKGROUND = MAIN_BACKGROUND;
        if (GAME_BACKGROUND == null) GAME_BACKGROUND = MAIN_BACKGROUND;
        if (PERSON_BACKGROUND == null) PERSON_BACKGROUND = MAIN_BACKGROUND;
        if (MATCH_BACKGROUND == null) MATCH_BACKGROUND = MAIN_BACKGROUND;
        if (END_BACKGROUND == null) END_BACKGROUND = MAIN_BACKGROUND;
    }

    private static String parseThemeNumber(String key) {
        if (key == null) return "1";
        key = key.trim().toUpperCase();
        if (key.startsWith("BG") && key.length() >= 3) {
            String n = key.substring(2);
            if (n.equals("1") || n.equals("2") || n.equals("3")) return n;
        }
        return "1";
    }

    // =====================================================================
    // IMAGE LOADING
    // =====================================================================

    /** Quick icon-like loader (classpath first then filesystem). */
    private static Image loadImageAny(String path) {
        try {
            URL cp = GameAssets.class.getClassLoader().getResource(path);
            if (cp == null) cp = GameAssets.class.getResource("/" + path);
            if (cp != null) {
                System.out.println("[ASSETS] Loaded IMG from CLASSPATH: " + path);
                return new ImageIcon(cp).getImage();
            }

            File f = new File(path.startsWith("/") ? path.substring(1) : path);
            System.out.println("[ASSETS] Trying IMG FS: " + f.getAbsolutePath());
            if (f.exists()) {
                System.out.println("[ASSETS] Loaded IMG from FILESYSTEM: " + f.getAbsolutePath());
                return new ImageIcon(f.getAbsolutePath()).getImage();
            }
        } catch (Exception e) {
            System.err.println("[ASSETS] IMG load error: " + path + " -> " + e.getMessage());
        }
        System.err.println("[ASSETS] IMG NOT FOUND: " + path);
        return null;
    }

    /** Background loader (classpath first then filesystem). */
    private static Image loadImageOnce(String path) {
        try {
            URL cp = GameAssets.class.getClassLoader().getResource(path);
            if (cp == null) cp = GameAssets.class.getResource("/" + path);

            if (cp != null) {
                System.out.println("[ASSETS] Loaded IMG from CLASSPATH: " + path);
                return new ImageIcon(cp).getImage();
            }

            File file = new File(path.startsWith("/") ? path.substring(1) : path);
            System.out.println("[ASSETS] Trying IMG FS: " + file.getAbsolutePath());
            if (file.exists()) {
                System.out.println("[ASSETS] Loaded IMG from FILESYSTEM: " + file.getAbsolutePath());
                return new ImageIcon(file.getAbsolutePath()).getImage();
            }

            System.err.println("[ASSETS] IMG NOT FOUND: " + path);
        } catch (Exception e) {
            System.err.println("[ASSETS] IMG load error: " + path);
            e.printStackTrace();
        }
        return null;
    }

    // =====================================================================
    // GIF LOADING 
    // =====================================================================
    private static ImageIcon[] loadGifArray(String[] paths) {
        ImageIcon[] icons = new ImageIcon[paths.length];

        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            try {
                ImageIcon icon = null;

                URL cp = GameAssets.class.getClassLoader().getResource(path);
                if (cp == null) cp = GameAssets.class.getResource("/" + path);

                if (cp != null) {
                    System.out.println("[ASSETS] GIF from CLASSPATH: " + path);
                    icon = new ImageIcon(cp);
                } else {
                    File file = new File(path.startsWith("/") ? path.substring(1) : path);
                    System.out.println("[ASSETS] GIF trying FS: " + file.getAbsolutePath());
                    if (file.exists()) {
                        System.out.println("[ASSETS] GIF from FILESYSTEM: " + file.getAbsolutePath());
                        icon = new ImageIcon(file.getAbsolutePath());
                    } else {
                        System.err.println("[ASSETS] GIF NOT FOUND: " + path);
                    }
                }

                icons[i] = icon;

            } catch (Exception e) {
                System.err.println("[ASSETS] Error loading GIF: " + path);
                e.printStackTrace();
                icons[i] = null;
            }
        }
        return icons;
    }
}
