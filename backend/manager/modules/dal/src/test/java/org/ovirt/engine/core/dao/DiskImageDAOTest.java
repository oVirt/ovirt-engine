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
    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final Guid FREE_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4354");
    private static final Guid EXISTING_IMAGE_ID = new Guid("42058975-3d5e-484a-80c1-01c31207f578");
    private static final Guid FREE_IMAGE_ID = new Guid("42058975-3d5e-484a-80c1-01c31207f579");
    private static final Guid EXISTING_IMAGE_DISK_TEMPLATE = new Guid("42058975-3d5e-484a-80c1-01c31207f578");
    private static final Guid ANCESTOR_IMAGE_ID = new Guid("c9a559d9-8666-40d1-9967-759502b19f0b");

    private static final int TOTAL_DISK_IMAGES = 4;
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
        newImage.setvm_guid(EXISTING_VM_ID);
        newImage.setit_guid(EXISTING_IMAGE_DISK_TEMPLATE);
        newImage.setId(Guid.NewGuid());
        newImage.setinternal_drive_mapping("4");
        newImage.setvolume_format(VolumeFormat.COW);
        newImage.setvolume_type(VolumeType.Sparse);
        newImage.setdisk_interface(DiskInterface.IDE);
        newImage.setdisk_type(DiskType.Data);
        newImage.setimage_group_id(Guid.NewGuid());
        newImageVmPoolMapping = new image_vm_pool_map(FREE_IMAGE_ID, "z", FREE_VM_ID);

        existingStatelessDiskImageMap = dao.getStatelessVmImageMapForImageId(existingEntity.getId());
        newStatelessVmImageMap = new stateless_vm_image_map(FREE_IMAGE_ID, "q", FREE_VM_ID);
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
                    EXISTING_VM_ID));
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
        List<image_vm_pool_map> result = dao.getImageVmPoolMapByVmId(EXISTING_VM_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (image_vm_pool_map map : result) {
            assertEquals(EXISTING_VM_ID, map.getvm_guid());
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
        List<stateless_vm_image_map> result = dao.getAllStatelessVmImageMapsForVm(EXISTING_VM_ID);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        for (stateless_vm_image_map mapping : result) {
            assertEquals(EXISTING_VM_ID, mapping.getvm_guid());
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
}
