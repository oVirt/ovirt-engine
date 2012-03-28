package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.VmNicResource;
import org.ovirt.engine.core.common.HotPlugUnplugVmNicParameters;
import org.ovirt.engine.core.common.PlugAction;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmNicResource extends BackendNicResource implements VmNicResource {

    protected BackendVmNicResource(String id,
                                   AbstractBackendReadOnlyDevicesResource<NIC, Nics, VmNetworkInterface> collection,
                                   VdcActionType updateType,
                                   ParametersProvider<NIC, VmNetworkInterface> updateParametersProvider,
                                   String[] requiredUpdateFields,
                                   String[] subCollections) {
        super(id, collection, updateType, updateParametersProvider, requiredUpdateFields, subCollections);
    }

    @Override
    protected NIC populate(NIC model, VmNetworkInterface entity) {
        BackendVmNicsResource parent = (BackendVmNicsResource)collection;
        Guid clusterId = parent.getClusterId();
        network network = parent.lookupClusterNetwork(clusterId, null, model.getNetwork().getName());
        model.getNetwork().setId(network.getId().toString());
        model.getNetwork().setName(null);
        return parent.addStatistics(model, entity, uriInfo, httpHeaders);
    }

    @Override
    public ActionResource getActionSubresource(String action, String oid) {
        return null;
    }

    @Override
    public Response activate(Action action) {
        HotPlugUnplugVmNicParameters params = new HotPlugUnplugVmNicParameters(guid, PlugAction.PLUG);
        BackendNicsResource parent = (BackendNicsResource) collection;
        params.setVmId(parent.parentId);
        return performAction(VdcActionType.HotPlugUnplugVmNic, params);
    }

    @Override
    public Response deactivate(Action action) {
        HotPlugUnplugVmNicParameters params = new HotPlugUnplugVmNicParameters(guid, PlugAction.UNPLUG);
        params.setVmId(((BackendNicsResource) collection).parentId);
        return performAction(VdcActionType.HotPlugUnplugVmNic, params);
    }
}
