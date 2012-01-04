package org.ovirt.engine.core.bll.lock;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Local;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;

/**
 * The following class an implementation of internal locking mechanism
 */
@Startup
@Singleton(name = "LockManager")
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Local(LockManager.class)
public class InMemoryLockManager implements LockManager, LockManagerMonitorMXBean {

    /** A map which is contains all internal representation of locks **/
    private final Map<String, InternalLockView> locks = new HashMap<String, InternalLockView>();
    /** A lock which is used to synchronized acquireLock(), acquireLockWait() and releaseLock() operations **/
    private final Lock globalLock = new ReentrantLock();
    /** A condition which is used in order to notify for waiting threads that some lock was released**/
    private final Condition releasedLock = globalLock.newCondition();

    private MBeanServer platformMBeanServer;
    private ObjectName objectName = null;
    private static LogCompat log = LogFactoryCompat.getLog(InMemoryLockManager.class);

    @PostConstruct
    public void registerInJMX() {
        try {
            objectName = new ObjectName("InMemoryLockManager:type=" + this.getClass().getName());
            platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            platformMBeanServer.registerMBean(this, objectName);
        } catch (Exception e) {
            throw new IllegalStateException("Problem during registration of Monitoring into JMX:" + e);
        }
    }

    @PreDestroy
    public void unregisterFromJMX() {
        try {
            platformMBeanServer.unregisterMBean(this.objectName);
        } catch (Exception e) {
            throw new IllegalStateException("Problem during unregistration of Monitoring into JMX:" + e);
        }
    }

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
        log.warn("Cleaning all in memory locks");
        globalLock.lock();
        try {
            locks.clear();
            releasedLock.signalAll();
        } finally {
            globalLock.unlock();
        }
    }

    @Override
    public boolean releaseLock(String lockId) {
        log.warnFormat("The following lock is going to be released via external call, lockId {0} ", lockId);
        globalLock.lock();
        try {
            InternalLockView lock = locks.get(lockId);
            if (lock == null) {
                log.warnFormat("Lock with id {0} does not exist and can not be released via external call", lockId);
                return false;
            }
            if (lock.getExclusive()) {
                releaseExclusiveLock(lockId);
            } else {
                releaseSharedLock(lockId);
            }
            releasedLock.signal();
        } finally {
            globalLock.unlock();
        }
        log.warnFormat("Lock {0} was released via external call", lockId);
        return true;
    }

    @Override
    public List<String> showAllLocks() {
        List<String> returnValue;
        log.debug("All in memory locks will be shown");
        globalLock.lock();
        try {
            returnValue = new ArrayList<String>();
            for(Map.Entry<String, InternalLockView> entry : locks.entrySet()) {
                String lock = new StringBuilder("The object id is : ").append(entry.getKey()).append(' ').append(entry.getValue()).toString();
                returnValue.add(lock);
            }
        } finally {
            globalLock.unlock();
        }
        log.debug("All in memory locks were shown");
        return returnValue;
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
                        log.debugFormat("Failed to acquire lock. Shared lock is taken for key :{0} , value: {1}",
                                entry.getKey(),
                                entry.getValue());
                        return false;
                    }
                }
            }
            if (lock.getExclusiveLocks() != null) {
                for (Entry<String, Guid> entry : lock.getExclusiveLocks().entrySet()) {
                    if (!insertExclusiveLock(buildHashMapKey(entry), checkOnly)) {
                        log.debugFormat("Failed to acquire lock. Exclusive lock is taken for key: {0} , value: {1}",
                                entry.getKey(),
                                entry.getValue());
                        return false;
                    }
                }
            }
            checkOnly = false;
        }
        log.debugFormat("Successed acquiring lock {0} succeeded ", lock);
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
            log.warnFormat("Trying to release exclusive lock which does not exist, lock key: {0}", key);
        } else {
            log.warnFormat("Trying to release exclusive lock but lock is not exclusive. lock key: {0}", key);
        }
    }

    private void releaseSharedLock(String key) {
        InternalLockView lock = locks.get(key);
        if (lock != null) {
            if (lock.getCount() > 0) {
                lock.decreaseCount();
                log.debugFormat("The shared lock for key {0} is released.", key);
                if (lock.getCount() == 0) {
                    locks.remove(key);
                    log.debugFormat("The shared lock for key {0} is removed from map", key);
                }
            } else {
                log.warnFormat("Trying to decrease a shared lock for key: {0} , but shared index is 0", key);
            }
        } else {
            log.warnFormat("Trying to release a shared lock for key: {0} , but lock does not exist", key);
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

        public int getCount() {
            return count;
        }

        public void increaseCount() {
            count++;
        }

        public void decreaseCount() {
            count--;
        }

        @Override
        public String toString() {
            if(exclusive) {
                return "The lock is exclusive";
            }
            return "The lock is shared and a number of shared locks is " + count;
        }
    }
}
