package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.HostDevice;
import org.ovirt.engine.api.resource.HostDeviceResource;
import org.ovirt.engine.api.restapi.utils.HexUtils;
import org.ovirt.engine.core.common.queries.HostDeviceParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostDeviceResource
        extends AbstractBackendSubResource<HostDevice, org.ovirt.engine.core.common.businessentities.HostDevice>
        implements HostDeviceResource {

    private final BackendHostDevicesResource parent;

    private final String deviceName;

    protected BackendHostDeviceResource(String deviceId, BackendHostDevicesResource parent) {
        super(deviceId, HostDevice.class, org.ovirt.engine.core.common.businessentities.HostDevice.class);
        this.parent = parent;
        this.deviceName = HexUtils.hex2string(deviceId);
    }

    @Override
    protected HostDevice addParents(HostDevice model) {
        return parent.addParents(model);
    }

    public BackendHostDevicesResource getParent() {
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
        return performGet(QueryType.GetHostDeviceByHostIdAndDeviceName, new HostDeviceParameters(parent.getHostId(), deviceName));
    }
}
