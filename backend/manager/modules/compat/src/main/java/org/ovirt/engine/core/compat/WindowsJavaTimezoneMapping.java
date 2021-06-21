package org.ovirt.engine.core.compat;

import java.util.Map;
import java.util.Set;

public class WindowsJavaTimezoneMapping {


    private static Map<String, String> windowsToJava;


    public static void init(Map<String, String> windowsToJava) {
        WindowsJavaTimezoneMapping.windowsToJava = windowsToJava;
    }

    public static Set<String> getKeys() {
        return windowsToJava.keySet();
    }

    public static String get(String key) {
        return windowsToJava.get(key);
    }

}
