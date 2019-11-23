package org.ovirt.engine.core.bll.lock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockingResult;

public class InMemoryLockManagerTest {

    private static final String ERROR1 = "Error1";
    private static final String ERROR2 = "Error2";
    private static final String ERROR3 = "Error3";
    private EngineLock updateLock1;
    private EngineLock updateLock2;
    private EngineLock updateLock3;
    private EngineLock lockLock1;
    private EngineLock lockLock2;
    private EngineLock updateAndLockLock;
    private EngineLock failLockLock;
    private String updateGuid;
    private String lockGuid;
    private InMemoryLockManager lockManager = new InMemoryLockManager();

    @BeforeEach
    public void setup() {
        updateGuid = Guid.newGuid().toString();
        lockGuid = Guid.newGuid().toString();
        Map<String, Pair<String, String>> updateRegionsMap = new HashMap<>();
        updateRegionsMap.put(updateGuid, new Pair<>("1", ERROR1));
        updateLock1 = new EngineLock();
        updateLock1.setSharedLocks(updateRegionsMap);
        lockLock1 = new EngineLock();
        lockLock1.setExclusiveLocks(updateRegionsMap);
        Map<String, Pair<String, String>> lockedRegionsMap = new HashMap<>();
        lockedRegionsMap.put(lockGuid, new Pair<>("2", ERROR2));
        lockLock2 = new EngineLock();
        lockLock2.setExclusiveLocks(lockedRegionsMap);
        updateLock2 = new EngineLock();
        updateLock2.setSharedLocks(lockedRegionsMap);
        updateAndLockLock = new EngineLock();
        updateAndLockLock.setSharedLocks(updateRegionsMap);
        updateAndLockLock.setExclusiveLocks(lockedRegionsMap);
        failLockLock = new EngineLock();
        failLockLock.setExclusiveLocks(updateRegionsMap);
        Map<String, Pair<String, String>> updateRegionsMap2 = new HashMap<>();
        updateRegionsMap2.put(updateGuid, new Pair<>("1", ERROR3));
        updateLock3 = new EngineLock();
        updateLock3.setSharedLocks(updateRegionsMap2);
    }

    @Test
    public void checkAcquireLockSuccess() {
        assertTrue(lockManager.acquireLock(updateLock1).isAcquired());
        assertTrue(lockManager.acquireLock(lockLock2).isAcquired());
        lockManager.releaseLock(lockLock2);
        assertTrue(lockManager.acquireLock(updateLock2).isAcquired());
        lockManager.releaseLock(updateLock1);
        lockManager.releaseLock(updateLock2);
        assertTrue(lockManager.acquireLock(updateAndLockLock).isAcquired());
        lockManager.releaseLock(updateAndLockLock);
        assertTrue(lockManager.acquireLock(updateLock1).isAcquired());
        assertTrue(lockManager.releaseLock(updateGuid + "1"));
        assertTrue(lockManager.showAllLocks().isEmpty());
    }

    @Test
    public void checkAcquireLockFailure() {
        assertTrue(lockManager.acquireLock(updateLock1).isAcquired());
        assertFalse(lockManager.acquireLock(lockLock1).isAcquired());
        lockManager.releaseLock(updateLock1);
        assertTrue(lockManager.acquireLock(lockLock1).isAcquired());
        lockManager.releaseLock(lockLock1);
        assertTrue(lockManager.acquireLock(updateAndLockLock).isAcquired());
        LockingResult lockResult = lockManager.acquireLock(lockLock1);
        assertFalse(lockResult.isAcquired());
        assertTrue(lockResult.getMessages().contains(ERROR1));
        assertEquals(1, lockResult.getMessages().size());
        lockResult = lockManager.acquireLock(updateLock2);
        assertFalse(lockResult.isAcquired());
        assertTrue(lockResult.getMessages().contains(ERROR2));
        assertEquals(1, lockResult.getMessages().size());
        lockManager.releaseLock(updateAndLockLock);
        assertTrue(lockManager.acquireLock(lockLock1).isAcquired());
        assertTrue(lockManager.acquireLock(updateLock2).isAcquired());
        lockManager.releaseLock(lockLock1);
        lockManager.releaseLock(updateLock2);
        assertTrue(lockManager.acquireLock(updateAndLockLock).isAcquired());
        assertTrue(lockManager.acquireLock(updateLock3).isAcquired());
        lockResult = lockManager.acquireLock(failLockLock);
        assertFalse(lockResult.isAcquired());
        assertTrue(lockResult.getMessages().contains(ERROR1));
        assertTrue(lockResult.getMessages().contains(ERROR3));
        assertEquals(2, lockResult.getMessages().size());
        lockManager.releaseLock(updateAndLockLock);
        lockManager.releaseLock(updateLock3);
    }

