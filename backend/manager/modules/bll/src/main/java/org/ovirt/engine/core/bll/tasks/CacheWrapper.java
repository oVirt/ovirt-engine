package org.ovirt.engine.core.bll.tasks;

import java.util.Set;

public interface CacheWrapper<K, V> {

    Set<K> keySet();

    void put(K key, V value);

    V get(K key);

    void remove(final K key);

    boolean containsKey(K key);

}
