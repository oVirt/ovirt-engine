package org.ovirt.engine.api.restapi.invocation;

/**
 * This manages the object that contains the data associated to the request that is currently being processed. Ideally
 * it should be managed as a request scoped CDI bean, but we don't have CDI support yet.
 */
public class CurrentManager {
    private static final ThreadLocal<Current> local = new ThreadLocal<>();

    public static Current get() {
        return local.get();
    }

    public static void put(Current current) {
        local.set(current);
    }

    public static void remove() {
        local.remove();
    }
}
