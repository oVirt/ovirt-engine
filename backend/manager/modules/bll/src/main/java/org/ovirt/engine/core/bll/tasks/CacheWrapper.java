package org.ovirt.engine.core.bll.tasks;

public interface CacheWrapper<K, V> {

    void put(K key, V value);

    V get(K key);

    void remove(final K key);

    public boolean containsKey(K key);

}
