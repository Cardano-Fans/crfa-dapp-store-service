package crfa.app.utils;

import lombok.val;

import java.util.HashMap;
import java.util.Map;

public class MoreMaps {

    public static <T, E> Map<T, E> addMaps(Map<T, E> a,
                                            Map<T, E> b) {
        val c = new HashMap<T, E>();
        c.putAll(a);
        c.putAll(b);

        return c;
    }

}
