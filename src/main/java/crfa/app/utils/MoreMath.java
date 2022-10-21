package crfa.app.utils;

import javax.annotation.Nullable;

public class MoreMath {

    @Nullable
    public static Double safeSubtraction(@Nullable Double a, @Nullable Double b) {
        if (a == null || b == null) {
            return null;
        }

        return a - b;
    }

    @Nullable
    public static Long safeSubtraction(@Nullable Long a, @Nullable Long b) {
        if (a == null || b == null) {
            return null;
        }

        return a - b;
    }

    public static Integer safeSubtraction(@Nullable Integer a, @Nullable Integer b) {
        if (a == null || b == null) {
            return null;
        }

        return a - b;
    }

    @Nullable
    public static Double safeDivision(@Nullable Double a, @Nullable Double b) {
        if (a == null || b == null) {
            return null;
        }
        if (a == 0.0D || b == 0.0D) {
            return 0.0D;
        }

        return a / b;
    }

    public static Double safeDivision(@Nullable Long a, @Nullable Long b) {
        if (a == null || b == null) {
            return null;
        }
        if (a == 0 || b == 0) {
            return 0.0D;
        }

        return a.doubleValue() / b.doubleValue();
    }

    public static Double safeDivision(@Nullable Integer a, @Nullable Integer b) {
        if (a == null || b == null) {
            return null;
        }
        if (a == 0.0D || b == 0.0D) {
            return 0.0D;
        }

        return a.doubleValue() / b.doubleValue();
    }

    public static Double safeMultiplication(@Nullable Double a, @Nullable Double b) {
        if (a == null || b == null) {
            return null;
        }
        if (a == 0.0D || b == 0.0D) {
            return 0.0D;
        }

        return a * b;
    }

}
