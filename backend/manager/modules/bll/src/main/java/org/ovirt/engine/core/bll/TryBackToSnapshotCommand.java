package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

/**
 * This command responsible to creating new snapshot from non leaf snapshot. Its
 * makes currently active snapshot to be inactive and makes new created snapshot
 * active.
 *
 * Parameters: Guid snapshotId - id of source snapshot Guid containerId - id of
 * container VM string drive - mapping of new snapshot in Vm
 */
@InternalCommandAttribute
public class TryBackToSnapshotCommand<T extends ImagesContainterParametersBase> extends CreateSnapshotCommand<T> {
    public TryBackToSnapshotCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        super.setVmId(parameters.getContainerId());
    }

    /**
     * Remove old image vm map.
     */
    @Override
    protected void processOldImageFromDb() {
        updateOldImageActive(SnapshotType.PREVIEW, false);
    }

    /**
     * Update the old image that represents the disk of the command's image to be in the given active state.
     *
     * @param snapshotType
     *            The type of snapshot to look for the same image in.
     * @param active
     *            The active state.
     */
    protected void updateOldImageActive(SnapshotType snapshotType, boolean active) {
        Guid oldImageId = findImageForSameDrive(snapshotType);
        if (oldImageId == null) {
            log.error("Can't find image to update to active '{}', snapshot type '{}', original image id '{}'",
                    active,
                    snapshotType,
                    getImageId());
            return;
        }

        DiskImage oldImage = getDiskImageDao().getSnapshotById(oldImageId);
        oldImage.setActive(active);
        getImageDao().update(oldImage.getImage());
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        updateOldImageActive(SnapshotType.ACTIVE, true);

        // Remove destination, unlock source:
        undoActionOnSourceAndDestination();

        setSucceeded(true);
    }
}
