package org.ovirt.engine.api.restapi.resource;

import org.junit.Test;
import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.queries.HostDeviceParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendHostDeviceResourceTest
        extends AbstractBackendHostDevicesResourceTest<BackendHostDeviceResource, org.ovirt.engine.core.common.businessentities.HostDevice> {

    public BackendHostDeviceResourceTest() {
        super(new BackendHostDeviceResource(HexUtils.string2hex(DEVICE_NAME), new BackendHostDevicesResource(HOST_ID)));
    }

    @Test
    public void testGet() throws Exception {
        setUpGetEntityExpectations(
                VdcQueryType.GetHostDeviceByHostIdAndDeviceName,
                HostDeviceParameters.class,
                new String[] { "HostId", "DeviceName" },
                new Object[] { HOST_ID, DEVICE_NAME },
                getEntity(0));

        control.replay();

        HostDevice device = resource.get();
        verifyHostDevice(device);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.HostDevice createDevice() {
        return new org.ovirt.engine.core.common.businessentities.HostDevice();
    }
}
