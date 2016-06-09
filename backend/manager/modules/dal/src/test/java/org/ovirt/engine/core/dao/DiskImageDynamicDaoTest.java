package org.ovirt.engine.core.dao;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class DiskImageDynamicDaoTest extends BaseDaoTestCase{
    private static final Guid EXISTING_IMAGE_ID = new Guid("42058975-3d5e-484a-80c1-01c31207f578");
    private static final int TOTAL_DYNAMIC_DISK_IMAGES = 5;
    private static final Guid EXISTING_IMAGE_DISK_TEMPLATE = new Guid("42058975-3d5e-484a-80c1-01c31207f578");



    private DiskImageDynamicDao dao;
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
        existingDynamic.setActualSize(existingDynamic.getActualSize() * 10);

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

    public DiskImageDynamic createDiskImageDynamic(Guid id) {
        DiskImageDynamic dynamic = new DiskImageDynamic();
        dynamic.setReadRate(5);
        dynamic.setWriteRate(6);
        dynamic.setReadLatency(0d);
        dynamic.setFlushLatency(0.0202020d);
        dynamic.setWriteLatency(null);
        dynamic.setId(id);
        return dynamic;
    }

    @Test
    public void testSave() {
        DiskImage newImage = new DiskImage();
        newImage.setImageId(Guid.newGuid());
        newImage.setVolumeFormat(VolumeFormat.COW);
        newImage.setVolumeType(VolumeType.Sparse);
        newImage.setActive(true);
        newImage.setImageTemplateId(EXISTING_IMAGE_DISK_TEMPLATE);
        newImage.setId(Guid.newGuid());
        imageDao.save(newImage.getImage());
        diskDao.save(newImage);
        DiskImageDynamic dynamic = createDiskImageDynamic(newImage.getImageId());
        dao.save(createDiskImageDynamic(newImage.getImageId()));
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
        existingDynamic.setActualSize(100);
        existingDynamic2.setReadRate(120);
        existingDynamic.setReadLatency(100d);
        existingDynamic2.setReadLatency(0.00001d);

        dao.updateAll(Arrays.asList(existingDynamic, existingDynamic2));

        assertEquals(existingDynamic, dao.get(existingDynamic.getId()));
        assertEquals(existingDynamic2, dao.get(existingDynamic2.getId()));
    }

    @Test
    public void updateAllDiskImageDynamicWithDiskIdByVmId() throws Exception {
        Guid imageId = FixturesTool.IMAGE_ID_2;
        Guid imageGroupId = FixturesTool.IMAGE_GROUP_ID_2;

        DiskImageDynamic existingDynamic2 = dao.get(imageId);
        assertFalse(existingDynamic2.getReadRate().equals(120));

        VmDevice device = dbFacade.getVmDeviceDao().get(new VmDeviceId(imageGroupId, FixturesTool.VM_RHEL5_POOL_57));
        assertNull(device.getSnapshotId());

        existingDynamic2.setId(imageGroupId);
        Integer readRate = 120;
        existingDynamic2.setReadRate(readRate);

        // test that the record is updated when the active disk is attached to the vm
        dao.updateAllDiskImageDynamicWithDiskIdByVmId(Collections.singleton(new Pair<>(FixturesTool.VM_RHEL5_POOL_57,
                existingDynamic2)));

        existingDynamic2.setId(imageId);
        assertEquals(existingDynamic2, dao.get(imageId));

        // test that the record isn't updated when a snapshot of the disk is attached to the vm
        device.setSnapshotId(FixturesTool.EXISTING_SNAPSHOT_ID);
        dbFacade.getVmDeviceDao().update(device);

        existingDynamic2.setReadRate(150);
        dao.updateAllDiskImageDynamicWithDiskIdByVmId(Collections.singleton(new Pair<>(FixturesTool.VM_RHEL5_POOL_57,
                existingDynamic2)));
        assertEquals(readRate, dao.get(imageId).getReadRate());
    }

    @Test
    public void sortDiskImageDynamicForUpdate() throws Exception {
        Guid firstGuid = Guid.Empty;
        Guid secondGuid = Guid.createGuidFromString("11111111-1111-1111-1111-111111111111");
        Guid thirdGuid = Guid.createGuidFromString("22222222-2222-2222-2222-222222222222");
        List<Pair<Guid, DiskImageDynamic>> diskImageDynamicForVm = new LinkedList<>();
        diskImageDynamicForVm.add(new Pair<>(Guid.Empty, createDiskImageDynamic(thirdGuid)));
        diskImageDynamicForVm.add(new Pair<>(Guid.Empty, createDiskImageDynamic(secondGuid)));
        diskImageDynamicForVm.add(new Pair<>(Guid.Empty, createDiskImageDynamic(firstGuid)));
        List<Pair<Guid, DiskImageDynamic>> sortedList =
                ((DiskImageDynamicDaoImpl)dao).sortDiskImageDynamicForUpdate(diskImageDynamicForVm);
        Collections.reverse(diskImageDynamicForVm);
        assertEquals(diskImageDynamicForVm, sortedList);
    }
}
