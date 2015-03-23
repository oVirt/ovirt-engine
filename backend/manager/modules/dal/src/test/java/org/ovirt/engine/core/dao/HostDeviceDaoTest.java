package org.ovirt.engine.core.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.HostDeviceId;
import org.ovirt.engine.core.compat.Guid;

public class HostDeviceDaoTest extends BaseGenericDaoTestCase<HostDeviceId, HostDevice, HostDeviceDao> {
    public static final Guid EXISTING_HOST_ID = new Guid("afce7a39-8e8c-4819-ba9c-796d316592e6");
    public static final String EXISTING_DEVICE_NAME = "pci_0000_00_1d_2";

    @Override
    protected HostDevice generateNewEntity() {
        HostDevice device = new HostDevice();
        device.setHostId(EXISTING_HOST_ID);
        device.setDeviceName(EXISTING_DEVICE_NAME + "___child");
        device.setParentDeviceName(EXISTING_DEVICE_NAME);
        device.setCapability("pci");

        return device;
    }

    @Override
    protected void updateExistingEntity() {
        existingEntity.setIommuGroup(null);
        existingEntity.setProductName("device upgrade");
    }

    @Override
    protected HostDeviceId getExistingEntityId() {
        return new HostDeviceId(EXISTING_HOST_ID, EXISTING_DEVICE_NAME);
    }

    @Override
    protected HostDeviceDao prepareDao() {
        return dbFacade.getHostDeviceDao();
    }

    @Override
    protected HostDeviceId generateNonExistingId() {
        return new HostDeviceId(Guid.newGuid(), "this_device_probably_doesnt_exist");
    }

    @Override
    protected int getEneitiesTotalCount() {
        return 36;
    }

    @Test
    public void saveNetworkDevice() {
        HostDevice netDevice = generateNewEntity();

        netDevice.setCapability("net");
        netDevice.setNetworkInterfaceName("eth1");

        dao.save(netDevice);

        HostDevice result = dao.get(netDevice.getId());

        assertNotNull(result);
        assertEquals(netDevice, result);
    }

    @Test
    public void updateNetworkDevice() {
        HostDeviceId netDeviceId =
                new HostDeviceId(FixturesTool.NETWORK_HOST_DEVICE_HOST_ID, FixturesTool.NETWORK_HOST_DEVICE_NAME);
        HostDevice before = dao.get(netDeviceId);

        before.setNetworkInterfaceName(before.getNetworkInterfaceName() + "new");

        dao.update(before);

        HostDevice after = dao.get(netDeviceId);

        assertNotNull(after);
        assertEquals(before, after);
    }
}
