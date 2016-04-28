package org.ovirt.engine.api.restapi.resource;

import java.util.Collections;

import org.junit.Test;
import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmHostDevicesParameters;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;

public class BackendVmHostDevicesResourceTest
        extends AbstractBackendHostDevicesResourceTest<BackendVmHostDevicesResource, HostDeviceView> {

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
                new Object[] { VM_ID, Collections.singletonList(DEVICE_NAME)},
                true, true);

        HostDevice device = new HostDevice();
        device.setName(DEVICE_NAME);
        resource.add(device);
    }

    @Override
    protected HostDeviceView createDevice() {
        return new HostDeviceView();
    }
}
