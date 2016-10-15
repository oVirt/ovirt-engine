package org.ovirt.engine.core.common.utils;

import java.util.HashMap;
import java.util.Map;

public class EnumUtils {

    private static Map<Class, Map> cacheEnumValuesInCapitalLetters = new HashMap<>();

    public static <E extends Enum<E>> E valueOf(Class<E> c, String name, boolean ignorecase) {
        if (!ignorecase) {
            return Enum.<E> valueOf(c, name);
        }

        E[] universe = c.getEnumConstants();
        if (universe == null) {
            throw new IllegalArgumentException(name + " is not an enum type");
        }

        Map<String, E> map = cacheEnumValuesInCapitalLetters.get(c);

        if (map == null) {
            // populate the map with enum values and add it to cache
            map = new HashMap<>(2 * universe.length);

            for (E e : universe) {
                map.put(e.name().toUpperCase(), e);
            }
            cacheEnumValuesInCapitalLetters.put(c, map);
        }

        E result = map.get(name.toUpperCase());
        if (result == null) {
            throw new IllegalArgumentException("No enum const " + c.getName() + "." + name);
        }
        return result;
    }

}
