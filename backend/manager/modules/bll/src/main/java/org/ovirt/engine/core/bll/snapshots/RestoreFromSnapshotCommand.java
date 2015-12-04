package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.storage.domain.PostZeroHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RestoreFromSnapshotParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.vdscommands.DestroyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

/**
 * This command responsible to make snapshot of some Vm mapped to some drive be
 * active snapshot. All children snapshots and other snapshot mapped to same
 * drive will be removed.
 */
@InternalCommandAttribute
public class RestoreFromSnapshotCommand<T extends RestoreFromSnapshotParameters> extends BaseImagesCommand<T> {

    private final ArrayList<Guid> _imagesToDelete = new ArrayList<>();

    public RestoreFromSnapshotCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        if (removeImages()) {
            if (getParameters().getSnapshot().getType() != SnapshotType.REGULAR) {
                getImage().setActive(true);
                getImageDao().update(getImage().getImage());
            }

            setSucceeded(true);
        }
    }

    private boolean removeImages() {
        Guid imageToRemoveId = findImageForSameDrive(getParameters().getRemovedSnapshotId());

        switch (getParameters().getSnapshot().getType()) {
        case REGULAR:
            removeOtherImageAndParents(imageToRemoveId, getDiskImage().getParentId());
            break;
        case PREVIEW:
        case STATELESS:
            if (imageToRemoveId != null) {
                removeSnapshot(getDiskImageDao().get(imageToRemoveId));
            }

            break;
        }

        VDSReturnValue vdsReturnValue = performImageVdsmOperation();
        return vdsReturnValue != null && vdsReturnValue.getSucceeded();
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.deleteVolume;
    }

    @Override
    protected void removeSnapshot(DiskImage snapshot) {
        super.removeSnapshot(snapshot);
        _imagesToDelete.add(_imagesToDelete.size(), snapshot.getImageId());
    }

    private void removeOtherImageAndParents(Guid imageId, Guid lastParent) {
        DiskImage image = getDiskImageDao().getSnapshotById(imageId);
        // store other mapped image's parent Id
        Guid currentParent = image.getParentId();
        // Remove other mapped image from Irs and db
        removeSnapshot(image);
        while (!lastParent.equals(currentParent)) {
            image = getDiskImageDao().getSnapshotById(currentParent);
            // store current image's parent Id
            currentParent = image.getParentId();
            removeSnapshot(image);
        }
    }

    @Override
    protected VDSReturnValue performImageVdsmOperation() {
        VDSReturnValue vdsReturnValue = null;
        try {
            Guid storagePoolId = getDiskImage().getStoragePoolId() != null ? getDiskImage().getStoragePoolId()
                    : Guid.Empty;
            Guid storageDomainId =
                    getDiskImage().getStorageIds() != null && !getDiskImage().getStorageIds().isEmpty() ? getDiskImage().getStorageIds()
                            .get(0)
                            : Guid.Empty;
            Guid imageGroupId = getDiskImage().getId() != null ? getDiskImage().getId() : Guid.Empty;

            Guid taskId = persistAsyncTaskPlaceHolder(VdcActionType.RestoreAllSnapshots);

            vdsReturnValue = runVdsCommand(VDSCommandType.DestroyImage,
                    PostZeroHandler.fixParametersWithPostZero(
                            new DestroyImageVDSCommandParameters(storagePoolId, storageDomainId, imageGroupId,
                                    _imagesToDelete, getDiskImage().isWipeAfterDelete(), true)));

            if (vdsReturnValue.getSucceeded()) {
                getReturnValue().getInternalVdsmTaskIdList().add(
                        createTask(taskId,
                                vdsReturnValue.getCreationInfo(),
                                VdcActionType.RestoreAllSnapshots,
                                VdcObjectType.Storage,
                                storageDomainId));
            }
        }
        // Don't throw an exception when cannot destroy image in the VDSM.
        catch (EngineException e) {
            // Set fault for parent command RestoreAllSnapshotCommand to use, if decided to fail the command.
            getReturnValue().setFault(new EngineFault(e, e.getVdsError().getCode()));
            log.info("Image '{}' not exist in Irs", getDiskImage().getImageId());
        }
        return vdsReturnValue;
    }
}
