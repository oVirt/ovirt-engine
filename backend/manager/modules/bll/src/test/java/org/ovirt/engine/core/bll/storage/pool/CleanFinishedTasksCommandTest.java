package org.ovirt.engine.core.bll.storage.pool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;


@MockitoSettings(strictness = Strictness.LENIENT)
public class CleanFinishedTasksCommandTest extends BaseCommandTest {
    private static final String AUDIT_LOG_DATA_CENTER_NAME = "datacentername";
    private static final String AUDIT_LOG_TASK_GUIDS = "taskguids";

    @Mock
    private StoragePoolDao storagePoolDao;
    @Mock
    private CommandCoordinator commandCoordinator;
    @Mock
    private CommandCoordinatorUtil commandCoordinatorUtil;

    private Guid storagePoolId = Guid.newGuid();
    private Guid vdsmTaskId1 = Guid.newGuid();
    private Guid vdsmTaskId2 = Guid.newGuid();
    private Guid vdsmTaskId3 = Guid.newGuid();
    private StoragePool storagePool;

    @InjectMocks
    @Spy
    private CleanFinishedTasksCommand<StoragePoolParametersBase> command = new CleanFinishedTasksCommand<>(
            new StoragePoolParametersBase(storagePoolId),
            null);

    @BeforeEach
    public void setup() {
        initializeStoragePool();
        mockCommand();
    }

    @Test
    public void testValidateSuccess() {
        ValidateTestUtils.runAndAssertValidateSuccess(command);
    }

