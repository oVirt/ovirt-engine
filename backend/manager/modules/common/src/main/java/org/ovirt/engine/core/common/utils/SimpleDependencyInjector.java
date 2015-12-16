package org.ovirt.engine.core.common.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is needed to decouple the searchbackend dependencies without introducing any DI frameworks or convulated
 * reference passing. It's a lightweight instance factory to solve situations where UI searchbackend needs different
 * instances than backend.
 * <p/>
 * Since searchbackend code is shared by UI and backend there is no way to reach other projects, that are specific to
 * each other, such as dal or uicommonweb.
 * <p/>
 * The first use case is searchbackend relying on Enums for value completion. If we want a completion object that
 * has values populated by code that is only accessible by both UI and backend we should better have the object
 * dependency managed outside, by the SyntaxChecker initiator.
 *
 * @see SyntaxChecker
 */
public class SimpleDependencyInjector {

    private static final SimpleDependencyInjector instance = new SimpleDependencyInjector();

    private Map<String, Object> map;

    public static SimpleDependencyInjector getInstance() {
        return instance;
    }

    private SimpleDependencyInjector() {
        map = new ConcurrentHashMap<>();
    }

    /**
     * save an instance to the injector. this instance can be later fetched by the {@code SimpleDependencyInjector#get}
     * method. note: only one instance binded to a Class type
     *
     * @param type
     *            the instance to be kept
     */
    public <T> void bind(T type) {
        map.put(type.getClass().getName(), type);
    }

    /**
     * bind an instance to a specific class type. This is needed in case bind(T) have concrete types <br>
     * over interface which have method. note: only one instance is bounded to a Class type
     * @param clazz
     *            the class representing the key this instance should be get on.
     * @param type
     *            the instance to be kept
     */
    public <T> void bind(Class<? super T> clazz, T type) {
        map.put(clazz.getName(), type);
    }

    /**
     * get the instance associated with the Class type note: only one instance binded to a Class type
     */
    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> clazz) {
        return (T) map.get(clazz.getName());
    }

}
