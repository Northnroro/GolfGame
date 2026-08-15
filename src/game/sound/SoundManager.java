package game.sound;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;

public final class SoundManager {
    private static final String[] CLUB_HITS = variants("club_hit", 3);
    private static final String[] GRASS_HITS = variants("grass", 3);
    private static final String[] ICE_HITS = variants("ice", 3);
    private static final String[] SAND_HITS = variants("sand", 3);
    private static final String[] WATER_HITS = variants("water", 3);
    private static final String[] HOLE_HITS = variants("hole", 3);

    private SoundManager() {
    }

    public static void playClick() {
        play("assets/menu_click.wav", 0.55);
    }

    public static void playShot(double power) {
        double p = clamp(power, 0.0, 1.0);
        playRandom(CLUB_HITS, 0.45 + p * 0.55);
    }

    public static void playBounce(double friction, double speed) {
        if (speed < 0.55)
            return;

        double volume = clamp(0.18 + speed / 9.0, 0.18, 1.0);
        if (friction <= 0.15) {
            playRandom(SAND_HITS, volume * 0.72);
        } else if (friction >= 0.80) {
            playRandom(ICE_HITS, volume);
        } else {
            playRandom(GRASS_HITS, volume * 0.82);
        }
    }

    public static void playWater(double speed) {
        playRandom(WATER_HITS, clamp(0.35 + speed / 12.0, 0.35, 1.0));
    }

    public static void playHole() {
        playRandom(HOLE_HITS, 0.92);
    }

    public static void playCombo(int comboIndex) {
        play("assets/combo.wav", comboIndex == 0 ? 0.78 : 0.88);
    }

    private static void playRandom(String[] paths, double volume) {
        int index = ThreadLocalRandom.current().nextInt(paths.length);
        play(paths[index], volume);
    }

    private static void play(final String resource, final double volume) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Clip clip = null;
                try {
                    InputStream raw = SoundManager.class.getResourceAsStream(resource);
                    if (raw == null)
                        return;
                    AudioInputStream audio = AudioSystem.getAudioInputStream(
                            new BufferedInputStream(raw));
                    clip = AudioSystem.getClip();
                    final Clip autoClose = clip;
                    clip.addLineListener(new LineListener() {
                        @Override
                        public void update(LineEvent event) {
                            if (event.getType() == LineEvent.Type.STOP) {
                                autoClose.close();
                            }
                        }
                    });
                    clip.open(audio);
                    audio.close();
                    setVolume(clip, volume);
                    clip.start();
                } catch (Throwable ignored) {
                    if (clip != null)
                        clip.close();
                }
            }
        }, "GolfGame-Sound");
        thread.setDaemon(true);
        thread.start();
    }

    private static void setVolume(Clip clip, double volume) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN))
            return;
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        double safe = clamp(volume, 0.01, 1.0);
        float db = (float) (20.0 * Math.log10(safe));
        gain.setValue(Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), db)));
    }

    private static String[] variants(String stem, int count) {
        String[] out = new String[count];
        for (int i = 0; i < count; i++) {
            out[i] = "assets/" + stem + "_" + (i + 1) + ".wav";
        }
        return out;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
