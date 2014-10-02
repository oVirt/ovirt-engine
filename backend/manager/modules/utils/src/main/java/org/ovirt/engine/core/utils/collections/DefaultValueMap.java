package org.ovirt.engine.core.utils.collections;

import java.util.HashMap;

/**
 * A utility class providing a &lt;String, String&gt; map with methods useful
 * for handling default and empty values.
 */
@SuppressWarnings("serial")
public class DefaultValueMap extends HashMap<String, String> {
    public String put(String key, Object value, String def) {
        return put(key, value != null ? value.toString() : def);
    }
    public String putIfNotEmpty(String key, Object value) {
        if (value != null && !"".equals(value.toString())) {
            return put(key, value.toString());
        } else {
            return null;
        }
    }
}

