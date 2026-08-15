package com.northnroro.golfgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

final class PlayerState {
    final int number;
    final boolean cpu;
    final BallState ball = new BallState();
    final Bitmap spriteSheet;
    final int[] scores = new int[3];
    final boolean[] holeOut = new boolean[3];
    int animationFrame;
    double golferX;
    double golferY;

    PlayerState(Context context, int number, boolean cpu) throws IOException {
        this.number = number;
        this.cpu = cpu;
        Arrays.fill(scores, Integer.MIN_VALUE);
        try (InputStream in = context.getAssets().open(
                "game/character/player" + number + ".png")) {
            spriteSheet = BitmapFactory.decodeStream(in);
        }
        if (spriteSheet == null)
            throw new IOException("Cannot load player sprite " + number);
    }
    void plantGolferAtBall(double aimAngleDegrees) {
        // Stand behind the ball instead of occupying the same point. The golfer
        // stays planted here during flight while only the ball travels.
        double direction = Math.cos(Math.toRadians(aimAngleDegrees)) >= 0.0 ? -1.0 : 1.0;
        golferX = ball.x + direction * 24.0;
        golferY = ball.y;
    }

    void recordScore(int hole, int relativeToPar) {
        scores[hole] = relativeToPar;
        holeOut[hole] = true;
    }

    int totalScore() {
        int total = 0;
        for (int score : scores) {
            if (score != Integer.MIN_VALUE)
                total += score;
        }
        return total;
    }

    String label() {
        return (cpu ? "CPU#" : "Player#") + (number + 1);
    }

    void recycle() {
        spriteSheet.recycle();
    }
}
