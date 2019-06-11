package org.ovirt.engine.core.dao.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDaoTestCase;
import org.ovirt.engine.core.dao.FixturesTool;

public class StoageDeviceDaoTest extends BaseDaoTestCase<StorageDeviceDao > {

    private static final Guid NEW_STORAGE_DEVICE_ID = new Guid("00000000-0000-0000-0000-000000000003");
    private static final Guid EXISTING_STORAGE_DEVICE_ID_1 = new Guid("00000000-0000-0000-0000-000000000001");
    private static final Guid EXISTING_STORAGE_DEVICE_ID_2 = new Guid("00000000-0000-0000-0000-000000000002");
    private static final Guid NON_EXISTING_STORAGE_DEVICE_ID = new Guid("00000000-0000-0000-0000-000000000000");

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

    @Test
    public void testGetById(){
        StorageDevice storageDevice = dao.get(EXISTING_STORAGE_DEVICE_ID_1);
        assertNotNull(storageDevice, "Failed to retrive storage device");
        assertEquals(EXISTING_STORAGE_DEVICE_ID_1, storageDevice.getId(), "Failed to retrive corrective storage device");

        storageDevice = dao.get(NON_EXISTING_STORAGE_DEVICE_ID);
        assertNull(storageDevice);
    }

    @Test
    public void testSave() {
        StorageDevice storageDevice = getStorageDevice();
        dao.save(storageDevice);
        StorageDevice storageDeviceFromDB = dao.get(storageDevice.getId());
        assertEquals(storageDevice, storageDeviceFromDB, "Storage device is not saved correctly");
    }

    @Test
    public void testGetStorageDevicesInHost() {
        List<StorageDevice> storageDevices = dao.getStorageDevicesInHost(FixturesTool.GLUSTER_BRICK_SERVER1);
        assertEquals(2, storageDevices.size(), "Fails to retrive all the storage devices for host");
    }

    @Test
    public void testRemove() {
        StorageDevice storageDevice = dao.get(EXISTING_STORAGE_DEVICE_ID_2);
        assertNotNull(storageDevice, "storage device doesn't exists");
        dao.remove(EXISTING_STORAGE_DEVICE_ID_2);
        storageDevice = dao.get(EXISTING_STORAGE_DEVICE_ID_2);
        assertNull(storageDevice, "Failed to remove storage device");
    }

    @Test
    public void testUpdateStorageDevice() {
        StorageDevice storageDevice = dao.get(EXISTING_STORAGE_DEVICE_ID_2);
        assertNotNull(storageDevice, "storage device doesn't exists");
        storageDevice.setSize(1234567L);
        storageDevice.setMountPoint("/gluster_bricks/brick1");
        storageDevice.setFsType("xfs");
        dao.update(storageDevice);
        StorageDevice storageDeviceFromDB = dao.get(EXISTING_STORAGE_DEVICE_ID_2);
        assertEquals(storageDevice, storageDeviceFromDB, "Failed to update Storage Device");
    }

    @Test
    public void updateIsFreeFlag() {
        StorageDevice storageDevice = dao.get(EXISTING_STORAGE_DEVICE_ID_2);
        assertNotNull(storageDevice, "storage device doesn't exists");
        dao.updateIsFreeFlag(EXISTING_STORAGE_DEVICE_ID_2, false);
        storageDevice = dao.get(EXISTING_STORAGE_DEVICE_ID_2);
        assertFalse(storageDevice.getCanCreateBrick(), "canCreateBrick is not updated");
        dao.updateIsFreeFlag(EXISTING_STORAGE_DEVICE_ID_2, true);
        storageDevice = dao.get(EXISTING_STORAGE_DEVICE_ID_2);
        assertTrue(storageDevice.getCanCreateBrick(), "canCreateBrick is not updated");
    }
}
