package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

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
    public TryBackToSnapshotCommand(T parameters) {
        super(parameters);
        super.setVmId(parameters.getContainerId());
    }

    @Override
    protected Guid getImageContainerId() {
        return getVm().getId();
    }

    /**
     * Remove old image vm map.
     */
    @Override
    protected void ProcessOldImageFromDb() {
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
            log.errorFormat("Can't find image to update to active: {0}, snapshot type: {1}, original image id: {2}",
                    active,
                    snapshotType,
                    getImageId());
            return;
        }

        DiskImage oldImage = DbFacade.getInstance().getDiskImageDAO().getSnapshotById(oldImageId);
        oldImage.setactive(active);
        DbFacade.getInstance().getImageDao().update(oldImage.getImage());
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
        setSucceeded(true);
    }

    @Override
    protected void EndWithFailure() {
        updateOldImageActive(SnapshotType.ACTIVE, true);

        // Remove destination, unlock source:
        UndoActionOnSourceAndDestination();

        setSucceeded(true);
    }
}
