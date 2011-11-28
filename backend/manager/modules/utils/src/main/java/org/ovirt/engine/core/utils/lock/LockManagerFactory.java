package org.ovirt.engine.core.utils.lock;

public class LockManagerFactory {

    private static LockManager lockManager = new InMemoryLockManager();

    public static LockManager getLockManager() {
        return lockManager;
    }
}