    @Test
    public void checkClear() {
        assertTrue(lockManager.acquireLock(lockLock1).isAcquired());
        assertTrue(lockManager.acquireLock(lockLock2).isAcquired());
        lockManager.clear();
        assertTrue(lockManager.acquireLock(lockLock1).isAcquired());
        assertTrue(lockManager.acquireLock(lockLock2).isAcquired());
        lockManager.clear();
    }

    @Test
    public void checkShowLocks() {
        assertTrue(lockManager.acquireLock(lockLock1).isAcquired());
        assertTrue(lockManager.acquireLock(lockLock2).isAcquired());
        assertEquals(2, lockManager.showAllLocks().size());
        lockManager.clear();
        assertTrue(lockManager.showAllLocks().isEmpty());
    }

    @Test
    public void testAcquireLockWaitTwoTimeouts() {
        assertTrue(lockManager.acquireLockWait(lockLock1, 1000L).isAcquired());
        assertEquals(1, lockManager.showAllLocks().size());
        long before = System.currentTimeMillis();
        assertFalse(lockManager.acquireLockWait(failLockLock, 5500L).isAcquired());
        assertEquals(1, lockManager.showAllLocks().size());
        long after = System.currentTimeMillis();
        assertTrue(after - before >= 5000 && after - before < 7000L);
        lockManager.releaseLock(lockLock1);
        assertEquals(0, lockManager.showAllLocks().size());
        assertTrue(lockManager.acquireLockWait(failLockLock, 1000L).isAcquired());
        assertEquals(1, lockManager.showAllLocks().size());
        lockManager.releaseLock(failLockLock);
        assertEquals(0, lockManager.showAllLocks().size());
    }

    @Test
    public void testAcquireLockWaitTimeoutAfterForever() {
        lockManager.acquireLockWait(lockLock1);
        assertEquals(1, lockManager.showAllLocks().size());
        long before = System.currentTimeMillis();
        assertFalse(lockManager.acquireLockWait(failLockLock, 5500L).isAcquired());
        assertEquals(1, lockManager.showAllLocks().size());
        long after = System.currentTimeMillis();
        assertTrue(after - before >= 5000 && after - before < 7000L);
        lockManager.releaseLock(lockLock1);
        assertEquals(0, lockManager.showAllLocks().size());
        assertTrue(lockManager.acquireLockWait(failLockLock, 1000L).isAcquired());
        assertEquals(1, lockManager.showAllLocks().size());
        lockManager.releaseLock(failLockLock);
        assertEquals(0, lockManager.showAllLocks().size());
    }

    @Test
    public void testAcquireLockWaitTimeoutBeforeForever() {
        assertTrue(lockManager.acquireLockWait(lockLock1, 1000L).isAcquired());
        assertEquals(1, lockManager.showAllLocks().size());
        new Thread(() -> lockManager.acquireLockWait(failLockLock)).start();
        assertEquals(1, lockManager.showAllLocks().size());
        lockManager.releaseLock(lockLock1);
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(1, lockManager.showAllLocks().size());
        lockManager.releaseLock(failLockLock);
        assertEquals(0, lockManager.showAllLocks().size());
    }

    @Test
    public void testAcquireLockNegativeTimeout() {
        assertThrows(IllegalArgumentException.class, () -> lockManager.acquireLockWait(lockLock1, -1000L));
    }

    @Test
    public void testLockHijack() {
        new Thread(() -> {
            System.out.println("t1 start " + System.currentTimeMillis());
            assertTrue(lockManager.acquireLock(lockLock1).isAcquired());
            System.out.println("t1 end " + System.currentTimeMillis());
        }, "t1").start();

        sleep();
        new Thread(() -> {
            System.out.println("t2 start " + System.currentTimeMillis());
            assertFalse(lockManager.acquireLockWait(lockLock1, 100L).isAcquired());
            // lock "must" be released because EngineLock implements AutoCloseable
            lockManager.releaseLock(lockLock1);
            System.out.println("t2 end " + System.currentTimeMillis());
        }, "t2").start();

        sleep();
        new Thread(() -> {
            System.out.println("t3 start " + System.currentTimeMillis());
            assertTrue(lockManager.acquireLock(lockLock1).isAcquired());
            System.out.println("t3 end " + System.currentTimeMillis());
        }, "t3").start();

        sleep();
    }

    private void sleep() {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
