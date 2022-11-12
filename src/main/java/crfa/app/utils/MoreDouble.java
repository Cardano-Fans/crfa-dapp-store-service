package crfa.app.utils;

public class MoreDouble {

    public static boolean isInvalid(Double d) {
        return Double.isInfinite(d) || Double.isNaN(d);
    }

}
