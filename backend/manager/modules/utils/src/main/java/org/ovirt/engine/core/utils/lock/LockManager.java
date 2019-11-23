package org.ovirt.engine.core.utils.lock;

import org.ovirt.engine.core.common.locks.LockInfo;

/**
 * The following interface is represent a lock mechanism
 */
public interface LockManager {

    /**
     * The following method will try to acquire provided lock
     * @return true - in case of locked was acquired , false with set of appropriate error messages otherwise
     */
    LockingResult acquireLock(EngineLock lock);

    /**
     * The following method will try to acquire lock and will wait until lock acquired The lock should be exclusive and
     * only one, otherwise exception will be thrown
     */
    void acquireLockWait(EngineLock lock);

    /**
     * The following method will wait until lock is acquired or until the specified timeout elapses.
     * The lock should be exclusive and only one, otherwise exception will be thrown
     */
    LockingResult acquireLockWait(EngineLock lock, long timeoutMillis);

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

    /**
     * <pre>
     * Query whether an exclusive lock is present in the {@link LockManager}.
     *
     * This method is read-only about the locks and should not modify them.
     * Therefore, the parameter {@link EngineLock} which is provided to this
     * method, should NOT be auto-closed after this method returns because
     * that would release the real lock that the parameter is pointing at.
     * see {@link EngineLock#close()} and {@link AutoCloseable}
     * </pre>
     *
     * @param lock - lock with parameters for searching exclusive locks
     * @return true if a lock was found, false otherwise
     */
    boolean isExclusiveLockPresent(EngineLock lock);
}
