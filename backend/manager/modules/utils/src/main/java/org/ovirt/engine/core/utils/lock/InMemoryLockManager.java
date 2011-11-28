package org.ovirt.engine.core.utils.lock;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

/**
 * The following class an implementation of internal locking mechanism
 */
public class InMemoryLockManager implements LockManager {

    /** A map which is contains all internal representation of locks **/
    private final Map<String, InternalLockView> locks = new HashMap<String, InternalLockView>();
    /** A lock which is used to synchronized acquireLock(), acquireLockWait() and releaseLock() operations **/
    private final Lock globalLock = new ReentrantLock();
    /** A condition which is used in order to notify for waiting threads that some lock was released**/
    private final Condition releasedLock = globalLock.newCondition();

    private static LogCompat log = LogFactoryCompat.getLog(InMemoryLockManager.class);

    @Override
    public boolean acquireLock(EngineLock lock) {
        log.debugFormat("Before acquiring lock {0}", lock);
        globalLock.lock();
        try {
            return acquireLockInternal(lock);
        } finally {
            globalLock.unlock();
        }
    }

    @Override
    public void acquireLockWait(EngineLock lock) {
        log.debugFormat("Before acquiring and wait lock {0}", lock);
        globalLock.lock();
        try {
            boolean firstRun = true;
            while (!acquireLockInternal(lock)) {
                // In case of first try, just wait
                if (firstRun) {
                    firstRun = false;
                } else {
                    // This is a second try, we did not successes, but possible that release signal for other waiting thread
                    // so try to signal to other thread
                    releasedLock.signal();
                }
                releasedLock.await();
            }
        } catch (InterruptedException e) {
            releasedLock.signal();
        } finally {
            globalLock.unlock();
        }
    }

    @Override
    public void releaseLock(EngineLock lock) {
        log.debugFormat("Before releasing a lock {0}", lock);
        globalLock.lock();
        try {
            if (lock.getSharedLocks() != null) {
                for (Entry<String, Guid> entry : lock.getSharedLocks().entrySet()) {
                    releaseSharedLock(buildHashMapKey(entry));
                }
            }
            if (lock.getExclusiveLocks() != null) {
                for (Entry<String, Guid> entry : lock.getExclusiveLocks().entrySet()) {
                    releaseExclusiveLock(buildHashMapKey(entry));
                }
            }
            releasedLock.signal();
        } finally {
            globalLock.unlock();
        }
    }

    @Override
    public void clear() {
        log.warn("All in memory locks are going to be cleaned");
        globalLock.lock();
        try {
            locks.clear();
            releasedLock.signalAll();
        } finally {
            globalLock.unlock();
        }

    }

    /**
     * Internal method should build a key for lock
     * @param entry
     * @return
     */
    private String buildHashMapKey(Entry<String, Guid> entry) {
        return new StringBuilder(entry.getKey()).append(entry.getValue()).toString();
    }

    /**
     * The following method contains a logic for acquiring a lock The method is contains two steps:
     * 1. The lock can be acquired
     * 2. If the first step successes acquire a lock
     * @param lock
     * @return
     */
    private boolean acquireLockInternal(EngineLock lock) {
        boolean checkOnly = true;
        for (int i = 0; i < 2; i++) {
            if (lock.getSharedLocks() != null) {
                for (Entry<String, Guid> entry : lock.getSharedLocks().entrySet()) {
                    if (!insertSharedLock(buildHashMapKey(entry), checkOnly)) {
                        log.debugFormat("Failed to acquire a lock because of shared lock - key :{0} and value {1}",
                                entry.getKey(),
                                entry.getValue());
                        return false;
                    }
                }
            }
            if (lock.getExclusiveLocks() != null) {
                for (Entry<String, Guid> entry : lock.getExclusiveLocks().entrySet()) {
                    if (!insertExclusiveLock(buildHashMapKey(entry), checkOnly)) {
                        log.debugFormat("Failed to acquire a lock because of exclusive lock - key :{0} and value {1}",
                                entry.getKey(),
                                entry.getValue());
                        return false;
                    }
                }
            }
            checkOnly = false;
        }
        log.debug("Successed to acquire a lock");
        return true;
    }

    /**
     * The following method should insert an "shared" internal lock
     * @param key
     * @param isCheckOnly
     *            - is insert or check if lock can be inserted
     * @return
     */
    private boolean insertSharedLock(String key, boolean isCheckOnly) {
        boolean result = true;
        InternalLockView lock = locks.get(key);
        if (lock != null) {
            if (!isCheckOnly) {
                lock.increaseCount();
            } else if (lock.getExclusive()) {
                result = false;
            }
        } else if (!isCheckOnly) {
            locks.put(key, new InternalLockView(1, false));
        }
        return result;
    }

    /**
     * The following method will add exclusive lock, the exclusive key can be
     * added only if there is not exist any shared or exclusive lock for given key
     */
    private boolean insertExclusiveLock(String key, boolean isCheckOnly) {
        if (locks.containsKey(key)) {
            return false;
        }
        if (!isCheckOnly) {
            locks.put(key, new InternalLockView(0, true));
        }
        return true;
    }

    private void releaseExclusiveLock(String key) {
        InternalLockView lock = locks.get(key);
        if (lock != null && lock.getExclusive()) {
            locks.remove(key);
            log.debugFormat("The exclusive lock for key {0} is released and lock is removed from map", key);
        } else if (lock == null) {
            log.warnFormat("Trying to release exclusive lock which is not exist with for {0}", key);
        } else {
            log.warnFormat("Trying to release exclusive lock which is not exclusive lock for key {0}", key);
        }
    }

    private void releaseSharedLock(String key) {
        InternalLockView lock = locks.get(key);
        if (key != null) {
            if (lock.getCount() > 0) {
                lock.decreaseCount();
                log.debugFormat("The shared lock for key {0} is released.", key);
                if (lock.getCount() == 0) {
                    locks.remove(key);
                    log.debugFormat("The shared lock for key {0} is removed from map", key);
                }
            } else {
                log.warnFormat("Trying decrease shared lock index which is 0 is for key {0}", key);
            }
        } else {
            log.warnFormat("Trying release shared lock which is not exist for key {0}", key);
        }
    }

    /**
     * The following class is represents different locks which are kept inside InMemoryLockManager
     */
    private class InternalLockView {

        /** Number for shared locks **/
        private int count;
        /** Indicate if the lock is exclusive and not allowing any other exclusive/shared locks with the same key **/
        private boolean exclusive;

        public InternalLockView(int count, boolean exclusive) {
            this.count = count;
            this.exclusive = exclusive;
        }

        public boolean getExclusive() {
            return exclusive;
        }

        public void setExclusive(boolean exclusive) {
            this.exclusive = exclusive;
        }

        public int getCount() {
            return count;
        }

        public void increaseCount() {
            count++;
        }

        public void decreaseCount() {
            count--;
        }
    }
}
