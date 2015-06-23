package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.resource.NicResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

public class BackendTemplateNicResource extends BackendNicResource implements NicResource {

    protected BackendTemplateNicResource(String id,
            AbstractBackendReadOnlyDevicesResource<NIC, Nics, VmNetworkInterface> collection,
            VdcActionType updateType,
            ParametersProvider<NIC, VmNetworkInterface> updateParametersProvider,
            String[] requiredUpdateFields,
            String... subCollections) {
        super(id, collection, updateType, updateParametersProvider, requiredUpdateFields, subCollections);
    }

    @Override
    protected NIC deprecatedPopulate(NIC model, VmNetworkInterface entity) {
        Network network = findNetwork(model);
        if (network != null) {
            model.getNetwork().setId(network.getId().toString());
            model.getNetwork().setName(null);
        }
        return model;
    }

}
