package com.northnroro.golfgame;

import java.util.List;

final class BallState {
    static final int SIZE = 11;
    static final double GRAVITY = 0.2;
    static final double MAX_SPEED = 10.0;
    private static final double STOP_DIFFERENCE = 3.5;
    private static final int[] COMBO_CODES = { 0b110110001010, 0b001011010000 };

    enum Result {
        MOVING, STOPPED, HOLED, OUT_OF_BOUNDS
    }

    double x;
    double y;
    double dx;
    double dy;
    boolean shootingStar;
    boolean tornado;
    boolean bouncedThisStep;
    double bounceFriction;
    double bounceImpact;

    private double differencePosition;
    private long lastBounceSoundMs;

    BallState() {
    }
    void reset(double x, double y) {
        this.x = x;
        this.y = y;
        dx = 0;
        dy = 0;
        differencePosition = 999;
        shootingStar = false;
        tornado = false;
        bouncedThisStep = false;
    }

    void launch(double angleDegrees, double power, List<Integer> comboInput,
            AndroidSound sound) {
        double angle = Math.toRadians(angleDegrees);
        dx = MAX_SPEED * power * Math.cos(angle);
        dy = MAX_SPEED * power * Math.sin(angle);
        differencePosition = 999;
        decodeCombos(comboInput, sound);
    }

    private void decodeCombos(List<Integer> input, AndroidSound sound) {
        shootingStar = false;
        tornado = false;
        checkComboBlock(input, 0, sound);
        if (input.size() >= 12)
            checkComboBlock(input, 6, sound);
    }

    private void checkComboBlock(List<Integer> input, int offset, AndroidSound sound) {
        if (input.size() < offset + 6)
            return;
        int code = 0;
        for (int i = 0; i < 6; i++)
            code = (code << 2) + input.get(offset + i);
        for (int i = 0; i < COMBO_CODES.length; i++) {
            if (code == COMBO_CODES[i]) {
                if (i == 0)
                    shootingStar = true;
                else
                    tornado = true;
                if (sound != null)
                    sound.combo(i);
            }
        }
    }

    Result update(Course course, AndroidSound sound, double shotAngleDegrees) {
        bouncedThisStep = false;
        dy += GRAVITY;
        setX(x + dx);
        setY(y + dy);

        Hit hit = findHit(course);
        if (hit != null) {
            handleCollision(course, hit, sound, shotAngleDegrees);
        }

        if (y > course.height + 10) {
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

        if (shootingStar && !tornado) {
            dx = 0;
            dy = 0;
            shootingStar = false;
        }
        if (tornado) {
            double boosted = Math.hypot(dx, dy) * 1.5;
            double angle = Math.toRadians(shotAngleDegrees);
            dx = boosted * Math.cos(angle);
            dy = boosted * Math.sin(angle);
            tornado = false;
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
