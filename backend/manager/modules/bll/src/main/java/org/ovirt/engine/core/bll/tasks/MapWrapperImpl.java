package org.ovirt.engine.core.bll.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapWrapperImpl<K, V> implements CacheWrapper<K, V> {

    private final Map<K, V> cache = new HashMap<>();

    public MapWrapperImpl() {
    }

    @Override
    public Set<K> keySet() {
        return getCache().keySet();
    }

    @Override
    public void put(final K key, final V value) {
        getCache().put(key, value);
    }

    @Override
    public V get(final K key) {
        return getCache().get(key);
    }

    @Override
    public void remove(final K key) {
        getCache().remove(key);
    }

    public Map<K, V> getCache() {
        return cache;
    }

    @Override
    public boolean containsKey(final K key) {
        return getCache().containsKey(key);
    }
}
