package com.northnroro.golfgame;

final class Particle {
    double x;
    double y;
    double dx;
    double dy;
    double ddy;
    float size;
    int color;
    long lifeMs;
    boolean screenSpace;

    Particle(double x, double y, double dx, double dy, double ddy,
            float size, int color, long lifeMs, boolean screenSpace) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.ddy = ddy;
        this.size = size;
        this.color = color;
        this.lifeMs = lifeMs;
        this.screenSpace = screenSpace;
    }

    boolean update(long dtMs) {
        double f = dtMs / 16.6667;
        x += dx * f;
        y += dy * f;
        dy += ddy * f;
        lifeMs -= dtMs;
        return lifeMs > 0;
    }
}
