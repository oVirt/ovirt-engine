package org.ovirt.engine.core.bll.validator;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DiskSnapshotsValidator {

    private List<DiskImage> images;

    public DiskSnapshotsValidator(List<DiskImage> images) {
        this.images = images;
    }

    /**
     * Validates images existence by a specified images IDs list
     *
     * @return A {@link org.ovirt.engine.core.bll.ValidationResult} with the validation information.
     */
    public ValidationResult diskSnapshotsNotExist(List<Guid> imageIds) {
        Map<Guid, DiskImage> diskImagesByIdMap = ImagesHandler.getDiskImagesByIdMap(images);
        List<String> disksNotExistInDb = new ArrayList<>();

        for (Guid imageId : imageIds) {
            if (!diskImagesByIdMap.containsKey(imageId)) {
                disksNotExistInDb.add(imageId.toString());
            }
        }

        if (!disksNotExistInDb.isEmpty()) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SNAPSHOTS_NOT_EXIST,
                    String.format("$diskSnapshotIds %s", StringUtils.join(disksNotExistInDb, ", ")));
        }

        return ValidationResult.VALID;
    }

    public ValidationResult imagesAreSnapshots() {
        List<String> activeSnapshots = new ArrayList<>();
        for (DiskImage diskImage : images) {
            if (diskImage.getActive()) {
                activeSnapshots.add(String.format("%s (%s)",
                        diskImage.getImageId().toString(),
                        diskImage.getDiskAlias()));
            }
        }

        if (!activeSnapshots.isEmpty()) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SNAPSHOTS_ACTIVE,
                    String.format("$diskSnapshotIds %s", StringUtils.join(activeSnapshots, ", ")));
        }

        return ValidationResult.VALID;
    }

    /**
     * Validates the all images belong to the same imageGroup
     *
     * @return A {@link org.ovirt.engine.core.bll.ValidationResult} with the validation information.
     */
    public ValidationResult diskImagesBelongToSameImageGroup() {
        Guid imageGroupId = null;
        for (DiskImage diskImage : images) {
            if (imageGroupId == null || diskImage.getId().equals(imageGroupId)) {
                imageGroupId = diskImage.getId();
                continue;
            }

            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_SNAPSHOTS_DONT_BELONG_TO_SAME_DISK);
        }

        return ValidationResult.VALID;
    }
}
