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
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Typed(DestroyImageCommandCallback.class)
public class DestroyImageCommandCallback implements CommandCallback {
    private static final Logger log = LoggerFactory.getLogger(DestroyImageCommandCallback.class);

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        DestroyImageCommand<DestroyImageParameters> command = commandCoordinatorUtil.retrieveCommand(cmdId);
        Set<Guid> taskIds = new HashSet<>(command.getReturnValue().getVdsmTaskIdList());
        Map<Guid, AsyncTaskStatus> idToTaskStatusMap = commandCoordinatorUtil.pollTasks(taskIds);
        for (Map.Entry<Guid, AsyncTaskStatus> idToTaskStatus : idToTaskStatusMap.entrySet()) {
            Guid id = idToTaskStatus.getKey();
            AsyncTaskStatus status = idToTaskStatus.getValue();
            if (status.getTaskIsRunning()) {
                log.info("Waiting on destroy image command to complete the task (taskId = {})", id);
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
        if (failedTasks.isEmpty()) {
            command.setSucceeded(true);
            command.setCommandStatus(CommandStatus.SUCCEEDED);
            command.persistCommand(command.getParameters().getParentCommand());
            log.info("Destroy image command has completed successfully for images '{}' with async tasks '{}'.",
                    command.getParameters().getImageList(), taskIds);
        } else {
            command.setSucceeded(false);
            command.setCommandStatus(CommandStatus.FAILED);
            log.info("Destroy image command has failed for images '{}' with async tasks '{}'.",
                    command.getParameters().getImageList(), failedTasks);
        }
        command.persistCommand(command.getParameters().getParentCommand());
    }

    @Override
    public boolean pollOnExecutionFailed() {
        return true;
    }
}
