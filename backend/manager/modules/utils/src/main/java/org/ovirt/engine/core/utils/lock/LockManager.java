package org.ovirt.engine.core.utils.lock;

import java.util.Set;

import org.ovirt.engine.core.common.locks.LockInfo;
import org.ovirt.engine.core.common.utils.Pair;


/**
 * The following interface is represent a lock mechanism
 */
public interface LockManager {

    /**
     * The following method will try to acquire provided lock
     * @return true - in case of locked was acquired , false with set of appropriate error messages otherwise
     */
    Pair<Boolean, Set<String>> acquireLock(EngineLock lock);

    /**
     * The following method will try to acquire lock and will wait until lock acquired The lock should be exclusive and
     * only one, otherwise exception will be thrown
     */
    void acquireLockWait(EngineLock lock);

    /**
     * The following method will release a lock Also it will notify all threads awaiting inside acquireLockWait that
     * some lock was released and they can try to acquire a lock
     */
    void releaseLock(EngineLock lock);

    /**
     * The following method will clear all inserted locks
     */
    void clear();

    /**
     * Query for lock for a given key
     * @param key - key that the lock is mapped to
     * @return lock for the given key, null if does not exist
     */
    LockInfo getLockInfo(String key);
}
