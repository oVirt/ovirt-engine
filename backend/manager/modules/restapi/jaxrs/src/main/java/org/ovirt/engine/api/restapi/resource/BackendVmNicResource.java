package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.resource.NicResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmNicResource extends BackendNicResource implements NicResource {

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
}
