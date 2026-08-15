package game.core;

import java.util.List;

public final class PangyaMechanics {
    public static final double IMPACT_CENTER = 0.045;
    public static final double PANGYA_HALF_WIDTH = 0.014;
    public static final double IMPACT_ZONE_HALF_WIDTH = 0.075;
    public static final double SPECIAL_MIN_POWER = 0.80;

    public enum SpecialShot {
        NORMAL, TOPSPIN, BACKSPIN, CURVE_LEFT, CURVE_RIGHT,
        TOMAHAWK, COBRA, SPIKE
    }

    private PangyaMechanics() {}

    public static SpecialShot decodeSpecial(List<Integer> input, double power) {
        if (input == null || input.isEmpty()) return SpecialShot.NORMAL;
        int n = input.size();
        if (power >= SPECIAL_MIN_POWER && n >= 2) {
            int a = input.get(n - 2);
            int b = input.get(n - 1);
            if (a == 0 && b == 2) return SpecialShot.TOMAHAWK; // up, down
            if (a == 1 && b == 0) return SpecialShot.COBRA;    // right, up
            if (a == 1 && b == 2) return SpecialShot.SPIKE;    // right, down
        }
        int last = input.get(n - 1);
        if (last == 0) return SpecialShot.TOPSPIN;
        if (last == 2) return SpecialShot.BACKSPIN;
        if (last == 3) return SpecialShot.CURVE_LEFT;
        if (last == 1) return SpecialShot.CURVE_RIGHT;
        return SpecialShot.NORMAL;
    }

    public static double impactError(double meterPosition) {
        return meterPosition - IMPACT_CENTER;
    }

    public static boolean isPangya(double meterPosition) {
        return Math.abs(impactError(meterPosition)) <= PANGYA_HALF_WIDTH;
    }

    public static boolean isInsideImpactZone(double meterPosition) {
        return Math.abs(impactError(meterPosition)) <= IMPACT_ZONE_HALF_WIDTH;
    }

    public static double impactPowerMultiplier(double meterPosition) {
        double error = Math.abs(impactError(meterPosition));
        if (error <= PANGYA_HALF_WIDTH) return 1.035;
        if (error <= IMPACT_ZONE_HALF_WIDTH) return 1.0 - error * 0.9;
        return Math.max(0.72, 0.94 - error * 1.8);
    }

    public static double hookDegrees(double meterPosition) {
        double e = impactError(meterPosition);
        if (Math.abs(e) <= PANGYA_HALF_WIDTH) return 0.0;
        return Math.max(-18.0, Math.min(18.0, e * 135.0));
    }

    public static double windAx(double speed, double angleDegrees) {
        return Math.cos(Math.toRadians(angleDegrees)) * speed * 0.0032;
    }

    public static double windAy(double speed, double angleDegrees) {
        return Math.sin(Math.toRadians(angleDegrees)) * speed * 0.0011;
    }
}
