package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.GET;

import org.ovirt.engine.api.model.ReportedDevice;
import org.ovirt.engine.api.resource.VmReportedDeviceResource;
import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;

public class BackendVmReportedDeviceResource extends AbstractBackendSubResource<ReportedDevice, VmGuestAgentInterface> implements VmReportedDeviceResource {

    private BackendVmReportedDevicesResource parent;

    public BackendVmReportedDeviceResource(String id, BackendVmReportedDevicesResource vmDevicesResource) {
        super(id, ReportedDevice.class, VmGuestAgentInterface.class);
        this.parent = vmDevicesResource;
    }

    @GET
    @Override
    public ReportedDevice get() {
        return parent.lookupReportedDevice(asGuid(id));
    }

    public BackendVmReportedDevicesResource getParent() {
        return parent;
    }

    @Override
    protected ReportedDevice doPopulate(ReportedDevice model, VmGuestAgentInterface entity) {
        return model;
    }
}
