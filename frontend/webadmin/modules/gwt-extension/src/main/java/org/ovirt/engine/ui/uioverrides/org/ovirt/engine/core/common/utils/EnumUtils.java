package org.ovirt.engine.core.common.utils;

import java.util.HashMap;
import java.util.Map;
import com.google.gwt.regexp.shared.RegExp;
import org.ovirt.engine.core.compat.*;

public class EnumUtils {

    private static Map<Class, Map> cacheEnumValuesInCapitalLetters = new HashMap<>();

    public static String ConvertToStringWithSpaces(String value) {
        /*TODO-GWT
        StringBuilder result = new StringBuilder();
        RegExp r = RegExp.compile("^([A-Z]{1,}[a-z]*)|([0-9]*)$");
        com.google.gwt.regexp.shared.MatchResult = r.exec(value);

        for (int i = 0; i < mr.groupCount(); i++) {
            result.append(mr.group(i));
            if (i + 1 != mr.groupCount()) {
                result.append(" ");
            }
        }
        return result.toString().trim();
        */
        return null;
    }

    public static <E extends Enum<E>> E valueOf(Class<E> c, String name, boolean ignorecase) {
        if (!ignorecase) {
            {
                return Enum.<E> valueOf(c, name);
            }
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
