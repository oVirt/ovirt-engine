package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Typed(RemoveImageCommandCallback.class)
public class RemoveImageCommandCallback implements CommandCallback {
    private static final Logger log = LoggerFactory.getLogger(RemoveImageCommandCallback.class);

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        RemoveImageCommand<RemoveImageParameters> command = getRemoveImageCommand(cmdId);
        Set<Guid> taskIds = new HashSet<>(command.getReturnValue().getVdsmTaskIdList());
        Map<Guid, AsyncTaskStatus> idToTaskStatusMap = commandCoordinatorUtil.pollTasks(taskIds);
        for (Map.Entry<Guid, AsyncTaskStatus> idToTaskStatus : idToTaskStatusMap.entrySet()) {
            Guid id = idToTaskStatus.getKey();
            AsyncTaskStatus status = idToTaskStatus.getValue();
            if (status.getTaskIsRunning()) {
                log.info("Waiting on remove image command to complete the task '{}'", id);
                return;
            }
        }

        List<AsyncTaskStatus> failedTasks = new ArrayList<>();
        idToTaskStatusMap.forEach((id, status) -> {
            if (!status.getTaskEndedSuccessfully()) {
                failedTasks.add(status);
             }
        });
        if (failedTasks.isEmpty()) {
            command.setSucceeded(true);
            command.setCommandStatus(CommandStatus.SUCCEEDED);
            command.persistCommand(command.getParameters().getParentCommand());
            log.info("Remove image command has completed successfully for disk '{}' with async task(s) '{}'.",
                    command.getParameters().getDiskImage().getId(), taskIds);
        } else {
            RuntimeException exception = failedTasks.get(0).getException();
            if (exception instanceof VDSErrorException) {
                command.getParameters()
                        .setAsyncTaskErrorMessage(((VDSErrorException) exception).getVdsError().getMessage());
            }
            command.setSucceeded(false);
            command.setCommandStatus(CommandStatus.FAILED);
            log.info("Remove image command has failed for disk '{}' with async task(s) '{}'.",
                    command.getParameters().getDiskImage().getId(), failedTasks);
        }
        command.persistCommand(command.getParameters().getParentCommand());
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        getRemoveImageCommand(cmdId).onSucceeded();
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        getRemoveImageCommand(cmdId).onFailed();
    }

    private RemoveImageCommand<RemoveImageParameters> getRemoveImageCommand(Guid cmdId) {
        return commandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
