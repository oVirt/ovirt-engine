package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.VmNicResource;
import org.ovirt.engine.api.resource.VmReportedDevicesResource;
import org.ovirt.engine.core.common.action.RemoveVmInterfaceParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmNicResource extends AbstractBackendNicResource implements VmNicResource {

    private Guid vmId;

    protected BackendVmNicResource(
            Guid vmId,
            String nicId,
            AbstractBackendReadOnlyDevicesResource<NIC, Nics, VmNetworkInterface> collection,
            VdcActionType updateType,
            ParametersProvider<NIC, VmNetworkInterface> updateParametersProvider,
            String[] requiredUpdateFields,
            String[] subCollections) {
        super(
            nicId,
            collection,
            updateType,
            updateParametersProvider,
            requiredUpdateFields,
            subCollections
        );
        this.vmId = vmId;
    }

    @Override
    protected NIC doPopulate(NIC model, VmNetworkInterface entity) {
        BackendVmNicsResource parent = (BackendVmNicsResource) collection;
        parent.addReportedDevices(model, entity);
        return model;
    }

    @Override
    protected NIC deprecatedPopulate(NIC model, VmNetworkInterface entity) {
        BackendVmNicsResource parent = (BackendVmNicsResource) collection;
        parent.addStatistics(model, entity);
        return model;
    }

    @Override
    public NIC update(NIC device) {
        validateEnums(NIC.class, device);
        return super.update(device);
    }

    @Override
    public ActionResource getActionSubresource(String action, String oid) {
        return null;
    }

    @Override
    public Response activate(Action action) {
        NIC nic = get();
        nic.setPlugged(true);
        update(nic);
        return actionSuccess(action);
    }

    @Override
    public Response deactivate(Action action) {
        NIC nic = get();
        nic.setPlugged(false);
        update(nic);
        return actionSuccess(action);
    }

    @Override
    public NIC get() {
        return super.get();//explicit call solves REST-Easy confusion
    }

    @Override
    public VmReportedDevicesResource getVmReportedDevicesResource() {
        return inject(new BackendVmReportedDevicesResource(guid));
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveVmInterface, new RemoveVmInterfaceParameters(vmId, guid));
    }
}
