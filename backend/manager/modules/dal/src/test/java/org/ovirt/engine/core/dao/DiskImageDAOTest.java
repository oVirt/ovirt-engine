package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code.DiskImageDAOTest</code> provides unit tests to validate {@link DiskImageDAO}.
 */
public class DiskImageDAOTest extends BaseReadDaoTestCase<Guid, DiskImage, DiskImageDAO> {
    private static final Guid EXISTING_IMAGE_ID = new Guid("42058975-3d5e-484a-80c1-01c31207f578");
    private static final Guid EXISTING_IMAGE_DISK_TEMPLATE = new Guid("52058975-3d5e-484a-80c1-01c31207f578");
    private static final Guid ANCESTOR_IMAGE_ID = new Guid("c9a559d9-8666-40d1-9967-759502b19f0b");
    private static final Guid EXISTING_VM_TEMPLATE = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79");

    private DiskImage existingTemplate;

    private static final int TOTAL_DISK_IMAGES = 7;
    private BaseDiskDao diskDao;

    @Override
    protected Guid generateNonExistingId() {
        return Guid.NewGuid();
    }

    @Override
    protected int getEneitiesTotalCount() {
        return TOTAL_DISK_IMAGES;
    }

    @Override
    protected DiskImageDAO prepareDao() {
        return prepareDAO(dbFacade.getDiskImageDAO());
    }

    @Override
    protected Guid getExistingEntityId() {
        return EXISTING_IMAGE_ID;
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        diskDao = prepareDAO(dbFacade.getBaseDiskDao());

        existingTemplate = dao.get(EXISTING_IMAGE_DISK_TEMPLATE);
    }

    @Test
    @Override
    public void testGet() {
        DiskImage result = dao.get(existingEntity.getImageId());

        assertNotNull(result);
        assertEquals(existingEntity, result);
    }

    @Override
    @Test(expected = NotImplementedException.class)
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
    public void testGetImagesByStorageIdAndTempleteNull() {
        List<DiskImage> result =
                dao.getImagesByStorageIdAndTemplateId(Guid.createGuidFromString("72e3a666-89e1-4005-a7ca-f7548004a9ab"),
                        null);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetImagesByStorageIdAndTempleteId() {
        List<DiskImage> result =
                dao.getImagesByStorageIdAndTemplateId(Guid.createGuidFromString("72e3a666-89e1-4005-a7ca-f7548004a9ab"),
                        Guid.createGuidFromString("1b85420c-b84c-4f29-997e-0eb674b40b79"));

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetAllAttachableDisksByPoolIdNull() {
        List<DiskImage> result =
                dao.getAllAttachableDisksByPoolId(null, null, false);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetAllAttachableDisksByPoolId() {
        List<DiskImage> result =
                dao.getAllAttachableDisksByPoolId(Guid.createGuidFromString("6d849ebf-755f-4552-ad09-9a090cda105d"),
                        null,
                        false);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetAllForVM() {
        List<DiskImage> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57);
        assertFullGetAllForVMResult(disks);
    }

    @Test
    public void testGetAllForQuotaId() {
        List<DiskImage> disks = dao.getAllForQuotaId(FixturesTool.QUOTA_GENERAL);
        assertEquals("VM should have five disks", 4, disks.size());
    }

    @Test
    public void testGetAllForVMFilteredWithPermissions() {
        // test user 3 - has permissions
        List<DiskImage> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57, PRIVILEGED_USER_ID, true);
        assertFullGetAllForVMResult(disks);
    }

    @Test
    public void testGetAllForVMFilteredWithPermissionsNoPermissions() {
        // test user 2 - hasn't got permissions
        List<DiskImage> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57, UNPRIVILEGED_USER_ID, true);
        assertTrue("VM should have no disks viewable to the user", disks.isEmpty());
    }

    @Test
    public void testGetAllForVMFilteredWithPermissionsNoPermissionsAndNoFilter() {
        // test user 2 - hasn't got permissions, but no filtering was requested
        List<DiskImage> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57, UNPRIVILEGED_USER_ID, false);
        assertFullGetAllForVMResult(disks);
    }

    public void testGetTemplate() {
        DiskImage result = dao.get(EXISTING_IMAGE_DISK_TEMPLATE);
        assertNotNull(result);
        assertEquals(existingTemplate, result);
    }

    @Test
    public void testGetAllForVm() {
        List<DiskImage> result = dao
                .getAllForVm(EXISTING_VM_TEMPLATE);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (DiskImage template : result) {
            assertEquals(EXISTING_IMAGE_DISK_TEMPLATE, template.getImageId());
        }
    }

    @Test
    public void testGetImagesWithNoDisk() {
        List<DiskImage> result = dao.getImagesWithNoDisk(FixturesTool.VM_RHEL5_POOL_57);

        assertNotNull(result);
        assertFalse(result.isEmpty());

        for (DiskImage image : result) {
            assertFalse(diskDao.exists(image.getimage_group_id()));
        }
    }

    @Test
    public void testGetAllImagesWithQuery() {
        List<DiskImage> result =
                dao.getAllWithQuery("SELECT * FROM (SELECT * FROM vm_images_view WHERE ( image_guid IN (SELECT vm_images_view.image_guid FROM  vm_images_view  ))  ORDER BY disk_alias ASC ) as T1 OFFSET (1 -1) LIMIT 100");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    public void testGetImagesWithNoDiskReturnsEmptyList() {
        List<DiskImage> result = dao.getImagesWithNoDisk(Guid.NewGuid());

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    /**
     * Asserts the result of {@link DiskImageDAO#getAllForVm(Guid)} contains the correct disks.
     * @param disks
     *            The result to check
     */
    private static void assertFullGetAllForVMResult(List<DiskImage> disks) {
        assertEquals("VM should have two disks", 2, disks.size());
    }
}
