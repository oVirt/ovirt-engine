package org.ovirt.engine.core.common.utils.pm;

import java.util.HashMap;

public class PowerManagementUtils {
    /**
     * Converts a PM Options string to a map.
     *
     * <b>Note:</b> A {@link HashMap} is used instead of the interface {@link Map}, as this method is used by the
     * frontend, and requires GWT compilation.
     *
     * @param pmOptions
     *            String representation of the map
     * @return A parsed map
     */
    public static HashMap<String, String> pmOptionsStringToMap(String pmOptions) {
        HashMap<String, String> map = new HashMap<>();
        if (pmOptions == null || pmOptions.equals("")) {
            return map;
        }
        String[] tokens = pmOptions.split(",");
        for (String token : tokens) {
            String[] pair = token.split("=");
            if (pair.length == 2) { // key=value setting
                pair[1] = pair[1] == null ? "" : pair[1];
                // ignore illegal settings
                if (pair[0].trim().length() > 0 && pair[1].trim().length() > 0) {
                    map.put(pair[0], pair[1]);
                }
            } else { // only key setting
                map.put(pair[0], "");
            }
        }
        return map;
    }
}
