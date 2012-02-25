package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.image_vm_map;
import org.ovirt.engine.core.common.businessentities.image_vm_pool_map;
import org.ovirt.engine.core.common.businessentities.stateless_vm_image_map;
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



    private static final int TOTAL_DISK_IMAGES = 5;
    private DiskImageDynamicDAO diskImageDynamicDao;
    private DiskDao diskDao;
    private DiskImage newImage;
    private image_vm_pool_map existingVmPoolMapping;
    private image_vm_pool_map newImageVmPoolMapping;
    private stateless_vm_image_map existingStatelessDiskImageMap;
    private stateless_vm_image_map newStatelessVmImageMap;

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
        existingEntity.setdescription("This is a new description");
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

        existingVmPoolMapping = dao.getImageVmPoolMapByImageId(EXISTING_IMAGE_ID);

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
        newImageVmPoolMapping = new image_vm_pool_map(FREE_IMAGE_ID, "z", FREE_VM_ID);

        existingStatelessDiskImageMap = dao.getStatelessVmImageMapForImageId(existingEntity.getId());
        newStatelessVmImageMap = new stateless_vm_image_map(FREE_IMAGE_ID, "q", FREE_VM_ID);
        existingTemplate = dao.get(EXISTING_IMAGE_DISK_TEMPLATE );
    }

     /**
     * Ensures that saving a disk image works as expected.
     */
    @Test
    @Override
    public void testSave() {
        dao.save(newImage);

        // TODO this call is only necessary when we have a DbFacade implementation
        if (dao instanceof BaseDAODbFacade) {
            dbFacade.getImageVmMapDAO().save(new image_vm_map(true, newImage.getId(),
                    FixturesTool.VM_RHEL5_POOL_57));
        }
        DiskImageDynamic dynamic = new DiskImageDynamic();
        dynamic.setId(newImage.getId());
        diskDao.save(newImage.getDisk());
        diskImageDynamicDao.save(dynamic);
        DiskImageDynamic dynamicFromDB = diskImageDynamicDao.get(dynamic.getId());
        assertNotNull(dynamicFromDB);
        DiskImage result = dao.get(newImage.getId());

        assertNotNull(result);
        assertEquals(newImage, result);

        image_vm_map mapping = dbFacade.getImageVmMapDAO().getByImageId(result.getId());

        assertNotNull(mapping);
        assertTrue(mapping.getactive());
        assertEquals(newImage.getId(), mapping.getimage_id());
        assertEquals(newImage.getvm_guid(), mapping.getvm_id());
    }

    @Test
    public void testGetImageVmPoolMapByImageIdWithWrongImage() {
        image_vm_pool_map result = dao.getImageVmPoolMapByImageId(Guid.NewGuid());

        assertNull(result);
    }

    @Test
    public void testGetImageVmPoolMapByImageId() {
        image_vm_pool_map result = dao.getImageVmPoolMapByImageId(EXISTING_IMAGE_ID);

        assertNotNull(result);
        assertEquals(existingVmPoolMapping, result);
    }

    @Test
    public void testAddImageVmPoolMap() {
        dao.addImageVmPoolMap(newImageVmPoolMapping);

        image_vm_pool_map result = dao.getImageVmPoolMapByImageId(newImageVmPoolMapping.getimage_guid());

        assertNotNull(result);
        assertEquals(newImageVmPoolMapping, result);
    }

    @Test
    public void testRemoveImageVmPoolMap() {
        dao.removeImageVmPoolMap(existingVmPoolMapping.getimage_guid());

        image_vm_pool_map result = dao.getImageVmPoolMapByImageId(existingVmPoolMapping.getimage_guid());

        assertNull(result);
    }

    @Test
    public void testGetImageVmPoolMapByVmId() {
        List<image_vm_pool_map> result = dao.getImageVmPoolMapByVmId(FixturesTool.VM_RHEL5_POOL_57);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (image_vm_pool_map map : result) {
            assertEquals(FixturesTool.VM_RHEL5_POOL_57, map.getvm_guid());
        }
    }

    @Test
    public void testGetStatelessDiskImageForImageId() {
        stateless_vm_image_map result = dao.getStatelessVmImageMapForImageId(EXISTING_IMAGE_ID);

        assertNotNull(result);
        assertEquals(existingStatelessDiskImageMap, result);
    }

    @Test
    public void testAddStatelessDiskImage() {
        dao.addStatelessVmImageMap(newStatelessVmImageMap);

        stateless_vm_image_map result = dao.getStatelessVmImageMapForImageId(FREE_IMAGE_ID);

        assertNotNull(result);
        assertEquals(newStatelessVmImageMap, result);
    }

    @Test
    public void testRemoveStatelessDiskImage() {
        dao.removeStatelessVmImageMap(existingStatelessDiskImageMap.getimage_guid());

        stateless_vm_image_map result = dao.getStatelessVmImageMapForImageId(EXISTING_IMAGE_ID);

        assertNull(result);
    }

    @Test
    public void testGetAllStatelessDiskImagesForVm() {
        List<stateless_vm_image_map> result = dao.getAllStatelessVmImageMapsForVm(FixturesTool.VM_RHEL5_POOL_57);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (stateless_vm_image_map mapping : result) {
            assertEquals(FixturesTool.VM_RHEL5_POOL_57, mapping.getvm_guid());
        }
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
    public void testGetAllForVM() {
        List<DiskImage> disks = dao.getAllForVm(FixturesTool.VM_RHEL5_POOL_57);
        assertFullGetAllForVMResult(disks);
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
            assertEquals(EXISTING_IMAGE_DISK_TEMPLATE, template.getId());
        }
    }

    /**
     * Asserts the result of {@link DiskImageDAO#getAllForVm(Guid)} contains the correct disks.
     * @param disks The result to check
     */
    private static void assertFullGetAllForVMResult(List<DiskImage> disks) {
        assertEquals("VM should have two disks", 2, disks.size());
    }

}
