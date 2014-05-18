package org.ovirt.engine.core.bll.validator;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.SnapshotDao;

public class DiskSnapshotsValidator {

    private List<DiskImage> images;

    public DiskSnapshotsValidator(List<DiskImage> images) {
        this.images = images;
    }

    /**
     * Validated whether the disk snapshots can be preview (according to the specified snapshot ID).
     * Previewing an Active VM snapshot is applicable only when custom selecting a subset of disks
     * (I.e. regular preview of Active VM snapshot isn't allowed).
     *
     * @return A {@link ValidationResult} with the validation information.
     */
    public ValidationResult canDiskSnapshotsBePreviewed(Guid dstSnapshotId) {
        Snapshot dstSnapshot = getSnapshotDao().get(dstSnapshotId);
        if (dstSnapshot.getType() == Snapshot.SnapshotType.ACTIVE) {
            if (images != null && !images.isEmpty()) {
                for (DiskImage diskImage : images) {
                    if (getDiskImageDao().get(diskImage.getImageId()) == null) {
                        return ValidationResult.VALID;
                    }
                }
            }

            return new ValidationResult(VdcBllMessages.CANNOT_PREVIEW_ACTIVE_SNAPSHOT);
        }

        return ValidationResult.VALID;
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    protected DiskImageDAO getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDao();
    }
}
