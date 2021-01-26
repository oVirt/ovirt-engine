package org.ovirt.engine.core.bll.storage.pool;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSNetworkException;

public class CleanFinishedTasksCommand<T extends StoragePoolParametersBase> extends CommandBase<T> {
    @Inject
    private CommandCoordinator commandCoordinator;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    private StoragePoolDao storagePoolDao;

    private Map<Guid, Boolean> taskCleanupStatuses = new HashMap<>();

    public CleanFinishedTasksCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public void init() {
        setStoragePoolId(getParameters().getStoragePoolId());
    }

    @Override
    protected boolean validate() {
        StoragePool storagePool = storagePoolDao.get(getStoragePoolId());
        StoragePoolValidator spValidator = new StoragePoolValidator(storagePool);
        return validate(spValidator.existsAndUp());
    }

    @Override
    protected void executeCommand() {
        Guid storagePoolId = getStoragePoolId();
        List<Guid> tasksIds;

        try {
            tasksIds = commandCoordinatorUtil.getTasksIdsByStatus(storagePoolId, AsyncTaskStatusEnum.finished);
        } catch (RuntimeException e) {
            log.error("Get SPM task statuses: calling VDSCommand '{}' with storagePoolId '{}' threw an exception: {}",
                    VDSCommandType.SPMGetAllTasksStatuses, storagePoolId, e.getMessage());
            handleError(e.getMessage());
            return;
        }

        if (tasksIds == null) {
            String errorMsg = String.format("Failed to retrieve finished tasks IDs for storage pool '%s'", storagePoolId);
            log.error(errorMsg);
            handleError(errorMsg);
            return;
        }

        // At the beginning all tasks are marked as "cleanup not done".
        tasksIds.forEach(vdsmTaskId -> taskCleanupStatuses.put(vdsmTaskId, Boolean.FALSE));

        for (Guid vdsmTaskId : tasksIds) {
            log.info("Attempting to clean up a finished task '{}'", vdsmTaskId);

            VDSReturnValue vdsReturnValue;
            try {
                vdsReturnValue = commandCoordinator.clearTask(storagePoolId, vdsmTaskId);
                if (commandCoordinator.removeByVdsmTaskId(vdsmTaskId) != 0) {
                    log.info("Task '{}' removed from the database", vdsmTaskId);
                }
            } catch (VDSNetworkException e) {
                log.error("Failed to clean up the finished task '{}' due to network issue: {}",
                        vdsmTaskId, e.getMessage());
                handleError(e.getMessage());
                return;
            } catch (RuntimeException e) {
                log.error("Failed to clean up the finished task '{}': {}, trying the next task",
                        vdsmTaskId, e.getMessage());
                handleError(e.getMessage());
                continue;
            }

            if (vdsReturnValue.getSucceeded()) {
                // Mark the task as "cleanup successfully finished".
                taskCleanupStatuses.put(vdsmTaskId, Boolean.TRUE);
            } else {
                VDSError vdsError = vdsReturnValue.getVdsError();
                log.error("Failed to clean up the finished task '{}', exception: {}, VDS error: {}",
                        vdsmTaskId, vdsReturnValue.getExceptionString(), vdsError);
                handleVdsError(vdsError);
            }
        }

        setSucceeded(taskCleanupStatuses.values().stream().allMatch(Boolean::valueOf));
    }

    private void handleError(String errorMsg) {
        EngineFault fault = getReturnValue().getFault();
        fault.setMessage(errorMsg);

        getReturnValue().getExecuteFailedMessages().add(errorMsg);
    }

    private void handleVdsError(VDSError vdsError) {
        EngineFault fault = getReturnValue().getFault();
        fault.setError(vdsError.getCode());
        fault.setMessage(vdsError.getMessage());

        getReturnValue().getExecuteFailedMessages().add(vdsError.getCode().toString());
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getStoragePoolId(),
                VdcObjectType.StoragePool, getActionType().getActionGroup()));
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__CLEAR);
        addValidationMessage(EngineMessage.VAR__TYPE__FINISHED_TASKS);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("DataCenterName", getStoragePool().getName());
        if (getSucceeded()) {
            return AuditLogType.VDS_ALERT_CLEAN_SPM_FINISHED_TASKS_SUCCESS;
        }

        if (taskCleanupStatuses.values().stream().noneMatch(Boolean::valueOf)) {
            // No task(s) that were indicated as cleaned successfully --> full failure.
            return AuditLogType.VDS_ALERT_CLEAN_SPM_FINISHED_TASKS_FAILURE;
        }

        // Some task(s) were indicated as cleaned successfully --> partial failure. Find the failed tasks:
        List<Guid> failedToCleanupTaskIds = taskCleanupStatuses.entrySet()
                .stream()
                .filter(Predicate.not(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        addCustomValue("TaskGuids", StringUtils.join(failedToCleanupTaskIds, ", "));
        return AuditLogType.VDS_ALERT_CLEAN_SPM_FINISHED_TASKS_FAILURE_PARTIAL;
    }
}
