package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByDriveParameters;
import org.ovirt.engine.core.common.queries.GetAllVmSnapshotsByDriveQueryReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;

@Deprecated
public class GetAllVmSnapshotsByDriveQuery<P extends GetAllVmSnapshotsByDriveParameters>
        extends QueriesCommandBase<P> {
    public GetAllVmSnapshotsByDriveQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected GetAllVmSnapshotsByDriveQueryReturnValue createReturnValue() {
        return new GetAllVmSnapshotsByDriveQueryReturnValue();
    }

    @Override
    protected void executeQueryCommand() {
        Guid tryingImage = Guid.Empty;
        DiskImage activeDisk = findImageForDrive(SnapshotType.ACTIVE);
        DiskImage inactiveDisk = findImageForDrive(SnapshotType.PREVIEW);

        if (getDiskDao().getAllForVm(getParameters().getId(), getUserID(), getParameters().isFiltered()).isEmpty()
                || activeDisk == null
                || imageBeforePreviewIsMissing(activeDisk, inactiveDisk)) {
            log.warnFormat("Vm {0} images data incorrect", getParameters().getId());
            getQueryReturnValue().setReturnValue(new ArrayList<DiskImage>());
            return;
        }

        if (inactiveDisk != null) {
            tryingImage = activeDisk.getParentId();
        }
        Guid topmostImageGuid = inactiveDisk == null ? activeDisk.getImageId() : inactiveDisk.getImageId();

        // Note that no additional permission filtering is needed -
        // if a user could read the disk of a VM, all its snapshots are OK too
        getQueryReturnValue().setReturnValue(
                ImagesHandler.getAllImageSnapshots(topmostImageGuid, activeDisk.getit_guid()));
        getQueryReturnValue().setTryingImage(tryingImage);
    }

    protected boolean imageBeforePreviewIsMissing(DiskImage activeDisk, DiskImage inactiveDisk) {
        return getSnapshotDao().exists(getParameters().getId(), SnapshotStatus.IN_PREVIEW)
                && inactiveDisk == null
                && activeDisk.getimageStatus() != ImageStatus.ILLEGAL;
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }

    protected DiskImageDAO getDiskImageDao() {
        return getDbFacade().getDiskImageDAO();
    }

    protected DiskDao getDiskDao() {
        return getDbFacade().getDiskDao();
    }

    /**
     * Find the image for the same drive by the snapshot type:<br>
     * The image is the image from the snapshot of the given type, which represents the same drive.
     * @param snapshotType
     *            The snapshot type for which the other image should exist.
     *
     * @return The image for the same drive, or <code>null</code> if not found.
     */
    private DiskImage findImageForDrive(SnapshotType snapshotType) {
        Guid snapshotId = getSnapshotDao().getId(getParameters().getId(), snapshotType);
        if (snapshotId == null) {
            return null;
        }

        List<DiskImage> imagesFromSanpshot = getDiskImageDao().getAllSnapshotsForVmSnapshot(snapshotId);
        for (DiskImage diskImage : imagesFromSanpshot) {
            if (getParameters().getDrive().equals(diskImage.getinternal_drive_mapping())) {
                return diskImage;
            }
        }

        return null;
    }

    @Override
    public GetAllVmSnapshotsByDriveQueryReturnValue getQueryReturnValue() {
        return (GetAllVmSnapshotsByDriveQueryReturnValue) super.getQueryReturnValue();
    }
}
