package org.ovirt.engine.core.bll.storage.pool;

import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.ForceSelectSPMParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.AsyncTaskDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VdsDao;

/** A test case for the {@link ForceSelectSPMCommand} command */
@MockitoSettings(strictness = Strictness.LENIENT)
public class ForceSelectSPMCommandTest extends BaseCommandTest {

    private Guid vdsId = Guid.newGuid();
    private Guid storagePoolId = Guid.newGuid();

    @InjectMocks
    private ForceSelectSPMCommand<ForceSelectSPMParameters> command =
            new ForceSelectSPMCommand<>(new ForceSelectSPMParameters(vdsId), null);

    private VDS vds;
    private StoragePool storagePool;

    @Mock
    private VdsDao vdsDaoMock;

    @Mock
    private StoragePoolDao storagePoolDaoMock;

    @Mock
    private AsyncTaskDao asyncTaskDaoMock;

    @BeforeEach
    public void setup() {
        createVDSandStoragePool();
        mockCommand();
    }

    @Test
    public void testCDANonExistingVds() {
        doReturn(null).when(vdsDaoMock).get(vdsId);
        ValidateTestUtils.runAndAssertValidateFailure("validate did not fail for non existing VDS",
                command, EngineMessage.VDS_NOT_EXIST);
    }

    @Test
    public void testCDAVDSDoesNotSupportVirtServices() {
        vds.setId(Guid.newGuid());
        vds.setClusterSupportsVirtService(false);
        ValidateTestUtils.runAndAssertValidateFailure("validate did not fail on host without virt services",
                command, EngineMessage.CANNOT_FORCE_SELECT_SPM_HOST_DOES_NOT_SUPPORT_VIRT_SERVICES);
    }

    @Test
    public void testCDAVdsNotUp() {
        vds.setStatus(VDSStatus.Down);
        ValidateTestUtils.runAndAssertValidateFailure
                ("validate did not fail for a VDS with a status different from UP",
                        command, EngineMessage.CANNOT_FORCE_SELECT_SPM_VDS_NOT_UP);
    }

    @Test
    public void testCDAStoragePoolValid() {
        vds.setId(Guid.newGuid());
        ValidateTestUtils.runAndAssertValidateFailure("validate did not fail on mismatch Storage Pool",
                command, EngineMessage.CANNOT_FORCE_SELECT_SPM_VDS_NOT_IN_POOL);
    }

    @Test
    public void testCDAVdsIsSPM() {
        vds.setSpmStatus(VdsSpmStatus.SPM);
        ValidateTestUtils.runAndAssertValidateFailure
                ("validate did not fail on a VDS that is already set as SPM",
                        command, EngineMessage.CANNOT_FORCE_SELECT_SPM_VDS_ALREADY_SPM);
    }

    @Test
    public void testCDAVdsSPMPrioritySetToNever() {
        vds.setVdsSpmPriority(BusinessEntitiesDefinitions.HOST_MIN_SPM_PRIORITY);
        ValidateTestUtils.runAndAssertValidateFailure
                ("validate did not fail on a VDS that is set to never be elected as SPM",
                        command, EngineMessage.CANNOT_FORCE_SELECT_SPM_VDS_MARKED_AS_NEVER_SPM);
    }

    @Test
    public void testCDAStoragePoolNotUp() {
        storagePool.setStatus(StoragePoolStatus.Uninitialized);
        ValidateTestUtils.runAndAssertValidateFailure
                ("validate did not fail on a Storage Pool which is not up", command,
                        EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
    }

    @Test
    public void testCDAStoragePoolHasTasks() {
        List<Guid> tasks = Collections.singletonList(Guid.newGuid());
        doReturn(tasks).when(asyncTaskDaoMock).getAsyncTaskIdsByStoragePoolId(storagePoolId);
        ValidateTestUtils.runAndAssertValidateFailure
                ("validate did not fail on a Storage Pool with running tasks", command,
                        EngineMessage.CANNOT_FORCE_SELECT_SPM_STORAGE_POOL_HAS_RUNNING_TASKS);
    }

    private void createVDSandStoragePool() {
        vds = new VDS();
        vds.setId(vdsId);
        vds.setVdsName("TestVDS");
        vds.setStoragePoolId(storagePoolId);
        vds.setStatus(VDSStatus.Up);
        vds.setClusterSupportsVirtService(true);
        vds.setSpmStatus(VdsSpmStatus.None);
        vds.setVdsSpmPriority(10);

        storagePool = new StoragePool();
        storagePool.setId(storagePoolId);
        storagePool.setStatus(StoragePoolStatus.Up);
    }

    private void mockCommand() {
        doReturn(storagePool).when(storagePoolDaoMock).getForVds(vdsId);
        doReturn(vds).when(vdsDaoMock).get(vdsId);
    }
}
