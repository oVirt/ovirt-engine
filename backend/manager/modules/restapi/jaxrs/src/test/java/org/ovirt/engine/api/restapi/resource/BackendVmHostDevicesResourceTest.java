package org.ovirt.engine.api.restapi.resource;

import org.junit.Test;
import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmHostDevicesParameters;

import java.util.Arrays;

public class BackendVmHostDevicesResourceTest
        extends AbstractBackendHostDevicesResourceTest<BackendVmHostDevicesResource> {

    public BackendVmHostDevicesResourceTest() {
        super(new BackendVmHostDevicesResource(VM_ID));
    }

    @Test
    public void testList() throws Exception {
        setUpGetVmHostDevicesExpectations();

        control.replay();

        verifyHostDevices(resource.list().getHostDevices());
    }

    @Test
    public void testAdd() throws Exception {

        resource.setUriInfo(setUpBasicUriExpectations());

        setUpGetVmHostDevicesExpectations();

        setUpActionExpectations(
                VdcActionType.AddVmHostDevices,
                VmHostDevicesParameters.class,
                new String[] { "VmId", "DeviceNames" },
                new Object[] { VM_ID, Arrays.asList(DEVICE_NAME) },
                true, true);

        HostDevice device = new HostDevice();
        device.setName(DEVICE_NAME);
        resource.add(device);
    }
}
