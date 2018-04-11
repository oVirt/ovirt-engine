package org.ovirt.engine.core.bll.validator.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.di.Injector;

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
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SNAPSHOTS_NOT_EXIST,
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
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SNAPSHOTS_ACTIVE,
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

            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_SNAPSHOTS_DONT_BELONG_TO_SAME_DISK);
        }

        return ValidationResult.VALID;
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

            return new ValidationResult(EngineMessage.CANNOT_PREVIEW_ACTIVE_SNAPSHOT);
        }

        return ValidationResult.VALID;
    }

    protected SnapshotDao getSnapshotDao() {
        return Injector.get(SnapshotDao.class);
    }

    protected DiskImageDao getDiskImageDao() {
        return Injector.get(DiskImageDao.class);
    }
}
