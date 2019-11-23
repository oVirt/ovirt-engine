package org.ovirt.engine.core.bll.lock;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

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

import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockInfo;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.lock.LockingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final Map<String, InternalLockView> locks = new HashMap<>();
    /** A lock which is used to synchronized acquireLock(), acquireLockWait() and releaseLock() operations **/
    private final Lock globalLock = new ReentrantLock();
    /** A condition which is used in order to notify for waiting threads that some lock was released**/
    private final Condition releasedLock = globalLock.newCondition();

    private MBeanServer platformMBeanServer;
    private ObjectName objectName = null;
    private static final Logger log = LoggerFactory.getLogger(InMemoryLockManager.class);

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
    public LockingResult acquireLock(EngineLock lock) {
        log.debug("Before acquiring lock '{}'", lock);
        globalLock.lock();
        try {
            return acquireLockInternal(lock);
        } finally {
            globalLock.unlock();
        }
    }

    @Override
    public void acquireLockWait(EngineLock lock) {
        log.debug("Before acquiring and wait lock '{}'", lock);
        validateLockForAcquireAndWait(lock);
        globalLock.lock();
        try {
            while (!acquireLockInternal(lock).isAcquired()) {
                log.info("Failed to acquire lock and wait lock '{}'", lock);
                releasedLock.await();
            }
        } catch (InterruptedException ignore) {

        } finally {
            globalLock.unlock();
        }
    }

    @Override
    public LockingResult acquireLockWait(EngineLock lock, long timeoutMillis) {
        log.debug("Before acquiring wait or timeout lock '{}'", lock);
        validateLockForAcquireAndWait(lock);
        if (timeoutMillis <= 0) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        long timeoutNanos = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        LockingResult lockAcquired = LockingResult.fail();
        globalLock.lock();
        try {
            do {
                lockAcquired = acquireLockInternal(lock);
                if (!lockAcquired.isAcquired()) {
                    if (timeoutNanos <= 0L) {
                        log.info("Failed to acquire lock because timeout was reached. lock {}", lock);
                        break;
                    }
                    log.info("Failed to acquire lock, will try again until timeout. lock '{}'", lock);
                    timeoutNanos = releasedLock.awaitNanos(timeoutNanos);
                }
            } while (!lockAcquired.isAcquired());
        } catch (InterruptedException ignore) {
            log.info("Acquire lock operation was interrupted. lock '{}'", lock);
        } finally {
            globalLock.unlock();
        }
        return lockAcquired;
    }

    private void validateLockForAcquireAndWait(EngineLock lock) {
        if (lock.getSharedLocks() != null && lock.getExclusiveLocks().size() > 1) {
            log.error("Trying to acquire or wait on shared or more than one exclusive locks '{}'", lock);
            throw new IllegalArgumentException("Trying to acquire or wait on shared or more than one exclusive locks");
        }
    }

    @Override
    public void releaseLock(EngineLock lock) {
        log.debug("Before releasing a lock '{}'", lock);
        globalLock.lock();
        try {
            if (lock.getSharedLocks() != null) {
                lock.getSharedLocks().entrySet().stream().forEach(entry ->
                    releaseSharedLock(buildHashMapKey(entry), entry.getValue().getSecond()));
            }
            if (lock.getExclusiveLocks() != null) {
                lock.getExclusiveLocks().entrySet().stream().forEach(entry ->
                    releaseExclusiveLock(buildHashMapKey(entry)));
            }
            releasedLock.signalAll();
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
        log.warn("The following lock is going to be released via external call, lockId '{}', error message can be"
                + " left for shared lock",
                lockId);
        globalLock.lock();
        try {
            InternalLockView lock = locks.get(lockId);
            if (lock == null) {
                log.warn("Lock with id '{}' does not exist and can not be released via external call", lockId);
                return false;
            }
            if (lock.getExclusive()) {
                releaseExclusiveLock(lockId);
            } else {
                releaseSharedLock(lockId, null);
            }
            releasedLock.signalAll();
        } finally {
            globalLock.unlock();
        }
        log.warn("Lock '{}' was released via external call", lockId);
        return true;
    }

    @Override
    public List<String> showAllLocks() {
        log.debug("All in memory locks will be shown");
        globalLock.lock();
        try {
            return locks.entrySet().stream().map(this::createLockDescription).collect(Collectors.toList());
        } finally {
            globalLock.unlock();
            log.debug("All in memory locks were shown");
        }
    }

    private String createLockDescription(Entry<String, InternalLockView> e) {
        return "The object id is : " + e.getKey() + ' ' + e.getValue();
    }

    /**
     * Internal method should build a key for lock
     */
    private String buildHashMapKey(Entry<String, Pair<String, String>> entry) {
        return entry.getKey() + entry.getValue().getFirst();
    }

    /**
     * The following method contains a logic for acquiring a lock. It is comprised of two steps:
     * 1. Check if the lock can be acquired
     * 2. If the first step succeeds, acquire a lock
     */
    private LockingResult acquireLockInternal(EngineLock lock) {
        var result = acquireLockInternalStep(lock, true);
        if (!result.isAcquired()) {
            return result;
        }

        result = acquireLockInternalStep(lock, false);
        if (!result.isAcquired()) {
            return result;
        }

        log.debug("Success acquiring lock '{}'", lock);
        return LockingResult.success();
    }

    private LockingResult acquireLockInternalStep(EngineLock lock, boolean checkOnly) {
        if (lock.getSharedLocks() != null) {
            for (Entry<String, Pair<String, String>> entry : lock.getSharedLocks().entrySet()) {
                LockingResult result =
                        insertSharedLock(buildHashMapKey(entry), entry.getValue().getSecond(), checkOnly);
                if (!result.isAcquired()) {
                    log.debug("Failed to acquire lock. Shared lock is taken for key '{}', value '{}'",
                            entry.getKey(),
                            entry.getValue().getFirst());
                    return result;
                }
            }
        }
        if (lock.getExclusiveLocks() != null) {
            for (Entry<String, Pair<String, String>> entry : lock.getExclusiveLocks().entrySet()) {
                LockingResult result =
                        insertExclusiveLock(buildHashMapKey(entry), entry.getValue().getSecond(), checkOnly);
                if (!result.isAcquired()) {
                    log.debug("Failed to acquire lock. Exclusive lock is taken for key '{}', value '{}'",
                            entry.getKey(),
                            entry.getValue().getFirst());
                    return result;
                }
            }
        }
        return LockingResult.success();
    }

    /**
     * The following method should insert an "shared" internal lock
     * @param message
     *            - error message associated with lock
     */
    private LockingResult insertSharedLock(String key, String message, boolean isCheckOnly) {
        InternalLockView lock = locks.get(key);
        if (lock != null) {
            if (!isCheckOnly) {
                lock.increaseCount();
                lock.addMessage(message);
            } else if (lock.getExclusive()) {
                return LockingResult.fail(lock.getMessages());
            }
        } else if (!isCheckOnly) {
            locks.put(key, new InternalLockView(1, message, false));
        }
        return LockingResult.success();
    }

    /**
     * The following method will add exclusive lock, the exclusive key can be
     * added only if there is not exist any shared or exclusive lock for given key
     */
    private LockingResult insertExclusiveLock(String key, String message, boolean isCheckOnly) {
        InternalLockView lock = locks.get(key);
        if (lock != null) {
            return LockingResult.fail(lock.getMessages());
        }
        if (!isCheckOnly) {
            locks.put(key, new InternalLockView(0, message, true));
        }
        return LockingResult.success();
    }

    private void releaseExclusiveLock(String key) {
        InternalLockView lock = locks.get(key);
        if (lock != null && lock.getExclusive()) {
            locks.remove(key);
            log.debug("The exclusive lock for key '{}' is released and lock is removed from map", key);
        } else if (lock == null) {
            log.warn("Trying to release exclusive lock which does not exist, lock key: '{}'", key);
        } else {
            log.warn("Trying to release exclusive lock but lock is not exclusive. lock key: '{}'", key);
        }
    }

    private void releaseSharedLock(String key, String message) {
        InternalLockView lock = locks.get(key);
        if (lock != null) {
            if (lock.getCount() > 0) {
                lock.decreaseCount();
                log.debug("The shared lock for key '{}' is released.", key);
                if (lock.getCount() == 0) {
                    locks.remove(key);
                    log.debug("The shared lock for key '{}' is removed from map", key);
                } else {
                    lock.removeMessage(message);
                }
            } else {
                log.warn("Trying to decrease a shared lock for key: '{}' , but shared index is 0", key);
            }
        } else {
            log.warn("Trying to release a shared lock for key: '{}' , but lock does not exist", key);
        }
    }

    @Override
    public LockInfo getLockInfo(String key) {
        InternalLockView internalLockView = locks.get(key);
        if (internalLockView == null) {
            return null;
        }

        Set<String> messages = internalLockView.getMessages();
        messages.remove(EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED.name());
        if (messages.isEmpty()) {
            // EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED should only be used for
            // short locks (locks for the execute phase) so we filter it and if no
            // other lock exists, the entity should be displayed as unlocked
            return null;
        }

        return new LockInfo(internalLockView.getExclusive(), messages);
    }

    @Override
    public boolean isExclusiveLockPresent(EngineLock lock) {
        return lock.getExclusiveLocks() != null &&
            lock.getExclusiveLocks().entrySet().stream()
                .anyMatch(entry -> getLockInfo(buildHashMapKey(entry)) != null);
    }

    /**
     * The following class represents different locks which are kept inside InMemoryLockManager
     */
    private static class InternalLockView {

        /** Number for shared locks **/
        private int count;
        /** Indicate if the lock is exclusive and not allowing any other exclusive/shared locks with the same key **/
        private final boolean exclusive;
        /** Contains error messages for that key **/
        private List<String> messages;

        public InternalLockView(int count, String message, boolean exclusive) {
            this.count = count;
            this.exclusive = exclusive;
            messages = new ArrayList<>();
            messages.add(message);
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

        public Set<String> getMessages() {
            return new HashSet<>(messages);
        }

        public void addMessage(String message) {
            messages.add(message);
        }

        public void removeMessage(String message) {
            if (message != null) {
                messages.remove(message);
            }
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
