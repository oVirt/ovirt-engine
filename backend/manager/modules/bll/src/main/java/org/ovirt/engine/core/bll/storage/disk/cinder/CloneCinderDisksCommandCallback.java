package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CloneCinderDisksParameters;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CloneCinderDisksCommandCallback<T extends CommandBase<CloneCinderDisksParameters>> extends CommandCallback {
    private static final Logger log = LoggerFactory.getLogger(CloneCinderDisksCommandCallback.class);

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {

        boolean anyFailed = false;
        int finishedChildren = 0;
        for (Guid childCmdId : childCmdIds) {
            CommandStatus commandStatus = CommandCoordinatorUtil.getCommandStatus(childCmdId);
            switch (commandStatus) {
            case NOT_STARTED:
            case ACTIVE:
                log.info("Waiting on CloneCinderDisksCommandCallback child commands to complete");
                break;
            case ENDED_SUCCESSFULLY:
            case SUCCEEDED:
                finishedChildren++;
                break;
            case ENDED_WITH_FAILURE:
            case FAILED:
            case FAILED_RESTARTED:
            case UNKNOWN:
                anyFailed = true;
            default:
                finishedChildren++;
                log.error("Invalid command status: '{}", commandStatus);
                break;
            }
        }

        if (finishedChildren == childCmdIds.size()) {
            T command = getCommand(cmdId);
            command.getParameters().setTaskGroupSuccess(!anyFailed);
            command.setCommandStatus(anyFailed ? CommandStatus.FAILED : CommandStatus.SUCCEEDED);
            log.info("All CloneCinderDisksCommandCallback commands have completed, status '{}'",
                    command.getCommandStatus());
        } else {
            log.info("Waiting for all child commands to finish. {}/{} child commands were completed.",
                    childCmdIds.size(),
                    finishedChildren);
        }
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).endAction();
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        revertCinderDisks(childCmdIds);
        getCommand(cmdId).endAction();
        getCommand(cmdId).getParameters().setTaskGroupSuccess(false);
    }

    private void revertCinderDisks(List<Guid> childCmdIds) {
        for (Guid childCmdId : childCmdIds) {
            ImagesContainterParametersBase commandParameters =
                    (ImagesContainterParametersBase) CommandCoordinatorUtil.getCommandEntity(childCmdId)
                            .getCommandParameters();
            Guid destinationImageId = commandParameters.getDestinationImageId();
            Guid storageDomainId = commandParameters.getStorageDomainId();
            removeCinderDisk(destinationImageId, storageDomainId);
        }
    }

    private void removeCinderDisk(Guid cinderDiskId, Guid storageDomainId) {
        Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                VdcActionType.RemoveCinderDisk,
                buildChildCommandParameters(cinderDiskId),
                null,
                new SubjectEntity(VdcObjectType.Storage, storageDomainId));
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Fail to revert disk id '{}'.", cinderDiskId);
            log.error("Exception: ", e);
        }
    }

    private RemoveCinderDiskParameters buildChildCommandParameters(Guid cinderDiskId) {
        RemoveCinderDiskParameters removeDiskParams = new RemoveCinderDiskParameters(cinderDiskId);
        removeDiskParams.setLockVM(false);
        removeDiskParams.setShouldBeLogged(false);
        return removeDiskParams;
    }

    private T getCommand(Guid cmdId) {
        return (T) CommandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
