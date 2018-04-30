package org.ovirt.engine.api.restapi.resource;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmHostDevicesParameters;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendVmHostDeviceResourceTest
        extends AbstractBackendHostDevicesResourceTest<BackendVmHostDeviceResource, HostDeviceView> {

    public BackendVmHostDeviceResourceTest() {
        super(new BackendVmHostDeviceResource(HexUtils.string2hex(DEVICE_NAME), new BackendVmHostDevicesResource(VM_ID)));
    }

    @Override
    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Test
    public void testGet() {
        setUpGetVmHostDevicesExpectations();

        HostDevice device = resource.get();
        verifyHostDevice(device);
    }

    @Test
    public void testRemove() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetVmHostDevicesExpectations();
        setUpActionExpectations(
                ActionType.RemoveVmHostDevices,
                VmHostDevicesParameters.class,
                new String[] { "VmId", "DeviceNames" },
                new Object[] { VM_ID, Collections.singletonList(DEVICE_NAME)},
                true, true);

        verifyRemove(resource.remove());
    }

    @Override
    protected HostDeviceView createDevice() {
        return new HostDeviceView();
    }
}
