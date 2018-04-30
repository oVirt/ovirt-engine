package org.ovirt.engine.api.restapi.resource;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmHostDevicesParameters;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendVmHostDevicesResourceTest
        extends AbstractBackendHostDevicesResourceTest<BackendVmHostDevicesResource, HostDeviceView> {

    public BackendVmHostDevicesResourceTest() {
        super(new BackendVmHostDevicesResource(VM_ID));
    }

    @Test
    public void testList() {
        setUpGetVmHostDevicesExpectations();

        verifyHostDevices(resource.list().getHostDevices());
    }

    @Test
    public void testAdd() {

        resource.setUriInfo(setUpBasicUriExpectations());

        setUpGetVmHostDevicesExpectations();

        setUpActionExpectations(
                ActionType.AddVmHostDevices,
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
