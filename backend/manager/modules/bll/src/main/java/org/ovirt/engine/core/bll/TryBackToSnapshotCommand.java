package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.image_vm_map;
import org.ovirt.engine.core.common.businessentities.image_vm_map_id;
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
        Guid oldImageId = findImageForSameDrive(SnapshotType.PREVIEW);
        DbFacade.getInstance().getImageVmMapDAO().remove(
                new image_vm_map_id(oldImageId, getImageContainerId()));
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
        setSucceeded(true);
    }

    @Override
    protected void EndWithFailure() {
        // Remove image_vm_map between Vm and the preview snapshot:
        DbFacade.getInstance().getImageVmMapDAO().remove(new image_vm_map_id(getDestinationImageId(), getVmId()));

        // Restore image_vm_map between Vm and original leaf image to be active:
        Guid originalLeafImage = findImageForSameDrive(SnapshotType.ACTIVE);
        DbFacade.getInstance().getImageVmMapDAO().save(new image_vm_map(true, originalLeafImage, getVmId()));

        // Remove destination, unlock source:
        UndoActionOnSourceAndDestination();

        setSucceeded(true);
    }
}
