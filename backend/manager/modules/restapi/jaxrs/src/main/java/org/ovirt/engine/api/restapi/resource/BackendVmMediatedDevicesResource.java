package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.VmMediatedDevice;
import org.ovirt.engine.api.model.VmMediatedDevices;
import org.ovirt.engine.api.resource.VmMediatedDeviceResource;
import org.ovirt.engine.api.resource.VmMediatedDevicesResource;
import org.ovirt.engine.core.common.businessentities.VmMdevType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmMediatedDevicesResource
        extends AbstractBackendCollectionResource<VmMediatedDevice, VmMdevType>
        implements VmMediatedDevicesResource {

    private final Guid vmId;

    public BackendVmMediatedDevicesResource(Guid vmId) {
        super(VmMediatedDevice.class, VmMdevType.class);
        this.vmId = vmId;
    }

    @Override
    public VmMediatedDevices list() {
        return BackendMdevHelper.list(this, vmId);
    }

    @Override
    public Response add(VmMediatedDevice mdev) {
        return BackendMdevHelper.add(this, this::list, mdev, vmId, true);
    }

    @Override
    public VmMediatedDeviceResource getDeviceResource(String id) {
        return inject(new BackendVmMediatedDeviceResource(this, vmId, id));
    }

    @Override
    protected VmMediatedDevice addParents(VmMediatedDevice model) {
        model.setVm(new Vm());
        model.getVm().setId(vmId.toString());
        return model;
    }
}
