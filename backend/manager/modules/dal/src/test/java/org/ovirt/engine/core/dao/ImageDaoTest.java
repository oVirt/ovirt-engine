package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.Image;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code.DiskImageDAOTest</code> provides unit tests to validate {@link ImageDao}.
 */
public class ImageDaoTest extends BaseGenericDaoTestCase<Guid, Image, ImageDao> {
    private static final Guid EXISTING_IMAGE_ID = new Guid("42058975-3d5e-484a-80c1-01c31207f578");
    private static final Guid EXISTING_IMAGE_DISK_TEMPLATE_ID = new Guid("52058975-3d5e-484a-80c1-01c31207f578");
    private static final Guid EXISTING_SNAPSHOT_ID = new Guid("a7bb24df-9fdf-4bd6-b7a9-f5ce52da0f89");

    private static final int TOTAL_IMAGES = 11;
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
        newImage.setTemplateImageId(EXISTING_IMAGE_DISK_TEMPLATE_ID);
        newImage.setSnapshotId(EXISTING_SNAPSHOT_ID);
        newImage.setId(Guid.newGuid());
        newImage.setVolumeFormat(VolumeFormat.COW);
        newImage.setVolumeType(VolumeType.Sparse);
        newImage.setDiskId(Guid.newGuid());
        newImage.setQuotaId(Guid.newGuid());
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
    public void testChangeQuotaForDisk() {
        // fetch image
        Image image = dao.get(FixturesTool.IMAGE_ID);
        Guid diskId = image.getDiskId();
        Guid quotaId = image.getQuotaId();
        // test that the current quota doesn't equal with the new quota
        if(quotaId.equals(FixturesTool.DEFAULT_QUOTA_GENERAL)){
            fail("Same source and dest quota id, cannot perform test");
        }
        // change quota to the new quota
        dao.updateQuotaForImageAndSnapshots(diskId, FixturesTool.DEFAULT_QUOTA_GENERAL);
        // fetch the image again
        image = dao.get(FixturesTool.IMAGE_ID);
        quotaId = image.getQuotaId();
        // check that the new quota is the inserted one
        assertEquals("quota wasn't changed", quotaId, FixturesTool.DEFAULT_QUOTA_GENERAL);
    }
}
