package org.ovirt.engine.core.bll.lock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;

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
    private InMemoryLockManager lockMager = new InMemoryLockManager();

    @Rule
    public MockEJBStrategyRule mockEjbRule = new MockEJBStrategyRule(BeanType.LOCK_MANAGER, lockMager);

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
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateLock1).getFirst());
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock2).getFirst());
        LockManagerFactory.getLockManager().releaseLock(lockLock2);
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateLock2).getFirst());
        LockManagerFactory.getLockManager().releaseLock(updateLock1);
        LockManagerFactory.getLockManager().releaseLock(updateLock2);
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateAndLockLock).getFirst());
        LockManagerFactory.getLockManager().releaseLock(updateAndLockLock);
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateLock1).getFirst());
        assertTrue(lockMager.releaseLock(updateGuid + "1"));
        assertTrue(lockMager.showAllLocks().isEmpty());
    }

    @Test
    public void checkAcquireLockFailure() {
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateLock1).getFirst());
        assertFalse(LockManagerFactory.getLockManager().acquireLock(lockLock1).getFirst());
        LockManagerFactory.getLockManager().releaseLock(updateLock1);
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock1).getFirst());
        LockManagerFactory.getLockManager().releaseLock(lockLock1);
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateAndLockLock).getFirst());
        Pair<Boolean, Set<String>> lockResult = LockManagerFactory.getLockManager().acquireLock(lockLock1);
        assertFalse(lockResult.getFirst());
        assertTrue(lockResult.getSecond().contains(ERROR1));
        assertEquals(lockResult.getSecond().size(), 1);
        lockResult = LockManagerFactory.getLockManager().acquireLock(updateLock2);
        assertFalse(lockResult.getFirst());
        assertTrue(lockResult.getSecond().contains(ERROR2));
        assertEquals(lockResult.getSecond().size(), 1);
        LockManagerFactory.getLockManager().releaseLock(updateAndLockLock);
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock1).getFirst());
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateLock2).getFirst());
        LockManagerFactory.getLockManager().releaseLock(lockLock1);
        LockManagerFactory.getLockManager().releaseLock(updateLock2);
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateAndLockLock).getFirst());
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateLock3).getFirst());
        lockResult = LockManagerFactory.getLockManager().acquireLock(failLockLock);
        assertFalse(lockResult.getFirst());
        assertTrue(lockResult.getSecond().contains(ERROR1));
        assertTrue(lockResult.getSecond().contains(ERROR3));
        assertEquals(lockResult.getSecond().size(), 2);
        LockManagerFactory.getLockManager().releaseLock(updateAndLockLock);
        LockManagerFactory.getLockManager().releaseLock(updateLock3);
    }

    @Test
    public void checkClear() {
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock1).getFirst());
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock2).getFirst());
        LockManagerFactory.getLockManager().clear();
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock1).getFirst());
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock2).getFirst());
        LockManagerFactory.getLockManager().clear();
    }

    @Test
    public void checkShowLocks() {
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock1).getFirst());
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock2).getFirst());
        assertTrue(lockMager.showAllLocks().size() == 2);
        LockManagerFactory.getLockManager().clear();
        assertTrue(lockMager.showAllLocks().isEmpty());
    }
}
