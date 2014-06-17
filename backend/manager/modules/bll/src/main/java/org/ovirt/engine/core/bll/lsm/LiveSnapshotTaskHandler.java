package org.ovirt.engine.core.bll.lsm;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.action.CreateAllSnapshotsFromVmParameters;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.LiveMigrateVmDisksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Image;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class LiveSnapshotTaskHandler implements SPMAsyncTaskHandler {

    private final TaskHandlerCommand<? extends LiveMigrateVmDisksParameters> enclosingCommand;
    private LinkedHashSet<Guid> movedVmDiskIds;

    public LiveSnapshotTaskHandler(TaskHandlerCommand<? extends LiveMigrateVmDisksParameters> enclosingCommand) {
        this.enclosingCommand = enclosingCommand;
    }

    private LinkedHashSet<Guid> getMovedDiskIds() {
       if (movedVmDiskIds == null) {
           movedVmDiskIds = new LinkedHashSet<>();
           for (LiveMigrateDiskParameters parameters : enclosingCommand.getParameters().getParametersList()) {
               movedVmDiskIds.add(parameters.getImageGroupID());
           }
       }
        return movedVmDiskIds;
    }

    private List<DiskImage> getMovedDisks() {
        LinkedHashSet<Guid> movedDiskIds = getMovedDiskIds();
        List<DiskImage> disks = new ArrayList<>();

        for (Guid diskId : movedDiskIds) {
            DiskImage disk = new DiskImage();
            disk.setId(diskId);
            disks.add(disk);
        }

        return disks;
    }

    @Override
    public void execute() {
        ImagesHandler.updateAllDiskImagesSnapshotsStatusInTransactionWithCompensation(getMovedDiskIds(),
                ImageStatus.LOCKED,
                ImageStatus.OK,
                enclosingCommand.getCompensationContext());

        if (enclosingCommand.getParameters().getTaskGroupSuccess()) {
            VdcReturnValueBase vdcReturnValue =
                    Backend.getInstance().runInternalAction(VdcActionType.CreateAllSnapshotsFromVm,
                    getCreateSnapshotParameters(),
                    ExecutionHandler.createInternalJobContext());
            enclosingCommand.getReturnValue().getVdsmTaskIdList().addAll(vdcReturnValue.getInternalVdsmTaskIdList());
        }
        enclosingCommand.getReturnValue().setSucceeded(true);
    }

    @Override
    public void endSuccessfully() {
        endCreateAllSnapshots();

        for (LiveMigrateDiskParameters parameters : enclosingCommand.getParameters().getParametersList()) {
            updateDestinationImageId(parameters);
        }

        ExecutionHandler.endJob(enclosingCommand.getExecutionContext(), true);
        enclosingCommand.setExecutionContext(null);
    }

    private void updateDestinationImageId(LiveMigrateDiskParameters parameters) {
        Image oldLeaf =
                DbFacade.getInstance().getImageDao().get(parameters.getImageId());
        List<DiskImage> allImages =
                DbFacade.getInstance().getDiskImageDao().getAllSnapshotsForImageGroup(oldLeaf.getDiskId());

        for (DiskImage image : allImages) {
            if (image.getImage().isActive()) {
                parameters.setDestinationImageId(image.getImageId());
                break;
            }
        }
    }

    private void endCreateAllSnapshots() {
        VdcReturnValueBase returnValue = Backend.getInstance().endAction(
                VdcActionType.CreateAllSnapshotsFromVm, getCreateSnapshotParameters(),
                ExecutionHandler.createDefaultContextForTasks(enclosingCommand.getContext()));
        enclosingCommand.getReturnValue().setSucceeded(returnValue.getSucceeded());
    }

    @Override
    public void endWithFailure() {
        for (LiveMigrateDiskParameters parameters : enclosingCommand.getParameters().getParametersList()) {
            updateDestinationImageId(parameters);
        }
        endCreateAllSnapshots();

        unlockAllDiskSnapshots();

        ExecutionHandler.endJob(enclosingCommand.getExecutionContext(), false);
        enclosingCommand.setExecutionContext(null);
        enclosingCommand.getReturnValue().setSucceeded(true);
    }

    @Override
    public void compensate() {
        unlockAllDiskSnapshots();
    }

    private void unlockAllDiskSnapshots() {
        // Unlock the image we left locked
        for (LiveMigrateDiskParameters parameters : enclosingCommand.getParameters().getParametersList()) {
            ImagesHandler.updateAllDiskImageSnapshotsStatus(parameters.getImageGroupID(), ImageStatus.OK);
        }
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

    protected CreateAllSnapshotsFromVmParameters getCreateSnapshotParameters() {
        CreateAllSnapshotsFromVmParameters params = new CreateAllSnapshotsFromVmParameters
                (enclosingCommand.getParameters().getVmId(), "Auto-generated for Live Storage Migration");

        params.setParentCommand(VdcActionType.LiveMigrateVmDisks);
        params.setSnapshotType(SnapshotType.REGULAR);
        params.setParentParameters(enclosingCommand.getParameters());
        params.setImagesParameters(enclosingCommand.getParameters().getImagesParameters());
        params.setTaskGroupSuccess(enclosingCommand.getParameters().getTaskGroupSuccess());
        params.setDisks(getMovedDisks());
        params.setDiskIdsToIgnoreInChecks(getMovedDiskIds());
        params.setNeedsLocking(false);

        return params;
    }
}
