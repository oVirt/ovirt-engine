package org.ovirt.engine.core.bll.snapshots;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.RemoveDiskSnapshotsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoveDiskSnapshotTaskHandler implements SPMAsyncTaskHandler {
    private static final Logger log = LoggerFactory.getLogger(RemoveDiskSnapshotTaskHandler.class);

    private final TaskHandlerCommand<? extends RemoveDiskSnapshotsParameters> enclosingCommand;
    private final Guid imageId;
    private final Guid imageGroupId;
    private final Guid vmId;

    public RemoveDiskSnapshotTaskHandler(TaskHandlerCommand<? extends RemoveDiskSnapshotsParameters> enclosingCommand,
                                         Guid imageId, Guid imageGroupId, Guid vmId) {
        this.enclosingCommand = enclosingCommand;
        this.imageId = imageId;
        this.imageGroupId = imageGroupId;
        this.vmId = vmId;
    }

    @Override
    public void execute() {
        if (enclosingCommand.getParameters().getExecutionIndex() == 0) {
            // lock all disk images in advance
            updateImagesStatus(ImageStatus.LOCKED);
        }

        VdcReturnValueBase vdcReturnValue = Backend.getInstance().runInternalAction(
                VdcActionType.RemoveSnapshotSingleDisk,
                buildRemoveSnapshotSingleDiskParameters(),
                getCommandContext());

        if (vdcReturnValue.getSucceeded()) {
            enclosingCommand.getReturnValue()
                    .getVdsmTaskIdList()
                    .addAll(vdcReturnValue.getInternalVdsmTaskIdList());
        }
        else {
            log.error("Failed RemoveSnapshotSingleDisk (Image '{}', VM '{}')", imageId, vmId);
        }

        ExecutionHandler.setAsyncJob(enclosingCommand.getExecutionContext(), true);
        enclosingCommand.getReturnValue().setSucceeded(true);
    }

    private ImagesContainterParametersBase buildRemoveSnapshotSingleDiskParameters() {
        ImagesContainterParametersBase parameters = new ImagesContainterParametersBase(
                imageId, vmId);

        DiskImage dest = DbFacade.getInstance().getDiskImageDao().getAllSnapshotsForParent(imageId).get(0);

        parameters.setDestinationImageId(dest.getImageId());
        parameters.setEntityInfo(enclosingCommand.getParameters().getEntityInfo());
        parameters.setParentParameters(enclosingCommand.getParameters());
        parameters.setParentCommand(enclosingCommand.getActionType());
        parameters.setWipeAfterDelete(dest.isWipeAfterDelete());
        parameters.setSessionId(enclosingCommand.getParameters().getSessionId());
        return parameters;
    }

    private CommandContext getCommandContext() {
        CommandContext commandContext =
                ExecutionHandler.createDefaultContextForTasks(enclosingCommand.getContext());
        commandContext.getExecutionContext().setShouldEndJob(isLastTaskHandler());

        return commandContext;
    }

    private boolean isLastTaskHandler() {
        RemoveDiskSnapshotsParameters parameters = enclosingCommand.getParameters();
        return parameters.getExecutionIndex() == parameters.getImageIds().size() - 1;
    }

    @Override
    public void endSuccessfully() {
        endRemoveSnapshotSingleDisk(true);
        enclosingCommand.taskEndSuccessfully();
        if (isLastTaskHandler()) {
            // Unlock on job finish
            updateImagesStatus(ImageStatus.OK);
        }
        enclosingCommand.getReturnValue().setSucceeded(true);
    }

    @Override
    public void endWithFailure() {
        endRemoveSnapshotSingleDisk(false);
        // Unlock all images since failure aborts the entire job
        Disk disk = DbFacade.getInstance().getDiskDao().get(imageGroupId);
        if (((DiskImage) disk).getImageStatus() == ImageStatus.LOCKED) {
            updateImagesStatus(ImageStatus.OK);
        }
        enclosingCommand.preventRollback();
        enclosingCommand.getReturnValue().setSucceeded(true);
    }

    private void endRemoveSnapshotSingleDisk(boolean taskGroupSuccess) {
        ImagesContainterParametersBase parameters = buildRemoveSnapshotSingleDiskParameters();
        parameters.setTaskGroupSuccess(taskGroupSuccess);
        VdcReturnValueBase vdcReturnValue = Backend.getInstance().endAction(
                VdcActionType.RemoveSnapshotSingleDisk,
                parameters,
                getCommandContext());
        enclosingCommand.getReturnValue().setSucceeded(vdcReturnValue.getSucceeded());
    }

    private void updateImagesStatus(ImageStatus imageStatus) {
        ImagesHandler.updateAllDiskImageSnapshotsStatusWithCompensation(imageGroupId,
                imageStatus,
                ImageStatus.ILLEGAL,
                null);
    }

    @Override
    public AsyncTaskType getTaskType() {
        // No implementation - handled by the command
        return null;
    }

    @Override
    public AsyncTaskType getRevertTaskType() {
        // No implementation - there is no live-merge
        return null;
    }

    @Override
    public void compensate() {
        if (enclosingCommand.getParameters().getExecutionIndex() == 0) {
            updateImagesStatus(ImageStatus.ILLEGAL);
        }
    }
}
