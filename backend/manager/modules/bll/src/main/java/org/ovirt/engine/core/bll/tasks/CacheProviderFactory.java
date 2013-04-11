package org.ovirt.engine.core.bll.tasks;

public class CacheProviderFactory {
    public static <K, V> CacheWrapper<K, V> getCacheWrapper(String name) {
        return new MapWrapperImpl<K, V>();
    }
}
