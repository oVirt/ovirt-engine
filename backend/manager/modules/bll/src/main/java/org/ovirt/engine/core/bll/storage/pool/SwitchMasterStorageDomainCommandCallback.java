package org.ovirt.engine.core.bll.storage.pool;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.SwitchMasterStorageDomainCommandParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Typed(SwitchMasterStorageDomainCommandCallback.class)
public class SwitchMasterStorageDomainCommandCallback implements CommandCallback {
    private static final Logger log = LoggerFactory.getLogger(SwitchMasterStorageDomainCommandCallback.class);

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        SwitchMasterStorageDomainCommand<SwitchMasterStorageDomainCommandParameters> command = getCommand(cmdId);

        Set<Guid> taskIds = new HashSet<>(command.getReturnValue().getVdsmTaskIdList());
        Map<Guid, AsyncTaskStatus> idToTaskStatusMap = commandCoordinatorUtil.pollTasks(taskIds);
        for (Map.Entry<Guid, AsyncTaskStatus> idToTaskStatus : idToTaskStatusMap.entrySet()) {
            Guid id = idToTaskStatus.getKey();
            AsyncTaskStatus status = idToTaskStatus.getValue();
            if (status.getTaskIsRunning()) {
                log.info("Waiting for command {} to complete the task (taskId = {})", command.getActionType(), id);
                return;
            }
        }
        // all tasks done
        List<Guid> failedTasks = new ArrayList<>();
        idToTaskStatusMap.forEach((id, status) -> {
            if (!status.getTaskEndedSuccessfully()) {
                failedTasks.add(id);
            }
        });
        if (!failedTasks.isEmpty() || command.getCommandStatus().equals(CommandStatus.EXECUTION_FAILED)) {
            command.getReturnValue().setSucceeded(false);
            command.setCommandStatus(CommandStatus.FAILED);
            command.getParameters().setTaskGroupSuccess(false);
            log.info("Switch master storage domain command has failed with async tasks '{}'.", taskIds);
        } else {
            command.getReturnValue().setSucceeded(true);
            command.setCommandStatus(CommandStatus.SUCCEEDED);
            command.getParameters().setTaskGroupSuccess(true);
            log.info("Switch master storage domain command has completed successfully with async tasks '{}'.", taskIds);
        }
        command.persistCommand(command.getParameters().getParentCommand());
    }

    @Override
    public boolean pollOnExecutionFailed() {
        return true;
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).endAction();
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).endAction();
    }

    private SwitchMasterStorageDomainCommand<SwitchMasterStorageDomainCommandParameters> getCommand(Guid cmdId) {
        return commandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
