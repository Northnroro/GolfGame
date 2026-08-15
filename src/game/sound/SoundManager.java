package game.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

public final class SoundManager {
    private static final float SAMPLE_RATE = 22050.0f;

    private SoundManager() {
    }

    public static void playClick() {
        playSweep(880.0, 660.0, 55, 0.16);
    }

    public static void playShot(double power) {
        double p = Math.max(0.0, Math.min(1.0, power));
        playSweep(260.0 + p * 120.0, 90.0, 180, 0.18 + p * 0.12);
    }

    public static void playBounce(double speed) {
        double s = Math.max(0.0, Math.min(15.0, speed));
        playSweep(130.0 + s * 16.0, 75.0, 80,
                Math.min(0.24, 0.08 + s * 0.012));
    }

    public static void playHole() {
        playNotes(new double[] { 523.25, 659.25, 783.99, 1046.50 },
                new int[] { 90, 90, 110, 220 }, 0.20);
    }

    public static void playCombo(int comboIndex) {
        if (comboIndex == 0) {
            playNotes(new double[] { 440.0, 659.25, 880.0 },
                    new int[] { 70, 70, 150 }, 0.16);
        } else {
            playSweep(300.0, 900.0, 260, 0.14);
        }
    }

    private static void playSweep(final double startHz, final double endHz,
            final int durationMs, final double volume) {
        runAsync(new Runnable() {
            @Override
            public void run() {
                writeSweep(startHz, endHz, durationMs, volume);
            }
        });
    }

    private static void playNotes(final double[] frequencies,
            final int[] durationsMs, final double volume) {
        runAsync(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < frequencies.length; i++) {
                    writeSweep(frequencies[i], frequencies[i],
                            durationsMs[i], volume);
                }
            }
        });
    }

    private static void runAsync(final Runnable sound) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sound.run();
                } catch (Throwable ignored) {
                    // Audio is optional: never crash when no output device exists.
                }
            }
        }, "GolfGame-Sound");
        thread.setDaemon(true);
        thread.start();
    }

    private static void writeSweep(double startHz, double endHz,
            int durationMs, double volume) {
        SourceDataLine line = null;
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
            line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();

            int samples = Math.max(1,
                    (int) (SAMPLE_RATE * durationMs / 1000.0));
            byte[] data = new byte[samples * 2];
            double phase = 0.0;

            for (int i = 0; i < samples; i++) {
                double t = samples == 1 ? 0.0 : (double) i / (samples - 1);
                double hz = startHz + (endHz - startHz) * t;
                phase += 2.0 * Math.PI * hz / SAMPLE_RATE;
                double envelope = Math.sin(Math.PI * t);
                short sample = (short) (Math.sin(phase) * envelope * volume
                        * Short.MAX_VALUE);
                data[i * 2] = (byte) (sample & 0xff);
                data[i * 2 + 1] = (byte) ((sample >>> 8) & 0xff);
            }

            line.write(data, 0, data.length);
            line.drain();
        } catch (Exception ignored) {
            // Java Sound can be unavailable on some systems; stay silent.
        } finally {
            if (line != null) {
                line.stop();
                line.close();
            }
        }
    }
}
