package org.ovirt.engine.core.dao.gluster;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class StoageDeviceDaoTest extends BaseDaoTestCase {

    private static final Guid NEW_STORAGE_DEVICE_ID = new Guid("00000000-0000-0000-0000-000000000003");
    private static final Guid EXISTING_STORAGE_DEVICE_ID_1 = new Guid("00000000-0000-0000-0000-000000000001");
    private static final Guid EXISTING_STORAGE_DEVICE_ID_2 = new Guid("00000000-0000-0000-0000-000000000002");
    private static final Guid NON_EXISTING_STORAGE_DEVICE_ID = new Guid("00000000-0000-0000-0000-000000000000");

    private StorageDeviceDao dao;

    private StorageDevice getStorageDevice() {
        StorageDevice storageDevice = new StorageDevice();
        storageDevice.setId(NEW_STORAGE_DEVICE_ID);
        storageDevice.setCanCreateBrick(true);
        storageDevice.setDescription("Test Device");
        storageDevice.setDevPath("/dev/sdc");
        storageDevice.setDevType("SCSI");
        storageDevice.setDevUuid("ocIYJv-Ej8x-vDPm-kcGr-sHqy-jjeo-Jt2hTj");
        storageDevice.setName("sdc");
        storageDevice.setSize(10000L);
        storageDevice.setVdsId(FixturesTool.GLUSTER_BRICK_SERVER1);
        return storageDevice;

    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        dao = dbFacade.getStorageDeviceDao();
    }

    @Test
    public void testGetById(){
        StorageDevice storageDevice = dao.get(EXISTING_STORAGE_DEVICE_ID_1);
        assertNotNull("Failed to retrive storage device", storageDevice);
        assertEquals("Failed to retrive corrective storage device", EXISTING_STORAGE_DEVICE_ID_1, storageDevice.getId());

        storageDevice = dao.get(NON_EXISTING_STORAGE_DEVICE_ID);
        assertNull(storageDevice);
    }

    @Test
    public void testSave() {
        StorageDevice storageDevice = getStorageDevice();
        dao.save(storageDevice);
        StorageDevice storageDeviceFromDB = dao.get(storageDevice.getId());
        assertEquals("Storage device is not saved correctly", storageDevice, storageDeviceFromDB);
    }

    @Test
    public void testGetStorageDevicesInHost() {
        List<StorageDevice> storageDevices = dao.getStorageDevicesInHost(FixturesTool.GLUSTER_BRICK_SERVER1);
        assertEquals("Fails to retrive all the storage devices for host", 2, storageDevices.size());
    }

    @Test
    public void testRemove() {
        StorageDevice storageDevice = dao.get(EXISTING_STORAGE_DEVICE_ID_2);
        assertNotNull("storage device doesn't exists", storageDevice);
        dao.remove(EXISTING_STORAGE_DEVICE_ID_2);
        storageDevice = dao.get(EXISTING_STORAGE_DEVICE_ID_2);
        assertNull("Failed to remove storage device", storageDevice);
    }

    @Test
    public void testUpdateStorageDevice() {
        StorageDevice storageDevice = dao.get(EXISTING_STORAGE_DEVICE_ID_2);
        assertNotNull("storage device doesn't exists", storageDevice);
        storageDevice.setSize(1234567L);
        storageDevice.setMountPoint("/gluster-bricks/brick1");
        storageDevice.setFsType("xfs");
        dao.update(storageDevice);
        StorageDevice storageDeviceFromDB = dao.get(EXISTING_STORAGE_DEVICE_ID_2);
        assertEquals("Failed to update Storage Device", storageDevice, storageDeviceFromDB);
    }

    @Test
    public void updateIsFreeFlag() {
        StorageDevice storageDevice = dao.get(EXISTING_STORAGE_DEVICE_ID_2);
        assertNotNull("storage device doesn't exists", storageDevice);
        dao.updateIsFreeFlag(EXISTING_STORAGE_DEVICE_ID_2, false);
        storageDevice = dao.get(EXISTING_STORAGE_DEVICE_ID_2);
        assertFalse("canCreateBrick is not updated", storageDevice.getCanCreateBrick());
        dao.updateIsFreeFlag(EXISTING_STORAGE_DEVICE_ID_2, true);
        storageDevice = dao.get(EXISTING_STORAGE_DEVICE_ID_2);
        assertTrue("canCreateBrick is not updated", storageDevice.getCanCreateBrick());
    }
}
