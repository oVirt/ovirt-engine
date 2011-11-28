package org.ovirt.engine.core.utils.lock;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;

public class InMemoryLockManagerTest {

    private EngineLock updateLock1;
    private EngineLock updateLock2;
    private EngineLock lockLock1;
    private EngineLock lockLock2;
    private EngineLock updateAndLockLock;
    private Guid updateGuid;
    private Guid lockGuid;

    @Before
    public void setup() {
        updateGuid = Guid.NewGuid();
        lockGuid = Guid.NewGuid();
        Map<String, Guid> updateRegionsMap = new HashMap<String, Guid>();
        updateRegionsMap.put("1", updateGuid);
        updateLock1 = new EngineLock();
        updateLock1.setSharedLocks(updateRegionsMap);
        lockLock1 = new EngineLock();
        lockLock1.setExclusiveLocks(updateRegionsMap);
        Map<String, Guid> lockedRegionsMap = new HashMap<String, Guid>();
        lockedRegionsMap.put("2", lockGuid);
        lockLock2 = new EngineLock();
        lockLock2.setExclusiveLocks(lockedRegionsMap);
        updateLock2 = new EngineLock();
        updateLock2.setSharedLocks(lockedRegionsMap);
        updateAndLockLock = new EngineLock();
        updateAndLockLock.setSharedLocks(updateRegionsMap);
        updateAndLockLock.setExclusiveLocks(lockedRegionsMap);
    }

    @Test
    public void checkAcquireLockSuccess() {
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateLock1));
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock2));
        LockManagerFactory.getLockManager().releaseLock(lockLock2);
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateLock2));
        LockManagerFactory.getLockManager().releaseLock(updateLock1);
        LockManagerFactory.getLockManager().releaseLock(updateLock2);
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateAndLockLock));
        LockManagerFactory.getLockManager().releaseLock(updateAndLockLock);
    }

    @Test
    public void checkAcquireLockFailure() {
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateLock1));
        assertFalse(LockManagerFactory.getLockManager().acquireLock(lockLock1));
        LockManagerFactory.getLockManager().releaseLock(updateLock1);
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock1));
        LockManagerFactory.getLockManager().releaseLock(lockLock1);
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateAndLockLock));
        assertFalse(LockManagerFactory.getLockManager().acquireLock(lockLock1));
        assertFalse(LockManagerFactory.getLockManager().acquireLock(updateLock2));
        LockManagerFactory.getLockManager().releaseLock(updateAndLockLock);
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock1));
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateLock2));
        LockManagerFactory.getLockManager().releaseLock(lockLock1);
        LockManagerFactory.getLockManager().releaseLock(updateLock2);
    }

    @Test
    public void checkClear() {
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock1));
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock2));
        LockManagerFactory.getLockManager().clear();
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock1));
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock2));
        LockManagerFactory.getLockManager().clear();
    }
}
