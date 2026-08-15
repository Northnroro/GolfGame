package com.northnroro.golfgame;

import java.util.List;

import game.core.PangyaMechanics;
import game.core.PangyaMechanics.SpecialShot;

final class BallState {
    static final int SIZE = 11;
    static final double GRAVITY = 0.2;
    static final double MAX_SPEED = 10.0;
    private static final double STOP_DIFFERENCE = 3.5;

    enum Result { MOVING, STOPPED, HOLED, OUT_OF_BOUNDS }

    double x;
    double y;
    double dx;
    double dy;
    boolean bouncedThisStep;
    double bounceFriction;
    double bounceImpact;
    SpecialShot specialShot = SpecialShot.NORMAL;

    private double differencePosition;
    private long lastBounceSoundMs;
    private int flightTicks;
    private boolean specialLandingApplied;

    void reset(double x, double y) {
        this.x = x;
        this.y = y;
        dx = 0;
        dy = 0;
        differencePosition = 999;
        bouncedThisStep = false;
        specialShot = SpecialShot.NORMAL;
        flightTicks = 0;
        specialLandingApplied = false;
    }

    void launch(double angleDegrees, double power, double impactMeter,
            List<Integer> comboInput, AndroidSound sound) {
        specialShot = PangyaMechanics.decodeSpecial(comboInput, power);
        double correctedAngle = angleDegrees + PangyaMechanics.hookDegrees(impactMeter);
        double speed = MAX_SPEED * power * PangyaMechanics.impactPowerMultiplier(impactMeter);
        double angle = Math.toRadians(correctedAngle);
        dx = speed * Math.cos(angle);
        dy = speed * Math.sin(angle);

        switch (specialShot) {
        case TOMAHAWK:
            dx *= 1.02;
            dy *= 1.28;
            break;
        case COBRA:
            dx *= 1.08;
            dy *= 0.38;
            break;
        case SPIKE:
            dx *= 0.98;
            dy *= 1.42;
            break;
        case TOPSPIN:
            dy *= 0.88;
            break;
        case BACKSPIN:
            dy *= 1.12;
            break;
        case CURVE_LEFT:
            dx *= 0.97;
            break;
        case CURVE_RIGHT:
            dx *= 1.03;
            break;
        default:
            break;
        }

        differencePosition = 999;
        flightTicks = 0;
        specialLandingApplied = false;
        if (specialShot != SpecialShot.NORMAL && sound != null)
            sound.combo(specialShot.ordinal() & 1);
    }

    Result update(Course course, AndroidSound sound, double shotAngleDegrees,
            double windSpeed, double windAngleDegrees) {
        bouncedThisStep = false;
        flightTicks++;

        applySpecialFlight();
        dx += PangyaMechanics.windAx(windSpeed, windAngleDegrees);
        dy += PangyaMechanics.windAy(windSpeed, windAngleDegrees);
        dy += GRAVITY;
        setX(x + dx);
        setY(y + dy);

        Hit hit = findHit(course);
        if (hit != null)
            handleCollision(course, hit, sound, shotAngleDegrees);

        if (y > course.height + 10 || x < -80 || x > course.width + 80) {
            if (sound != null)
                sound.water(Math.hypot(dx, dy));
            return Result.OUT_OF_BOUNDS;
        }

        if (isStopped()) {
            dx = 0;
            dy = 0;
            if (course.inHole(x, y)) {
                if (sound != null)
                    sound.hole();
                return Result.HOLED;
            }
            return Result.STOPPED;
        }
        return Result.MOVING;
    }

    private void applySpecialFlight() {
        switch (specialShot) {
        case COBRA:
            if (flightTicks > 22 && flightTicks < 72)
                dy -= 0.075;
            break;
        case SPIKE:
            if (flightTicks > 34 && dy > -1.2)
                dy += 0.16;
            break;
        case CURVE_LEFT:
            dx -= 0.010;
            break;
        case CURVE_RIGHT:
            dx += 0.010;
            break;
        default:
            break;
        }
    }

    private void handleCollision(Course course, Hit hit, AndroidSound sound,
            double shotAngleDegrees) {
        double speed = Math.hypot(dx, dy);
        double normalLength = Math.hypot(hit.nx, hit.ny);
        double impact = normalLength > 1e-6
                ? Math.abs(dx * hit.nx + dy * hit.ny) / normalLength
                : speed;

        long now = System.currentTimeMillis();
        if (impact > 0.45 && now - lastBounceSoundMs > 110) {
            if (sound != null)
                sound.bounce(hit.friction, impact);
            lastBounceSoundMs = now;
            bouncedThisStep = true;
            bounceFriction = hit.friction;
            bounceImpact = impact;
        }

        double originalAngle = Math.atan2(dy, dx);
        double hitAngle = Math.atan2(hit.ny, hit.nx);
        double reflectedAngle = 2.0 * hitAngle - originalAngle;
        pushOutOfSurface(course, hit);
        speed *= hit.friction;
        dx = -speed * Math.cos(reflectedAngle);
        dy = -speed * Math.sin(reflectedAngle);

        if (!specialLandingApplied) {
            switch (specialShot) {
            case TOMAHAWK:
                dx = 0;
                dy = 0;
                break;
            case TOPSPIN:
                dx *= 1.30;
                dy *= 0.55;
                break;
            case BACKSPIN:
                dx *= -0.34;
                dy *= 0.45;
                break;
            case SPIKE:
                dx *= 0.82;
                break;
            default:
                break;
            }
            specialLandingApplied = true;
        }
    }

    private void pushOutOfSurface(Course course, Hit initial) {
        Hit hit = initial;
        for (int i = 0; i < 20 && hit != null; i++) {
            double len = Math.hypot(hit.nx, hit.ny);
            if (len < 1e-6)
                break;
            setX(x - hit.nx / len);
            setY(y - hit.ny / len);
            hit = findHit(course);
        }
    }

    private Hit findHit(Course course) {
        int count = 0;
        double nx = 0;
        double ny = 0;
        double friction = 0;
        double radius = SIZE / 2.0;
        for (int i = 0; i < 10; i++) {
            double px = radius * Math.cos(2.0 * Math.PI * i / 10.0);
            double py = radius * Math.sin(2.0 * Math.PI * i / 10.0);
            int checkX = (int) Math.round(x + px);
            int checkY = (int) Math.round(y + py);
            double f = course.frictionAt(checkX, checkY);
            if (f < 1.0 - 1e-5) {
                count++;
                nx += px;
                ny += py;
                friction += f;
            }
        }
        if (count == 0)
            return null;
        return new Hit(nx, ny, friction / count);
    }

    boolean isStopped() {
        return differencePosition < STOP_DIFFERENCE;
    }

    private void setX(double value) {
        differencePosition += Math.abs(value - x);
        differencePosition /= 1.1;
        x = value;
    }

    private void setY(double value) {
        differencePosition += Math.abs(value - y);
        differencePosition /= 1.1;
        y = value;
    }

    BallState previewCopy(double launchDx, double launchDy) {
        BallState copy = new BallState();
        copy.x = x;
        copy.y = y;
        copy.dx = launchDx;
        copy.dy = launchDy;
        copy.differencePosition = 999;
        return copy;
    }

    private static final class Hit {
        final double nx;
        final double ny;
        final double friction;
        Hit(double nx, double ny, double friction) {
            this.nx = nx;
            this.ny = ny;
            this.friction = friction;
        }
    }
}
