package org.ovirt.engine.core.bll.lock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.lock.EngineLock;

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

    @Before
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
        assertTrue(lockManager.acquireLock(updateLock1).getFirst());
        assertTrue(lockManager.acquireLock(lockLock2).getFirst());
        lockManager.releaseLock(lockLock2);
        assertTrue(lockManager.acquireLock(updateLock2).getFirst());
        lockManager.releaseLock(updateLock1);
        lockManager.releaseLock(updateLock2);
        assertTrue(lockManager.acquireLock(updateAndLockLock).getFirst());
        lockManager.releaseLock(updateAndLockLock);
        assertTrue(lockManager.acquireLock(updateLock1).getFirst());
        assertTrue(lockManager.releaseLock(updateGuid + "1"));
        assertTrue(lockManager.showAllLocks().isEmpty());
    }

    @Test
    public void checkAcquireLockFailure() {
        assertTrue(lockManager.acquireLock(updateLock1).getFirst());
        assertFalse(lockManager.acquireLock(lockLock1).getFirst());
        lockManager.releaseLock(updateLock1);
        assertTrue(lockManager.acquireLock(lockLock1).getFirst());
        lockManager.releaseLock(lockLock1);
        assertTrue(lockManager.acquireLock(updateAndLockLock).getFirst());
        Pair<Boolean, Set<String>> lockResult = lockManager.acquireLock(lockLock1);
        assertFalse(lockResult.getFirst());
        assertTrue(lockResult.getSecond().contains(ERROR1));
        assertEquals(1, lockResult.getSecond().size());
        lockResult = lockManager.acquireLock(updateLock2);
        assertFalse(lockResult.getFirst());
        assertTrue(lockResult.getSecond().contains(ERROR2));
        assertEquals(1, lockResult.getSecond().size());
        lockManager.releaseLock(updateAndLockLock);
        assertTrue(lockManager.acquireLock(lockLock1).getFirst());
        assertTrue(lockManager.acquireLock(updateLock2).getFirst());
        lockManager.releaseLock(lockLock1);
        lockManager.releaseLock(updateLock2);
        assertTrue(lockManager.acquireLock(updateAndLockLock).getFirst());
        assertTrue(lockManager.acquireLock(updateLock3).getFirst());
        lockResult = lockManager.acquireLock(failLockLock);
        assertFalse(lockResult.getFirst());
        assertTrue(lockResult.getSecond().contains(ERROR1));
        assertTrue(lockResult.getSecond().contains(ERROR3));
        assertEquals(2, lockResult.getSecond().size());
        lockManager.releaseLock(updateAndLockLock);
        lockManager.releaseLock(updateLock3);
    }

    @Test
    public void checkClear() {
        assertTrue(lockManager.acquireLock(lockLock1).getFirst());
        assertTrue(lockManager.acquireLock(lockLock2).getFirst());
        lockManager.clear();
        assertTrue(lockManager.acquireLock(lockLock1).getFirst());
        assertTrue(lockManager.acquireLock(lockLock2).getFirst());
        lockManager.clear();
    }

    @Test
    public void checkShowLocks() {
        assertTrue(lockManager.acquireLock(lockLock1).getFirst());
        assertTrue(lockManager.acquireLock(lockLock2).getFirst());
        assertEquals(2, lockManager.showAllLocks().size());
        lockManager.clear();
        assertTrue(lockManager.showAllLocks().isEmpty());
    }
}
