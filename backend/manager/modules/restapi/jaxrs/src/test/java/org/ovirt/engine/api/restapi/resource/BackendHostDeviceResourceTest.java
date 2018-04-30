package org.ovirt.engine.api.restapi.resource;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.queries.HostDeviceParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendHostDeviceResourceTest
        extends AbstractBackendHostDevicesResourceTest<BackendHostDeviceResource, org.ovirt.engine.core.common.businessentities.HostDevice> {

    public BackendHostDeviceResourceTest() {
        super(new BackendHostDeviceResource(HexUtils.string2hex(DEVICE_NAME), new BackendHostDevicesResource(HOST_ID)));
    }

    @Test
    public void testGet() {
        setUpGetEntityExpectations(
                QueryType.GetHostDeviceByHostIdAndDeviceName,
                HostDeviceParameters.class,
                new String[] { "HostId", "DeviceName" },
                new Object[] { HOST_ID, DEVICE_NAME },
                getEntity(0));


        HostDevice device = resource.get();
        verifyHostDevice(device);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.HostDevice createDevice() {
        return new org.ovirt.engine.core.common.businessentities.HostDevice();
    }
}
