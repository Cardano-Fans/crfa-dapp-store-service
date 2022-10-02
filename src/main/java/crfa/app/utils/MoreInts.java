package crfa.app.utils;

public class MoreInts {

    public static int nullSafe(Integer i) {
        return i == null ? 0 : i.intValue();
    }

    public static long nullSafe(Long i) {
        return i == null ? 0L : i.longValue();
    }

}
