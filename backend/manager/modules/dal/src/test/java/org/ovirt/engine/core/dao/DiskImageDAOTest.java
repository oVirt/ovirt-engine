package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code.DiskImageDAOTest</code> provides unit tests to validate {@link DiskImageDAO}.
 *
 *
 */
public class DiskImageDAOTest extends BaseGenericDaoTestCase<Guid, DiskImage, DiskImageDAO> {
    private static final Guid FREE_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");
    private static final Guid EXISTING_IMAGE_ID = new Guid("42058975-3d5e-484a-80c1-01c31207f578");
    private static final Guid FREE_IMAGE_ID = new Guid("42058975-3d5e-484a-80c1-01c31207f579");
    private static final Guid EXISTING_IMAGE_DISK_TEMPLATE = new Guid("52058975-3d5e-484a-80c1-01c31207f578");
    private static final Guid ANCESTOR_IMAGE_ID = new Guid("c9a559d9-8666-40d1-9967-759502b19f0b");
    private static final Guid EXISTING_VM_TEMPLATE = new Guid("1b85420c-b84c-4f29-997e-0eb674b40b79");

    private DiskImage existingTemplate;

    private static final int TOTAL_DISK_IMAGES = 6;
    private static final int TOTAL_DISK_IMAGES_FOR_QAUOTA = 12;
    private DiskImageDynamicDAO diskImageDynamicDao;
    private DiskDao diskDao;
    private VmDeviceDAO vmDeviceDao;
    private DiskImage newImage;

    @Override
    protected Guid generateNonExistingId() {
        return Guid.NewGuid();
    }

    @Override
    protected int getEneitiesTotalCount() {
        return TOTAL_DISK_IMAGES;
    }

    @Override
    protected DiskImage generateNewEntity() {
        return newImage;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setvolume_type(VolumeType.Preallocated);
        existingEntity.setvolume_format(VolumeFormat.RAW);
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

        diskImageDynamicDao = prepareDAO(dbFacade.getDiskImageDynamicDAO());
        diskDao = prepareDAO(dbFacade.getDiskDao());
        vmDeviceDao = prepareDAO(dbFacade.getVmDeviceDAO());

        newImage = new DiskImage();
        newImage.setactive(true);
        newImage.setvm_guid(FixturesTool.VM_RHEL5_POOL_57);
        newImage.setit_guid(EXISTING_IMAGE_DISK_TEMPLATE);
        newImage.setId(Guid.NewGuid());
        newImage.setinternal_drive_mapping("4");
        newImage.setvolume_format(VolumeFormat.COW);
        newImage.setvolume_type(VolumeType.Sparse);
        newImage.setdisk_interface(DiskInterface.IDE);
        newImage.setdisk_type(DiskType.Data);
        newImage.setimage_group_id(Guid.NewGuid());
        newImage.setQuotaId(Guid.NewGuid());
        newImage.setstorage_ids(new ArrayList<Guid>(Arrays.asList(Guid.Empty)));
        existingTemplate = dao.get(EXISTING_IMAGE_DISK_TEMPLATE);
    }

    /**
    * Ensures that saving a disk image works as expected.
    */
    @Test
    @Override
    public void testSave() {
        dao.save(newImage);

        DiskImageDynamic dynamic = new DiskImageDynamic();
        dynamic.setId(newImage.getId());
        diskDao.save(newImage.getDisk());
        diskImageDynamicDao.save(dynamic);
        VmDevice vmDevice =
                new VmDevice(new VmDeviceId(newImage.getimage_group_id(), newImage.getvm_guid()),
                        "",
                        "",
                        "",
                        0,
                        "",
                        false,
                        false,
                        false);
        vmDeviceDao.save(vmDevice);
        DiskImageDynamic dynamicFromDB = diskImageDynamicDao.get(dynamic.getId());
        assertNotNull(dynamicFromDB);
        DiskImage result = dao.get(newImage.getId());

        assertNotNull(result);
        assertEquals(newImage, result);
        assertTrue(newImage.getactive());
    }

    @Test
    public void testGetAncestorForSon() {
        DiskImage result = dao.getAncestor(existingEntity.getId());

        assertNotNull(result);
        assertEquals(ANCESTOR_IMAGE_ID, result.getId());
    }

    @Test
    public void testGetAncestorForFather() {
        DiskImage result = dao.getAncestor(ANCESTOR_IMAGE_ID);

        assertNotNull(result);
        assertEquals(ANCESTOR_IMAGE_ID, result.getId());
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
    public void testGetAllForVM() {
        List<DiskImage> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57);
        assertFullGetAllForVMResult(disks);
    }

    @Test
    public void testGetAllForQuotaId() {
        List<DiskImage> disks = dao.getAllForQuotaId(FixturesTool.QUOTA_GENERAL);
        assertEquals("Wrong number of disk images for quota ", TOTAL_DISK_IMAGES_FOR_QAUOTA, disks.size());
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
            assertEquals(EXISTING_VM_TEMPLATE, template.getvm_guid());
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
     * @param disks The result to check
     */
    private static void assertFullGetAllForVMResult(List<DiskImage> disks) {
        assertEquals("VM should have two disks", 2, disks.size());
    }
}
