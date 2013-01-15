package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.lock.InMemoryLockManager;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBaseMockUtils;
import org.ovirt.engine.core.dao.VmDAO;
import org.ovirt.engine.core.utils.MockEJBStrategyRule;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;

/** A test case for {@link RemoveDiskCommandTest} */
@RunWith(MockitoJUnitRunner.class)
public class RemoveDiskCommandTest {

    private LockManager lockManager = new InMemoryLockManager();

    @Rule
    public MockEJBStrategyRule ejbRule = new MockEJBStrategyRule(BeanType.LOCK_MANAGER, lockManager);

    @Mock
    private VmDAO vmDao;

    private RemoveDiskCommand<RemoveDiskParameters> cmd;
    private DiskImage disk;
    private VM vm;

    @Before
    public void setUp() {
        Guid diskId = Guid.NewGuid();

        disk = new DiskImage();
        disk.setId(diskId);
        disk.setVmEntityType(VmEntityType.VM);

        Guid vmId = Guid.NewGuid();
        vm = new VM();
        vm.setId(vmId);

        when(vmDao.getVmsListForDisk(diskId)).thenReturn(Collections.singletonList(vm));

        RemoveDiskParameters params = new RemoveDiskParameters(diskId);

        cmd = spy(new RemoveDiskCommand<RemoveDiskParameters>(params));
        doReturn(disk).when(cmd).getDisk();
        AuditLogableBaseMockUtils.mockVmDao(cmd, vmDao);
    }

    /* Tests for canDoAction() flow */

    @Test
    public void testCanDoActionFlowImageDoesNotExist() {
        doReturn(null).when(cmd).getDisk();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
    }

    @Test
    public void testCanDoActionFlowUnableToLock() {
        EngineLock lock =
                new EngineLock
                (Collections.singletonMap(vm.getId().toString(), LockingGroup.VM.name()),
                        Collections.<String, String> emptyMap());
        lockManager.acquireLock(lock);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd,
                VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED);
    }
}
