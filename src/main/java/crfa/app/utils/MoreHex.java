package crfa.app.utils;

import static java.util.stream.Collectors.joining;

public class MoreHex {

    public static String toHex(String str) {
        return str.chars().mapToObj(Integer::toHexString).collect(joining());
    }

}

