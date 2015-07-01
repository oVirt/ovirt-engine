package org.ovirt.engine.core.bll;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.ForceSelectSPMParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.AsyncTaskDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;

/** A test case for the {@link ForceSelectSPMCommand} command */

@RunWith(MockitoJUnitRunner.class)
public class ForceSelectSPMCommandTest {

    private Guid vdsId = Guid.newGuid();
    private Guid storagePoolId = Guid.newGuid();

    private ForceSelectSPMCommand<ForceSelectSPMParameters> command;
    private VDS vds;
    private StoragePool storagePool;

    @Mock
    private VdsDao vdsDaoMock;

    @Mock
    private StoragePoolDao storagePoolDaoMock;

    @Mock
    private AsyncTaskDao asyncTaskDaoMock;

    @Before
    public void setup() {
        createVDSandStoragePool();
        mockCommand();
    }

    @Test
    public void testCDANonExistingVds() {
        doReturn(null).when(vdsDaoMock).get(vdsId);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure("canDoAction did not fail for non existing VDS",
                command, VdcBllMessages.VDS_NOT_EXIST);
    }

    @Test
    public void testCDAVdsNotUp() {
        vds.setStatus(VDSStatus.Down);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                ("canDoAction did not fail for a VDS with a status different from UP",
                        command, VdcBllMessages.CANNOT_FORCE_SELECT_SPM_VDS_NOT_UP);
    }

    @Test
    public void testCDAStoragePoolValid() {
        vds.setId(Guid.newGuid());
        CanDoActionTestUtils.runAndAssertCanDoActionFailure("canDoAction did not fail on mismatch Storage Pool",
                command, VdcBllMessages.CANNOT_FORCE_SELECT_SPM_VDS_NOT_IN_POOL);
    }

    @Test
    public void testCDAVdsIsSPM() {
        vds.setSpmStatus(VdsSpmStatus.SPM);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                ("canDoAction did not fail on a VDS that is already set as SPM",
                        command, VdcBllMessages.CANNOT_FORCE_SELECT_SPM_VDS_ALREADY_SPM);
    }

    @Test
    public void testCDAVdsSPMPrioritySetToNever() {
        vds.setVdsSpmPriority(BusinessEntitiesDefinitions.HOST_MIN_SPM_PRIORITY);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                ("canDoAction did not fail on a VDS that is set to never be elected as SPM",
                        command, VdcBllMessages.CANNOT_FORCE_SELECT_SPM_VDS_MARKED_AS_NEVER_SPM);
    }

    @Test
    public void testCDAStoragePoolNotUp() {
        storagePool.setStatus(StoragePoolStatus.Uninitialized);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                ("canDoAction did not fail on a Storage Pool which is not up", command,
                        VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_REPOSITORY_NOT_FOUND);
    }

    @Test
    public void testCDAStoragePoolHasTasks() {
        List<Guid> tasks = Arrays.asList(Guid.newGuid());
        doReturn(tasks).when(asyncTaskDaoMock).getAsyncTaskIdsByStoragePoolId(storagePoolId);
        CanDoActionTestUtils.runAndAssertCanDoActionFailure
                ("canDoAction did not fail on a Storage Pool with running tasks", command,
                        VdcBllMessages.CANNOT_FORCE_SELECT_SPM_STORAGE_POOL_HAS_RUNNING_TASKS);
    }

    private void createVDSandStoragePool() {
        vds = new VDS();
        vds.setId(vdsId);
        vds.setVdsName("TestVDS");
        vds.setStoragePoolId(storagePoolId);
        vds.setStatus(VDSStatus.Up);
        vds.setSpmStatus(VdsSpmStatus.None);
        vds.setVdsSpmPriority(10);

        storagePool = new StoragePool();
        storagePool.setId(storagePoolId);
        storagePool.setStatus(StoragePoolStatus.Up);
    }

    private void mockCommand() {
        ForceSelectSPMParameters params = new ForceSelectSPMParameters(vdsId);
        command = spy(new ForceSelectSPMCommand<ForceSelectSPMParameters>(params));
        doReturn(vdsDaoMock).when(command).getVdsDao();
        doReturn(storagePoolDaoMock).when(command).getStoragePoolDao();
        doReturn(asyncTaskDaoMock).when(command).getAsyncTaskDao();
        doReturn(storagePool).when(storagePoolDaoMock).getForVds(vdsId);
        doReturn(vds).when(vdsDaoMock).get(vdsId);
    }
}
