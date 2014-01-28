package org.ovirt.engine.ui.uicommonweb;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple map that supports default value when requested key doesn't exist.
 * @param <K> key type
 * @param <V> value type
 */
public class MapWithDefaults<K, V> {

    private Map<K, V> data;
    private V defaultValue;

    public MapWithDefaults(Map<K, V> data, V defaultValue) {
        this.data = new HashMap<K, V>();
        this.data.putAll(data);
        this.defaultValue = defaultValue;
    }

    /**
     * Retrieves value under specific key. If the key doesn't exist, returns default value instead.
     */
    public V get(K key) {
        return (data.containsKey(key))
                ? data.get(key)
                : defaultValue;
    }

    /**
     * Returns unmodifiable set of keys.
     */
    public Set<K> keySet() {
        return Collections.unmodifiableSet(data.keySet());
    }

}
