package util;

import javax.sound.sampled.*;
import java.io.InputStream;

public class BackgroundMusic {

    private static Clip clip;

    public static void start() {
        System.out.println("🎵 BackgroundMusic.start() called");

        try {
        	InputStream is = BackgroundMusic.class
        	        .getClassLoader()
        	        .getResourceAsStream(
        	        		"resources/audio/background song.wav"
        	        );

            System.out.println("InputStream is null? " + (is == null));

            if (is == null) {
                System.err.println("❌ WAV FILE NOT FOUND");
                return;
            }

            AudioInputStream audio =
                    AudioSystem.getAudioInputStream(is);

            clip = AudioSystem.getClip();
            clip.open(audio);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

            System.out.println("✅ Music started");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
