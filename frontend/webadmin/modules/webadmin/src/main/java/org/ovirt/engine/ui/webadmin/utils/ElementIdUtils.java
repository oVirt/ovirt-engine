package org.ovirt.engine.ui.webadmin.utils;

public class ElementIdUtils {

    /**
     * Creates a DOM element ID based on a prefix and a custom (dynamic) value.
     *
     * @param prefix
     *            Element ID prefix that meets ID constraints (unique, deterministic).
     * @param value
     *            Custom value used to extend the prefix.
     */
    public static String createElementId(String prefix, String value) {
        String sanitizedValue = value.replaceAll("[^\\w]", "_");
        return prefix + "_" + sanitizedValue;
    }

}
