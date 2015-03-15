package org.ovirt.engine.core.dao;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;


public class DiskImageDynamicDAOTest extends BaseDAOTestCase{
    private static final Guid EXISTING_IMAGE_ID = new Guid("42058975-3d5e-484a-80c1-01c31207f578");
    private static final int TOTAL_DYNAMIC_DISK_IMAGES = 5;
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
        DiskImageDynamic result = dao.get(Guid.newGuid());

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
        newImage.setImageId(Guid.newGuid());
        newImage.setvolumeFormat(VolumeFormat.COW);
        newImage.setVolumeType(VolumeType.Sparse);
        newImage.setDiskInterface(DiskInterface.IDE);
        newImage.setActive(true);
        newImage.setImageTemplateId(EXISTING_IMAGE_DISK_TEMPLATE);
        newImage.setId(Guid.newGuid());
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
        assertEquals(dynamic, result);
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

    @Test
    public void updateAllDiskImageDynamicWithDiskIdByVmId() throws Exception {
        Guid imageId = FixturesTool.IMAGE_ID_2;
        Guid imageGroupId = FixturesTool.IMAGE_GROUP_ID_2;

        DiskImageDynamic existingDynamic2 = dao.get(imageId);
        assertFalse(existingDynamic2.getread_rate().equals(120));

        VmDevice device = dbFacade.getVmDeviceDao().get(new VmDeviceId(imageGroupId, FixturesTool.VM_RHEL5_POOL_57));
        assertNull(device.getSnapshotId());

        existingDynamic2.setId(imageGroupId);
        Integer readRate = new Integer(120);
        existingDynamic2.setread_rate(readRate);

        // test that the record is updated when the active disk is attached to the vm
        dao.updateAllDiskImageDynamicWithDiskIdByVmId(Arrays.<Pair<Guid, DiskImageDynamic>> asList(new Pair(FixturesTool.VM_RHEL5_POOL_57,
                existingDynamic2)));

        existingDynamic2.setId(imageId);
        assertEquals(existingDynamic2, dao.get(imageId));

        // test that the record isn't updated when a snapshot of the disk is attached to the vm
        device.setSnapshotId(FixturesTool.EXISTING_SNAPSHOT_ID);
        dbFacade.getVmDeviceDao().update(device);

        existingDynamic2.setread_rate(150);
        dao.updateAllDiskImageDynamicWithDiskIdByVmId(Arrays.<Pair<Guid, DiskImageDynamic>>asList(new Pair(FixturesTool.VM_RHEL5_POOL_57,
                existingDynamic2)));
        assertEquals(readRate, dao.get(imageId).getread_rate());
    }
}
