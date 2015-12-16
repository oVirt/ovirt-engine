package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>DiskImageDaoTest</code> provides unit tests to validate {@link ImageDao}.
 */
public class ImageDaoTest extends BaseGenericDaoTestCase<Guid, Image, ImageDao> {
    private static final Guid EXISTING_IMAGE_ID = new Guid("42058975-3d5e-484a-80c1-01c31207f578");
    private static final Guid EXISTING_IMAGE_DISK_TEMPLATE_ID = new Guid("52058975-3d5e-484a-80c1-01c31207f578");
    private static final Guid EXISTING_SNAPSHOT_ID = new Guid("a7bb24df-9fdf-4bd6-b7a9-f5ce52da0f89");

    private static final int TOTAL_IMAGES = 13;
    private Image newImage;

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEneitiesTotalCount() {
        return TOTAL_IMAGES;
    }

    @Override
    protected Image generateNewEntity() {
        return newImage;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setVolumeType(VolumeType.Preallocated);
        existingEntity.setVolumeFormat(VolumeFormat.RAW);
    }

    @Override
    protected ImageDao prepareDao() {
        return dbFacade.getImageDao();
    }

    @Override
    protected Guid getExistingEntityId() {
        return EXISTING_IMAGE_ID;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        newImage = new Image();
        newImage.setActive(true);
        newImage.setVolumeClassification(VolumeClassification.Volume);
        newImage.setTemplateImageId(EXISTING_IMAGE_DISK_TEMPLATE_ID);
        newImage.setSnapshotId(EXISTING_SNAPSHOT_ID);
        newImage.setId(Guid.newGuid());
        newImage.setVolumeFormat(VolumeFormat.COW);
        newImage.setVolumeType(VolumeType.Sparse);
        newImage.setDiskId(Guid.newGuid());
    }

    @Test
    public void testUpdateStatus() {
        dao.updateStatus(EXISTING_IMAGE_ID, ImageStatus.LOCKED);
        Image imageFromDb = dao.get(EXISTING_IMAGE_ID);
        assertNotNull(imageFromDb);
        assertEquals(ImageStatus.LOCKED, imageFromDb.getStatus());
    }

    @Test
    public void testUpdateImageVmSnapshotId() {
        Guid guid = Guid.newGuid();
        dao.updateImageVmSnapshotId(EXISTING_IMAGE_ID, guid);
        Image imageFromDb = dao.get(EXISTING_IMAGE_ID);
        assertNotNull(imageFromDb);
        assertEquals("Image snapshot id wasn't updated properly", guid, imageFromDb.getSnapshotId());
    }

    @Test
    public void updateStatusOfImagesByImageGroupId() {
        Image image = dao.get(EXISTING_IMAGE_ID);
        List<DiskImage> snapshots = dbFacade.getDiskImageDao().getAllSnapshotsForImageGroup(image.getDiskId());
        assertFalse(snapshots.size() == 1);
        for (DiskImage diskImage : snapshots) {
            assertFalse(ImageStatus.LOCKED == diskImage.getImageStatus());
        }
        dao.updateStatusOfImagesByImageGroupId(image.getDiskId(), ImageStatus.LOCKED);
        snapshots = dbFacade.getDiskImageDao().getAllSnapshotsForImageGroup(image.getDiskId());

        for (DiskImage diskImage : snapshots) {
            assertEquals(ImageStatus.LOCKED, diskImage.getImageStatus());
        }
    }
}
