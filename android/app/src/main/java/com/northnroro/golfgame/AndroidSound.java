package com.northnroro.golfgame;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

final class AndroidSound {
    private final SoundPool pool;
    private final int[] club = new int[3];
    private final int[] grass = new int[3];
    private final int[] ice = new int[3];
    private final int[] sand = new int[3];
    private final int[] water = new int[3];
    private final int[] hole = new int[3];
    private final int click;
    private final int combo;

    AndroidSound(Context context) {
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        pool = new SoundPool.Builder().setMaxStreams(12).setAudioAttributes(attrs).build();
        for (int i = 0; i < 3; i++) {
            int n = i + 1;
            club[i] = load(context, "game/sound/assets/club_hit_" + n + ".wav");
            grass[i] = load(context, "game/sound/assets/grass_" + n + ".wav");
            ice[i] = load(context, "game/sound/assets/ice_" + n + ".wav");
            sand[i] = load(context, "game/sound/assets/sand_" + n + ".wav");
            water[i] = load(context, "game/sound/assets/water_" + n + ".wav");
            hole[i] = load(context, "game/sound/assets/hole_" + n + ".wav");
        }
        click = load(context, "game/sound/assets/menu_click.wav");
        combo = load(context, "game/sound/assets/combo.wav");
    }

    private int load(Context context, String path) {
        try (AssetFileDescriptor fd = context.getAssets().openFd(path)) {
            return pool.load(fd, 1);
        } catch (IOException e) {
            return 0;
        }
    }

    void click() {
        play(click, 0.55f);
    }
    void shot(double power) {
        playRandom(club, (float) clamp(0.45 + power * 0.55, 0.45, 1.0));
    }

    void bounce(double friction, double impactSpeed) {
        if (impactSpeed < 0.55)
            return;
        float volume = (float) clamp(0.18 + impactSpeed / 9.0, 0.18, 1.0);
        if (friction <= 0.15) {
            playRandom(sand, volume * 0.72f);
        } else if (friction >= 0.80) {
            playRandom(ice, volume);
        } else {
            playRandom(grass, volume * 0.82f);
        }
    }

    void water(double speed) {
        playRandom(water, (float) clamp(0.35 + speed / 12.0, 0.35, 1.0));
    }

    void hole() {
        playRandom(hole, 0.92f);
    }

    void combo(int index) {
        play(combo, index == 0 ? 0.78f : 0.88f);
    }
    private void playRandom(int[] sounds, float volume) {
        int id = sounds[ThreadLocalRandom.current().nextInt(sounds.length)];
        play(id, volume);
    }

    private void play(int id, float volume) {
        if (id != 0)
            pool.play(id, volume, volume, 1, 0, 1.0f);
    }

    void release() {
        pool.release();
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
