package org.ovirt.engine.core.dao;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.ovirt.engine.core.dao.FixturesTool.IMAGE_ID;
import static org.ovirt.engine.core.dao.FixturesTool.TEMPLATE_IMAGE_ID;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
/**
 * <code>DiskImageDaoTest</code> provides unit tests to validate {@link DiskImageDao}.
 */
public class DiskImageDaoTest extends BaseReadDaoTestCase<Guid, DiskImage, DiskImageDao> {
    private static final Guid ANCESTOR_IMAGE_ID = new Guid("c9a559d9-8666-40d1-9967-759502b19f0b");

    private DiskImage existingTemplate;

    private static final int TOTAL_DISK_IMAGES = 7;
    private BaseDiskDao diskDao;

    @Override
    protected Guid generateNonExistingId() {
        return Guid.newGuid();
    }

    @Override
    protected int getEneitiesTotalCount() {
        return TOTAL_DISK_IMAGES;
    }

    @Override
    protected DiskImageDao prepareDao() {
        return dbFacade.getDiskImageDao();
    }

    @Override
    protected Guid getExistingEntityId() {
        return IMAGE_ID;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        diskDao = dbFacade.getBaseDiskDao();
        existingTemplate = dao.get(TEMPLATE_IMAGE_ID);
    }

    @Test
    @Override
    public void testGet() {
        DiskImage result = dao.get(existingEntity.getImageId());

        assertNotNull(result);
        assertEquals(existingEntity, result);
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testGetAll() {
        super.testGetAll();
    }

    @Test
    public void testGetAncestorForSon() {
        DiskImage result = dao.getAncestor(existingEntity.getImageId());

        assertNotNull(result);
        assertEquals(ANCESTOR_IMAGE_ID, result.getImageId());
    }

    @Test
    public void testGetAncestorForFather() {
        DiskImage result = dao.getAncestor(ANCESTOR_IMAGE_ID);

        assertNotNull(result);
        assertEquals(ANCESTOR_IMAGE_ID, result.getImageId());
    }

    @Test
    public void getDiskSnapshotForVmSnapshotSameSnapshot() {
        DiskImage result = dao.getDiskSnapshotForVmSnapshot(existingEntity.getId(), existingEntity.getVmSnapshotId());
        assertNotNull(result);
        assertEquals(existingEntity.getId(), result.getId());
        assertEquals(existingEntity.getVmSnapshotId(), result.getVmSnapshotId());
    }

    @Test
    public void getDiskSnapshotForVmSnapshotDifferentSnapshot() {
        DiskImage result1 = dao.getDiskSnapshotForVmSnapshot(existingEntity.getId(), FixturesTool.EXISTING_SNAPSHOT_ID);
        DiskImage result2 =
                dao.getDiskSnapshotForVmSnapshot(existingEntity.getId(), FixturesTool.EXISTING_SNAPSHOT_ID2);
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(result1.getId(), result2.getId());
        assertNotSame("Images should be different", result1.getImageId(), result2.getImageId());
        assertNotSame("Vm snapshots should be different", result1.getVmSnapshotId(), result2.getVmSnapshotId());
    }

    @Test
    public void testGetTemplate() {
        DiskImage result = dao.get(TEMPLATE_IMAGE_ID);
        assertNotNull(result);
        assertEquals(existingTemplate, result);
    }

    @Test
    public void testGetImagesWithNoDisk() {
        List<DiskImage> result = dao.getImagesWithNoDisk(FixturesTool.VM_RHEL5_POOL_57);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        for (DiskImage image : result) {
            assertFalse(diskDao.exists(image.getId()));
        }
    }

    @Test
    public void testGetDiskSnapshotForVmSnapshot() {
        DiskImage result = dao.getDiskSnapshotForVmSnapshot(FixturesTool.IMAGE_GROUP_ID, FixturesTool.EXISTING_SNAPSHOT_ID);

        assertNotNull(result);
        assertEquals(FixturesTool.IMAGE_GROUP_ID, result.getId());
        assertEquals(FixturesTool.EXISTING_SNAPSHOT_ID, result.getVmSnapshotId());
    }

    @Test
    public void testGetDiskSnapshotForVmSnapshotNonExisting() {
        DiskImage result = dao.getDiskSnapshotForVmSnapshot(Guid.Empty, FixturesTool.EXISTING_SNAPSHOT_ID);

        assertNull(result);
    }

    @Test
    public void testGetImagesWithNoDiskReturnsEmptyList() {
        List<DiskImage> result = dao.getImagesWithNoDisk(Guid.newGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAllSnapshotsForLeaf() {
        List<DiskImage> images = dao.getAllSnapshotsForLeaf(FixturesTool.IMAGE_ID);

        assertFalse(images.isEmpty());
        assertTrue(images.stream().noneMatch(d -> d.getVmEntityType() == VmEntityType.TEMPLATE));
    }

    @Test
    public void testGetAllSnapshotsForLeafInvalidGuid() {
        List<DiskImage> images = dao.getAllSnapshotsForLeaf(Guid.newGuid());

        assertTrue(images.isEmpty());
    }

    @Test
    public void testEmptyGetAllDisksByDiskProfiles() {
        List<DiskImage> diskImages = dao.getAllForDiskProfiles(Collections.singletonList(Guid.newGuid()));

        assertNotNull(diskImages);
        assertTrue(diskImages.isEmpty());
    }

    @Test
    public void testGetAllDisksByDiskProfiles() {
        List<DiskImage> diskImages = dao.getAllForDiskProfiles(
                Arrays.asList(FixturesTool.DISK_PROFILE_1, FixturesTool.DISK_PROFILE_2));

        assertNotNull(diskImages);
        assertEquals(6, diskImages.size());
    }
}
