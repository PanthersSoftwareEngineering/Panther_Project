package view;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.net.URL;


// Here we will save and pre-upload all pictures and gifs so the system will be faster
public final class GameAssets {

	public static final Image GAME_ICON = loadImage("assets/Icon/bomb2.png");

	private static Image loadImage(String path) {
	    try {
	        // Try loading as Resource first
	        java.net.URL imgURL = GameAssets.class.getResource(path);
	        if (imgURL != null) {
	            return new ImageIcon(imgURL).getImage();
	        }

	        // If not found as resource, try loading directly from filesystem
	        // We remove the leading slash if it exists for filesystem check
	        String fsPath = path.startsWith("/") ? path.substring(1) : path;
	        java.io.File file = new java.io.File(fsPath);
	        
	        if (file.exists()) {
	            return new ImageIcon(file.getAbsolutePath()).getImage();
	        } else {
	            System.err.println("[DEBUG] File NOT FOUND in Resource or Filesystem: " + fsPath);
	            return null;
	        }
	    } catch (Exception e) {
	        System.err.println("[DEBUG] Error loading image: " + e.getMessage());
	        return null;
	    }
	}
	
    // ---------- Paths ----------
    public static final String MAIN_BG_PATH =
            "assets/MainMenuPics/MainMenu.png";
    
    public static final String HISTORY_BG_PATH =
            "assets/HistoryPics/HistoryPic.png";

    public static final String PLAY_BG_PATH =
            "assets/PlayerViewPics/NewPlayBack.png";

    public static final String END_BG_PATH =
            "assets/EndGamePics/EndBackground.png";

    public static final String MATCH_BG_PATH =
            "assets/MatchViewPics/MatchBackground.png";

    public static final String[] WIN_GIF_PATHS = {
            "assets/EndGamePics/win1.gif",
            "assets/EndGamePics/win2.gif",
            "assets/EndGamePics/win3.gif",
            "assets/EndGamePics/win4.gif"
    };

    public static final String[] LOSE_GIF_PATHS = {
            "assets/EndGamePics/lose1.gif",
            "assets/EndGamePics/lose2.gif",
            "assets/EndGamePics/lose3.gif",
            "assets/EndGamePics/lose4.gif"
    };

    // public arrays with the actual pre-loaded GIF icons
    public static final ImageIcon[] WIN_GIFS;
    public static final ImageIcon[] LOSE_GIFS;

    // size you want on the end screen (change if needed)
    private static final int END_GIF_SIZE = 220;

    // static block runs ONCE when GameAssets is first loaded
    static {
        WIN_GIFS  = loadGifArray(WIN_GIF_PATHS, END_GIF_SIZE);
        LOSE_GIFS = loadGifArray(LOSE_GIF_PATHS, END_GIF_SIZE);
    }


    private static ImageIcon[] loadGifArray(String[] paths, int size) {
        ImageIcon[] icons = new ImageIcon[paths.length];

        for (int i = 0; i < paths.length; i++) {
            String path = paths[i];
            try {
                ImageIcon icon = null;

                // 1) classpath
                URL cp = GameAssets.class.getClassLoader().getResource(path);
                if (cp != null) {
                    System.out.println("[ASSETS] GIF from CLASSPATH: " + path);
                    icon = new ImageIcon(cp);
                } else {
                    // 2) filesystem
                    File file = new File(path);
                    System.out.println("[ASSETS] GIF trying FS: " + file.getAbsolutePath());
                    if (file.exists()) {
                        System.out.println("[ASSETS] GIF from FILESYSTEM: " + file.getAbsolutePath());
                        icon = new ImageIcon(file.getAbsolutePath());
                    } else {
                        System.err.println("[ASSETS] GIF NOT FOUND: " + path);
                    }
                }

                if (icon != null) {
                    Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_DEFAULT);
                    icons[i] = new ImageIcon(img);
                } else {
                    icons[i] = null;
                }

            } catch (Exception e) {
                System.err.println("[ASSETS] Error loading GIF: " + path);
                e.printStackTrace();
                icons[i] = null;
            }
        }
        return icons;
    }


    // ---------- Cached images (loaded once) ----------
    public static final Image MAIN_BACKGROUND =
            loadImageOnce(MAIN_BG_PATH);
    
    public static final Image HISTORY_BACKGROUND =
            loadImageOnce(HISTORY_BG_PATH);

    public static final Image GAME_BACKGROUND =
            loadImageOnce(PLAY_BG_PATH);

    public static final Image END_BACKGROUND =
            loadImageOnce(END_BG_PATH);


    public static final Image MATCH_BACKGROUND= loadImageOnce(MATCH_BG_PATH);


    private GameAssets() { }  // no instances

    /**
     * Tries to load an image once.
     * 1) From CLASSPATH (for JAR)
     * 2) From filesystem (for IntelliJ/IDE)
     * If both fail, logs a warning and returns null.
     */
    private static Image loadImageOnce(String path) {
        File file = null;
        try {
            // 1) classpath
            URL cp = GameAssets.class.getClassLoader().getResource(path);
            if (cp != null) {
                System.out.println("[ASSETS] Loaded from CLASSPATH: " + path);
                return new ImageIcon(cp).getImage();
            }

            // 2) filesystem
            file = new File(path);
            System.out.println("[ASSETS] Trying FS: " + file.getAbsolutePath());
            if (file.exists()) {
                System.out.println("[ASSETS] Loaded from FILESYSTEM: " +
                        file.getAbsolutePath());
                return new ImageIcon(file.getAbsolutePath()).getImage();
            }

            // 3) fail – non-fatal
            System.err.println("=== ASSET LOAD WARNING (non-fatal) ===");
            System.err.println("Requested : " + path);
            if (file != null) {
                System.err.println("FS path   : " + file.getAbsolutePath() +
                        " (exists=" + file.exists() + ")");
            }
            System.err.println("Work dir  : " + System.getProperty("user.dir"));
            System.err.println("Returning null for this asset.");
            System.err.println("======================================");
        } catch (Exception ex) {
            System.err.println("=== ASSET LOAD ERROR (non-fatal) ===");
            System.err.println("Requested : " + path);
            ex.printStackTrace();
            System.err.println("Returning null for this asset.");
            System.err.println("=================================");
        }
        return null;
    }


}
