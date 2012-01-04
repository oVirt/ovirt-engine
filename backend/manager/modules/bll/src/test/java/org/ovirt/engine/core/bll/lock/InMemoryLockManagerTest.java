package org.ovirt.engine.core.bll.lock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EJBUtilsStrategy;
import org.ovirt.engine.core.utils.ejb.EjbUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.lock.LockManagerFactory;

public class InMemoryLockManagerTest {

    private EngineLock updateLock1;
    private EngineLock updateLock2;
    private EngineLock lockLock1;
    private EngineLock lockLock2;
    private EngineLock updateAndLockLock;
    private Guid updateGuid;
    private Guid lockGuid;
    private InMemoryLockManager lockMager;

    @Before
    public void setup() {
        mockLockManager();
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

    private void mockLockManager() {
        lockMager = new InMemoryLockManager();
        EJBUtilsStrategy ejbStrategy = mock(EJBUtilsStrategy.class);
        when(ejbStrategy.<LockManager> findBean(BeanType.LOCK_MANAGER, BeanProxyType.LOCAL))
                .thenReturn(lockMager);
        EjbUtils.setStrategy(ejbStrategy);
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
        assertTrue(LockManagerFactory.getLockManager().acquireLock(updateLock1));
        assertTrue(lockMager.releaseLock("1" + updateGuid));
        assertTrue(lockMager.showAllLocks().isEmpty());
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

    @Test
    public void checkShowLocks() {
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock1));
        assertTrue(LockManagerFactory.getLockManager().acquireLock(lockLock2));
        assertTrue(lockMager.showAllLocks().size() == 2);
        LockManagerFactory.getLockManager().clear();
        assertTrue(lockMager.showAllLocks().isEmpty());
    }
}
