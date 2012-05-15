package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

    private static final int TOTAL_IMAGES = 9;
    private Image newImage;

    @Override
    protected Guid generateNonExistingId() {
        return Guid.NewGuid();
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
        return prepareDAO(dbFacade.getImageDao());
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
        newImage.setId(Guid.NewGuid());
        newImage.setVolumeFormat(VolumeFormat.COW);
        newImage.setVolumeType(VolumeType.Sparse);
        newImage.setDiskId(Guid.NewGuid());
        newImage.setQuotaId(Guid.NewGuid());
    }

    @Test
    public void testUpdateStatus() {
        dao.updateStatus(EXISTING_IMAGE_ID, ImageStatus.LOCKED);
        Image imageFromDb = dao.get(EXISTING_IMAGE_ID);
        assertNotNull(imageFromDb);
        assertEquals(ImageStatus.LOCKED, imageFromDb.getStatus());
    }
}
