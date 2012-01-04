package org.ovirt.engine.core.utils.lock;


/**
 * The following interface is represent a lock mechanism
 */
public interface LockManager {

    /**
     * The following method will try to acquire provided lock
     * @param lock
     * @return true - in case of locked was acquired , false - otherwise
     */
    boolean acquireLock(EngineLock lock);

    /**
     * The following method will try to acquire lock and will wait until lock acquired
     * @param lock
     */
    void acquireLockWait(EngineLock lock);

    /**
     * The following method will release a lock Also it will notify all threads awaiting inside acquireLockWait that
     * some lock was released and they can try to acquire a lock
     * @param lock
     */
    void releaseLock(EngineLock lock);

    /**
     * The following method will clear all inserted locks
     */
    void clear();

}