    @Test
    public void testValidateFailureNullStoragePoolID() {
        when(command.getStoragePoolId()).thenReturn(null);
        ValidateTestUtils.runAndAssertValidateFailure
                ("Validate did not fail on a null storage pool", command,
                        EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
    }

    @Test
    public void testValidateFailureEmptyStoragePoolID() {
        when(command.getStoragePoolId()).thenReturn(Guid.Empty);
        ValidateTestUtils.runAndAssertValidateFailure
                ("Validate did not fail on an empty storage pool ID", command,
                        EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
    }

    @Test
    public void testValidateFailureRandomStoragePoolID() {
        when(command.getStoragePoolId()).thenReturn(Guid.newGuid());
        ValidateTestUtils.runAndAssertValidateFailure
                ("Validate did not fail on randomly generated storage pool ID", command,
                        EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NOT_EXIST);
    }

    @Test
    public void testValidateFailureStoragePoolNotInUpStatus() {
        storagePool.setStatus(StoragePoolStatus.Maintenance);
        ValidateTestUtils.runAndAssertValidateFailure
                ("Validate did not fail on storage pool that is not in the UP status", command,
                        EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
    }

    @Test
    public void testExecuteCommandSuccessEmptyFinishedTasks() {
        when(commandCoordinatorUtil.getTasksIdsByStatus(storagePoolId, AsyncTaskStatusEnum.finished))
                .thenReturn(Collections.emptyList());

        command.executeCommand();

        assertTrue(command.getReturnValue().getSucceeded());
        assertEquals(AuditLogType.VDS_ALERT_CLEAN_SPM_FINISHED_TASKS_SUCCESS, command.getAuditLogTypeValue());
        assertEquals(1, command.getCustomValues().size());
        assertEquals(storagePool.getName(), command.getCustomValues().get(AUDIT_LOG_DATA_CENTER_NAME));

        List<String> executeFailedMessages = command.getReturnValue().getExecuteFailedMessages();
        assertNotNull(executeFailedMessages);
        assertTrue(executeFailedMessages.isEmpty());
    }

    @Test
    public void testExecuteCommandSuccessMultipleFinishedTasks() {
        command.executeCommand();

        assertTrue(command.getReturnValue().getSucceeded());
        assertEquals(AuditLogType.VDS_ALERT_CLEAN_SPM_FINISHED_TASKS_SUCCESS, command.getAuditLogTypeValue());
        assertEquals(1, command.getCustomValues().size());
        assertEquals(storagePool.getName(), command.getCustomValues().get(AUDIT_LOG_DATA_CENTER_NAME));

        List<String> executeFailedMessages = command.getReturnValue().getExecuteFailedMessages();
        assertNotNull(executeFailedMessages);
        assertTrue(executeFailedMessages.isEmpty());
    }

    @Test
    public void testExecuteCommandFailureGetTasksIdsThrowsRuntimeException() {
        String expectedErrorMessage = "Test error message (RuntimeException)";
        when(commandCoordinatorUtil.getTasksIdsByStatus(storagePoolId, AsyncTaskStatusEnum.finished))
                .thenThrow(new RuntimeException(expectedErrorMessage));

        command.executeCommand();

        assertFalse(command.getReturnValue().getSucceeded());
        assertEquals(AuditLogType.VDS_ALERT_CLEAN_SPM_FINISHED_TASKS_FAILURE, command.getAuditLogTypeValue());
        assertEquals(1, command.getCustomValues().size());
        assertEquals(storagePool.getName(), command.getCustomValues().get(AUDIT_LOG_DATA_CENTER_NAME));

        EngineFault fault = command.getReturnValue().getFault();
        assertEquals(EngineError.ENGINE, fault.getError());
        assertEquals(expectedErrorMessage, fault.getMessage());

        List<String> executeFailedMessages = command.getReturnValue().getExecuteFailedMessages();
        assertNotNull(executeFailedMessages);
        assertEquals(1, executeFailedMessages.size());
        assertEquals(expectedErrorMessage, executeFailedMessages.get(0));
    }

    @Test
    public void testExecuteCommandFailureGetTasksIdsReturnsNull() {
        when(commandCoordinatorUtil.getTasksIdsByStatus(storagePoolId, AsyncTaskStatusEnum.finished)).thenReturn(null);

        command.executeCommand();

        assertFalse(command.getReturnValue().getSucceeded());
        assertEquals(AuditLogType.VDS_ALERT_CLEAN_SPM_FINISHED_TASKS_FAILURE, command.getAuditLogTypeValue());
        assertEquals(1, command.getCustomValues().size());
        assertEquals(storagePool.getName(), command.getCustomValues().get(AUDIT_LOG_DATA_CENTER_NAME));


        String expectedErrorMessage = String.format("Failed to retrieve finished tasks IDs for storage pool '%s'",
                storagePoolId);
        EngineFault fault = command.getReturnValue().getFault();
        assertEquals(EngineError.ENGINE, fault.getError());
        assertEquals(expectedErrorMessage, fault.getMessage());

        List<String> executeFailedMessages = command.getReturnValue().getExecuteFailedMessages();
        assertNotNull(executeFailedMessages);
        assertEquals(1, executeFailedMessages.size());
        assertEquals(expectedErrorMessage, executeFailedMessages.get(0));
    }

    @Test
    public void testExecuteCommandFailureClearTaskReturnsVDSReturnValueIndicatingErrors() {
        String expectedErrorMessage = "Broken pipe";
        EngineError expectedError = EngineError.VDS_NETWORK_ERROR;
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(false);
        vdsReturnValue.setVdsError(new VDSError(expectedError, expectedErrorMessage));

        when(commandCoordinator.clearTask(storagePoolId, vdsmTaskId1)).thenReturn(vdsReturnValue);

        command.executeCommand();

        assertFalse(command.getReturnValue().getSucceeded());
        assertEquals(AuditLogType.VDS_ALERT_CLEAN_SPM_FINISHED_TASKS_FAILURE_PARTIAL, command.getAuditLogTypeValue());
        assertEquals(2, command.getCustomValues().size());
        assertEquals(storagePool.getName(), command.getCustomValues().get(AUDIT_LOG_DATA_CENTER_NAME));
        assertEquals(vdsmTaskId1.toString(), command.getCustomValues().get(AUDIT_LOG_TASK_GUIDS));

        EngineFault fault = command.getReturnValue().getFault();
        assertEquals(expectedError, fault.getError());
        assertEquals(expectedErrorMessage, fault.getMessage());

        List<String> executeFailedMessages = command.getReturnValue().getExecuteFailedMessages();
        assertNotNull(executeFailedMessages);
        assertEquals(1, executeFailedMessages.size());
        assertEquals(expectedError.toString(), executeFailedMessages.get(0));
    }

    @Test
    public void testExecuteCommandFailureClearTaskThrowsRuntimeException() {
        String expectedErrorMessage = "Test error message (RuntimeException)";
        when(commandCoordinator.clearTask(storagePoolId, vdsmTaskId2))
                .thenThrow(new RuntimeException(expectedErrorMessage));

        command.executeCommand();

        assertFalse(command.getReturnValue().getSucceeded());
        assertEquals(AuditLogType.VDS_ALERT_CLEAN_SPM_FINISHED_TASKS_FAILURE_PARTIAL, command.getAuditLogTypeValue());
        assertEquals(2, command.getCustomValues().size());
        assertEquals(storagePool.getName(), command.getCustomValues().get(AUDIT_LOG_DATA_CENTER_NAME));
        assertEquals(vdsmTaskId2.toString(), command.getCustomValues().get(AUDIT_LOG_TASK_GUIDS));

        EngineFault fault = command.getReturnValue().getFault();
        assertEquals(EngineError.ENGINE, fault.getError());
        assertEquals(expectedErrorMessage, fault.getMessage());

        List<String> executeFailedMessages = command.getReturnValue().getExecuteFailedMessages();
        assertNotNull(executeFailedMessages);
        assertEquals(1, executeFailedMessages.size());
        assertEquals(expectedErrorMessage, executeFailedMessages.get(0));
    }

    @Test
    public void testExecuteCommandFailureClearTaskThrowsVDSNetworkException() {
        String errorMessage = "Test error message (VDSNetworkException)";
        when(commandCoordinator.clearTask(storagePoolId, vdsmTaskId1))
                .thenThrow(new VDSNetworkException(errorMessage));

        command.executeCommand();

        assertFalse(command.getReturnValue().getSucceeded());
        // Full failure, since network error stops the cleanup, and we fail on the most 1st task...
        assertEquals(AuditLogType.VDS_ALERT_CLEAN_SPM_FINISHED_TASKS_FAILURE, command.getAuditLogTypeValue());
        assertEquals(1, command.getCustomValues().size());
        assertEquals(storagePool.getName(), command.getCustomValues().get(AUDIT_LOG_DATA_CENTER_NAME));

        String expectedErrorMessage = "VDSGenericException: VDSNetworkException: " + errorMessage;
        EngineFault fault = command.getReturnValue().getFault();
        assertEquals(EngineError.ENGINE, fault.getError());
        assertEquals(expectedErrorMessage, fault.getMessage());

        List<String> executeFailedMessages = command.getReturnValue().getExecuteFailedMessages();
        assertNotNull(executeFailedMessages);
        assertEquals(1, executeFailedMessages.size());
        assertEquals(expectedErrorMessage, executeFailedMessages.get(0));
    }


    private void initializeStoragePool() {
        storagePool = new StoragePool();
        storagePool.setId(storagePoolId);
        storagePool.setCompatibilityVersion(Version.getLast());
        storagePool.setName("DC" + storagePool.getCompatibilityVersion());
        storagePool.setStatus(StoragePoolStatus.Up);

        when(command.getStoragePool()).thenReturn(storagePool);
        when(command.getStoragePoolId()).thenReturn(storagePoolId);
        when(storagePoolDao.get(storagePoolId)).thenReturn(storagePool);
    }

    private void mockCommand() {
        List<Guid> tasksIds = Arrays.asList(vdsmTaskId1, vdsmTaskId2, vdsmTaskId3);
        VDSReturnValue vdsReturnValue = new VDSReturnValue();
        vdsReturnValue.setSucceeded(true);

        when(commandCoordinatorUtil.getTasksIdsByStatus(storagePoolId, AsyncTaskStatusEnum.finished)).thenReturn(tasksIds);

        for (Guid vdsmTaskId : tasksIds) {
            when(commandCoordinator.clearTask(storagePoolId, vdsmTaskId)).thenReturn(vdsReturnValue);
            when(commandCoordinator.removeByVdsmTaskId(vdsmTaskId)).thenReturn(0);
        }
    }

}
