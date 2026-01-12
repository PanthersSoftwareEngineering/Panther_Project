package util;

import javax.sound.sampled.*;
import java.io.InputStream;

public class BackgroundMusic {

    private static Clip clip;
    private static FloatControl gainControl;

    // =========================================================
    // START
    // =========================================================
    public static void start() {
        if (clip != null && clip.isRunning())
            return;

        try {
            InputStream is = BackgroundMusic.class
                    .getClassLoader()
                    .getResourceAsStream(
                    		"resources/audio/background song.wav"
                    );

            if (is == null) {
                System.err.println("❌ WAV FILE NOT FOUND");
                return;
            }

            AudioInputStream audio =
                    AudioSystem.getAudioInputStream(is);

            clip = AudioSystem.getClip();
            clip.open(audio);

            // ✅ initialize volume control AFTER open
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                gainControl =
                        (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                setVolume(0.6f); // default 60%
            }

            clip.loop(Clip.LOOP_CONTINUOUSLY);
            clip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================================================
    // STOP
    // =========================================================
    public static void stop() {
        if (clip != null) {
            clip.stop();
            clip.close();
            clip = null;
            gainControl = null;
        }
    }

    // =========================================================
    // VOLUME (0.0 – 1.0)
    // =========================================================
    public static void setVolume(float volume) {
        if (gainControl == null)
            return;

        volume = Math.max(0.0001f, Math.min(1f, volume));

        // 🔊 logarithmic scaling (IMPORTANT)
        float dB = (float) (20.0 * Math.log10(volume));
        gainControl.setValue(dB);
    }

    // =========================================================
    // HELPERS
    // =========================================================
    public static boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    public static int getVolumePercent() {
        if (gainControl == null)
            return 60;

        float min = gainControl.getMinimum();
        float max = gainControl.getMaximum();
        float val = gainControl.getValue();

        return (int) (((val - min) / (max - min)) * 100);
    }
}
