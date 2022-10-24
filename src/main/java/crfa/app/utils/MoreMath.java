package crfa.app.utils;

import javax.annotation.Nullable;

public class MoreMath {

    public static @Nullable Double safeSubtraction(@Nullable Double a, @Nullable Double b) {
        if (a == null || b == null) {
            return null;
        }

        return a - b;
    }

    public static @Nullable Long safeSubtraction(@Nullable Long a, @Nullable Long b) {
        if (a == null || b == null) {
            return null;
        }

        return a - b;
    }

    public static @Nullable Integer safeSubtraction(@Nullable Integer a, @Nullable Integer b) {
        if (a == null || b == null) {
            return null;
        }

        return a - b;
    }

    public static @Nullable Double safeDivision(@Nullable Double a, @Nullable Double b) {
        if (a == null || b == null) {
            return null;
        }
        if (a == 0.0D || b == 0.0D) {
            return 0.0D;
        }

        return a / b;
    }

    public static @Nullable Double safeDivision(@Nullable Long a, @Nullable Long b) {
        if (a == null || b == null) {
            return null;
        }
        if (a == 0 || b == 0) {
            return 0.0D;
        }

        return a.doubleValue() / b.doubleValue();
    }

    public static @Nullable Double safeDivision(@Nullable Integer a, @Nullable Integer b) {
        if (a == null || b == null) {
            return null;
        }
        if (a == 0.0D || b == 0.0D) {
            return 0.0D;
        }

        return a.doubleValue() / b.doubleValue();
    }

    public static Long safeAdd(@Nullable Long a, @Nullable Long b) {
        return (a == null ? 0 : a) + (b == null ? 0 : b);
    }

    public static @Nullable Double safeMultiplication(@Nullable Double a, @Nullable Double b) {
        if (a == null || b == null) {
            return null;
        }
        if (a == 0.0D || b == 0.0D) {
            return 0.0D;
        }

        return a * b;
    }

}
