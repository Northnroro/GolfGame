package com.northnroro.golfgame;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

final class Course {
    final Bitmap terrain;
    final Bitmap background;
    final Bitmap friction;
    final int width;
    final int height;
    final int par;
    final int startX;
    final int startY;
    final int finishX;
    final int finishY;

    private final int[] frictionPixels;
    private final boolean[] destroyed;

    Course(Context context, int index) throws IOException {
        String base = "game/stage/hole/CourseTest_Hole" + index;
        terrain = loadBitmap(context, base + ".png");
        background = loadBitmap(context, base + "_background.png");
        friction = loadBitmap(context, base + "_friction.png");
        width = friction.getWidth();
        height = friction.getHeight();

        try (InputStream in = context.getAssets().open(base + "_config.txt");
                Scanner scanner = new Scanner(in)) {
            par = scanner.nextInt();
            startX = scanner.nextInt();
            startY = scanner.nextInt();
            finishX = scanner.nextInt();
            finishY = scanner.nextInt();
        }

        frictionPixels = new int[width * height];
        friction.getPixels(frictionPixels, 0, width, 0, 0, width, height);
        destroyed = new boolean[width * height];
        for (int i = 0; i < frictionPixels.length; i++) {
            destroyed[i] = Color.alpha(frictionPixels[i]) <= 10;
        }
        cutCupOpening();
    }

    private static Bitmap loadBitmap(Context context, String path) throws IOException {
        try (InputStream in = context.getAssets().open(path)) {
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            if (bitmap == null)
                throw new IOException("Cannot decode " + path);
            return bitmap;
        }
    }
    private void cutCupOpening() {
        int radius = 11;
        for (int dx = -radius + 1; dx < radius; dx++) {
            for (int dy = radius / 2; finishY + dy >= 0; dy--) {
                int x = finishX + dx;
                int y = finishY + dy;
                if (x < 0 || x >= width || y < 0 || y >= height)
                    break;
                int pos = y * width + x;
                if (!destroyed[pos]) {
                    destroyed[pos] = true;
                } else {
                    break;
                }
            }
        }
    }

    double frictionAt(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height)
            return 1.0;
        int pos = y * width + x;
        if (destroyed[pos])
            return 1.0;
        return Color.red(frictionPixels[pos]) / 255.0;
    }

    boolean inHole(double x, double y) {
        return Math.hypot(x - finishX, y - finishY) < 55.0;
    }

    void recycle() {
        terrain.recycle();
        background.recycle();
        friction.recycle();
    }
}
