package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.VmMediatedDevice;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.VmMediatedDeviceResource;
import org.ovirt.engine.api.restapi.types.VmMapper;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MdevParameters;
import org.ovirt.engine.core.common.businessentities.VmMdevType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmMediatedDeviceResource extends AbstractBackendActionableResource<VmMediatedDevice, VmMdevType>
        implements VmMediatedDeviceResource {

    private BackendVmMediatedDevicesResource parent;
    private Guid vmId;
    private String mdevId;

    public BackendVmMediatedDeviceResource(BackendVmMediatedDevicesResource parent, Guid vmId, String mdevId) {
        super(mdevId, VmMediatedDevice.class, VmMdevType.class);
        this.parent = parent;
        this.vmId = vmId;
    }

    @Override
    public ActionResource getActionResource(String action, String oid) {
        return inject(new BackendActionResource(action, oid));
    }

    @Override
    public VmMediatedDevice get() {
        return BackendMdevHelper.get(parent::list, id);
    }

    @Override
    public Response remove() {
        return BackendMdevHelper.remove(this, vmId, id, true);
    }

    @Override
    public VmMediatedDevice update(VmMediatedDevice mdev) {
        return performUpdate(mdev, new VmMediatedDeviceResolver(), ActionType.UpdateMdev,
                new UpdateParametersProvider());
    }

    private class UpdateParametersProvider implements ParametersProvider<VmMediatedDevice, VmMdevType> {
        @Override
        public ActionParametersBase getParameters(VmMediatedDevice model, VmMdevType entity) {
            VmMdevType template = new VmMdevType(entity);
            return new MdevParameters(VmMapper.map(model, template), true);
        }
    }

    private class VmMediatedDeviceResolver extends EntityIdResolver<Guid> {
        @Override
        public VmMdevType lookupEntity(Guid guid) throws BackendFailureException {
            return BackendMdevHelper.lookup(BackendVmMediatedDeviceResource.this, vmId, id, true);
        }
    }
}
