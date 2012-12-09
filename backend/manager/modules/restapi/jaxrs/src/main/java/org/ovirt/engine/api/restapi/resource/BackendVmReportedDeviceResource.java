package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.GET;

import org.ovirt.engine.api.model.Device;
import org.ovirt.engine.api.resource.VmReportedDeviceResource;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;

public class BackendVmReportedDeviceResource extends AbstractBackendSubResource<Device, VmGuestAgentInterface> implements VmReportedDeviceResource {

    private BackendVmReportedDevicesResource parent;

    public BackendVmReportedDeviceResource(String id, BackendVmReportedDevicesResource vmDevicesResource) {
        super(id, Device.class, VmGuestAgentInterface.class);
        this.parent = vmDevicesResource;
    }

    @GET
    @Override
    public Device get() {
        return parent.lookupReportedDevice(asGuid(id));
    }

    public BackendVmReportedDevicesResource getParent() {
        return parent;
    }

    @Override
    protected Device doPopulate(Device model, VmGuestAgentInterface entity) {
        return model;
    }
}
