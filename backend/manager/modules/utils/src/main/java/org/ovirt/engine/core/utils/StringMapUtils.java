package org.ovirt.engine.core.utils;

import java.util.HashMap;
import java.util.Map;

public class StringMapUtils {
    private static final String KEY_VALUE_SEPARATOR = "=";
    private static final String PAIRS_SEPARATOR = ",";

    /**
     * Converting a comma delimited key=value format string to a key,value map
     * values should not contain the equal sign (=)
     * @return a Map of the key/value pairs
     */
    public static Map<String, String> string2Map(String str) {

        Map<String, String> map = new HashMap<>();
        if (str != null) {
            // remove map markers
            str = str.trim();
            if (str.startsWith("{")) {
                str = str.substring(1, str.length() - 1);
            }
            if (str.endsWith("}")) {
                str = str.substring(0, str.length() - 1);
            }
            str = str.trim();
            if (str.length() > 0) {
                String[] keyValPairs = str.split(PAIRS_SEPARATOR);
                for (String pair : keyValPairs) {
                    String[] keyval = pair.split(KEY_VALUE_SEPARATOR);
                    if (keyval.length == 2) {
                        map.put(keyval[0].trim(), keyval[1].trim());
                    } else if (keyval.length == 1) {
                        map.put(keyval[0].trim(), "");
                    }
                }
            }
        }
        return map;
    }
}
