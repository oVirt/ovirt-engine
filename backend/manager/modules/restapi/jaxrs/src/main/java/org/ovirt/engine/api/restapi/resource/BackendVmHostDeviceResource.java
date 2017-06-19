package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.api.model.HostDevices;
import org.ovirt.engine.api.resource.VmHostDeviceResource;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmHostDevicesParameters;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmHostDeviceResource
    extends AbstractBackendSubResource<HostDevice, HostDeviceView>
    implements VmHostDeviceResource {

    private final BackendVmHostDevicesResource parent;
    private final String deviceName;

    protected BackendVmHostDeviceResource(String deviceId, BackendVmHostDevicesResource parent) {
        super(deviceId, HostDevice.class, HostDeviceView.class);
        this.parent = parent;
        this.deviceName = HexUtils.hex2string(deviceId);
    }

    @Override
    protected HostDevice addParents(HostDevice model) {
        return parent.addParents(model);
    }

    public BackendVmHostDevicesResource getParent() {
        return parent;
    }

    // We need to override this method because the native identifier of this
    // resource isn't an UUID but a device name.
    @Override
    protected Guid asGuidOr404(String id) {
        return null;
    }

    @Override
    public HostDevice get() {
        HostDevices devices = parent.list();
        return getHostDevice(devices);
    }

    private HostDevice getHostDevice(HostDevices devices) {
        for (HostDevice device : devices.getHostDevices()) {
            if (device.getId().equals(id)) {
                return device;
            }
        }
        return notFound();
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveVmHostDevices, new VmHostDevicesParameters(parent.getVmId(), deviceName));
    }
}
