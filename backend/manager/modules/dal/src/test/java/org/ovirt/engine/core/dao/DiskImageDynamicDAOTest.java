package org.ovirt.engine.core.dao;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Guid;


public class DiskImageDynamicDAOTest extends BaseDAOTestCase{
    private static final Guid EXISTING_IMAGE_ID = new Guid("42058975-3d5e-484a-80c1-01c31207f578");
    private static final int TOTAL_DYNAMIC_DISK_IMAGES = 4;
    private static final Guid EXISTING_VM_ID = new Guid("77296e00-0cad-4e5a-9299-008a7b6f4355");
    private static final Guid EXISTING_IMAGE_DISK_TEMPLATE = new Guid("42058975-3d5e-484a-80c1-01c31207f578");



    private DiskImageDynamicDAO dao;
    private ImageDao imageDao;
    private BaseDiskDao diskDao;
    private DiskImageDynamic existingDynamic;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getDiskImageDynamicDao();
        imageDao = dbFacade.getImageDao();
        diskDao = dbFacade.getBaseDiskDao();
        existingDynamic = dao.get(EXISTING_IMAGE_ID);
    }

    /**
     * Ensures that retrieving with an incorrect ID returns null.
     */
    @Test
    public void testGetWithInvalidId() {
        DiskImageDynamic result = dao.get(Guid.NewGuid());

        assertNull(result);
    }

    /**
     * Ensures that retrieving the dynamic image works as expected.
     */
    @Test
    public void testGet() {
        DiskImageDynamic result = dao.get(existingDynamic.getId());

        assertNotNull(result);
        assertEquals(existingDynamic, result);
    }

    /**
     * Ensures that retrieving all dynamic disk images works.
     */
    @Test
    public void testGetAll() {
        List<DiskImageDynamic> result = dao.getAll();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(TOTAL_DYNAMIC_DISK_IMAGES, result.size());
    }

    /**
     * Ensures that updating a dynamic image works.
     */
    @Test
    public void testUpdate() {
        existingDynamic.setactual_size(existingDynamic.getactual_size() * 10);

        dao.update(existingDynamic);

        DiskImageDynamic result = dao.get(existingDynamic.getId());

        assertNotNull(result);
        assertEquals(existingDynamic, result);
    }

    /**
     * Ensures that updating a dynamic image works.
     */
    @Test
    public void testUpdateLatency() {
        existingDynamic.setReadLatency(0.000000001d);
        existingDynamic.setWriteLatency(0.000000002d);
        existingDynamic.setFlushLatency(0.999999999d);

        dao.update(existingDynamic);

        DiskImageDynamic result = dao.get(existingDynamic.getId());

        assertNotNull(result);
        assertEquals(existingDynamic, result);
    }

    @Test
    public void testSave() {
        DiskImage newImage = new DiskImage();
        newImage.setImageId(Guid.NewGuid());
        newImage.setvolumeFormat(VolumeFormat.COW);
        newImage.setVolumeType(VolumeType.Sparse);
        newImage.setDiskInterface(DiskInterface.IDE);
        newImage.setActive(true);
        newImage.setImageTemplateId(EXISTING_IMAGE_DISK_TEMPLATE);
        newImage.setId(Guid.NewGuid());
        imageDao.save(newImage.getImage());
        diskDao.save(newImage);
        DiskImageDynamic dynamic = new DiskImageDynamic();
        dynamic.setread_rate(5);
        dynamic.setwrite_rate(6);
        dynamic.setReadLatency(0d);
        dynamic.setFlushLatency(0.0202020d);
        dynamic.setWriteLatency(null);
        dynamic.setId(newImage.getImageId());
        dao.save(dynamic);
        DiskImageDynamic result = dao.get(dynamic.getId());
        assertNotNull(result);
        assertEquals(dynamic,result);
    }
    /**
     * Ensures that removing a dynamic image works as expected.
     */
    @Test
    public void testRemove() {
        dao.remove(existingDynamic.getId());

        DiskImageDynamic result = dao.get(existingDynamic.getId());

        assertNull(result);
    }

    @Test
    public void testUpdateAll() throws Exception {
        DiskImageDynamic existingDynamic2 = dao.get(new Guid("42058975-3d5e-484a-80c1-01c31207f579"));
        existingDynamic.setactual_size(100);
        existingDynamic2.setread_rate(120);
        existingDynamic.setReadLatency(100d);
        existingDynamic2.setReadLatency(0.00001d);

        dao.updateAll(Arrays.asList(new DiskImageDynamic[] { existingDynamic, existingDynamic2 }));

        assertEquals(existingDynamic, dao.get(existingDynamic.getId()));
        assertEquals(existingDynamic2, dao.get(existingDynamic2.getId()));
    }
}
