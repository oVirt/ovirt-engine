package org.ovirt.engine.core.bll.storage.pool;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
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
            return;
        }

        if (tasksIds == null) {
            log.error("Failed to receive finished tasks IDs for storage pool '{}'", storagePoolId);
            return;
        }

        boolean allTasksCleared = true;
        for (Guid vdsmTaskID : tasksIds) {
            log.info("Attempting to clear the finished task '{}'", vdsmTaskID);

            VDSReturnValue vdsReturnValue;
            try {
                vdsReturnValue = commandCoordinator.clearTask(storagePoolId, vdsmTaskID);
                if (commandCoordinator.removeByVdsmTaskId(vdsmTaskID) != 0) {
                    log.info("Task '{}' removed from the database", vdsmTaskID);
                }
            } catch (VDSNetworkException e) {
                log.error("Failed to clear task '{}' due to network issue: {}, terminate", vdsmTaskID, e.getMessage());
                return;
            } catch (RuntimeException e) {
                log.error("Failed to clear task '{}': {}, continue to the next task", vdsmTaskID, e.getMessage());
                allTasksCleared = false;
                continue;
            }

            if (!vdsReturnValue.getSucceeded()) {
                log.error("Failed to clear task '{}', exception: {}, VDS error: {}",
                        vdsmTaskID, vdsReturnValue.getExceptionString(), vdsReturnValue.getVdsError());
                allTasksCleared = false;
            }
        }

        setSucceeded(allTasksCleared);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getStoragePoolId(),
                VdcObjectType.StoragePool, getActionType().getActionGroup()));
    }
}
