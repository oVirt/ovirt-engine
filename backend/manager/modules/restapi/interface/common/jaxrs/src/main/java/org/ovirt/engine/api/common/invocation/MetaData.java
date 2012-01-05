package org.ovirt.engine.api.common.invocation;

import java.util.HashMap;
import java.util.Map;

public class MetaData {
    private Map<String, Object> meta;

    public MetaData() {
        meta = new HashMap<String, Object>();
    }

    public void set(String key, Object value) {
        meta.put(key, value);
    }

    public Object get(String key) {
        return meta.get(key);
    }

    public boolean hasKey(String key) {
        return meta.containsKey(key);
    }

    public void remove(String key) {
        meta.remove(key);
    }
}
