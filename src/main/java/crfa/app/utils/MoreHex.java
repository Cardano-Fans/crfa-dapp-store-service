package crfa.app.utils;

import java.util.stream.Collectors;

public class MoreHex {

    public static String toHex(String str) {
        return str.chars().mapToObj(Integer::toHexString).collect(Collectors.joining());
    }

}
