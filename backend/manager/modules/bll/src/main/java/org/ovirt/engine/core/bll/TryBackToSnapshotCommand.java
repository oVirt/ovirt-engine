package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
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
        return getVm().getvm_guid();
    }

    /**
     * Set old image be inactive
     */
    @Override
    protected void ProcessOldImageFromDb() {
        DiskImage oldImage = GetOtherImageMappedToSameDrive();
        oldImage.setactive(false);
        DbFacade.getInstance().getImageVmMapDAO().update(
                new image_vm_map(false, oldImage.getId(), oldImage.getvm_guid()));
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

        // Update image_vm_map between Vm and original leaf image to be active:
        DiskImage originalLeafImage = GetOtherImageMappedToSameDrive();
        if (originalLeafImage != null) {
            DbFacade.getInstance().getImageVmMapDAO().update(
                    new image_vm_map(true, originalLeafImage.getId(), getVmId()));
        }

        // Remove destination, unlock source:
        UndoActionOnSourceAndDestination();

        setSucceeded(true);
    }
}
