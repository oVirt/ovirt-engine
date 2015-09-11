package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendNicsResource.SUB_COLLECTIONS;

import org.ovirt.engine.api.model.NIC;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.resource.NicResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendNicResource extends BackendDeviceResource<NIC, Nics, VmNetworkInterface> implements NicResource {

    protected AbstractBackendNicResource(
            String id,
            AbstractBackendReadOnlyDevicesResource<NIC, Nics, VmNetworkInterface> collection,
            VdcActionType updateType,
            ParametersProvider<NIC, VmNetworkInterface> updateParametersProvider,
            String[] requiredUpdateFields,
            String... subCollections) {
        super(
            NIC.class,
            VmNetworkInterface.class,
            collection.asGuidOr404(id),
            collection,
            updateType,
            updateParametersProvider,
            requiredUpdateFields,
            SUB_COLLECTIONS
        );
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        EntityIdResolver<Guid> resolver = new EntityIdResolver<Guid>() {
            @Override
            public VmNetworkInterface lookupEntity(Guid guid)
                    throws BackendFailureException {
                return collection.lookupEntity(guid);
            }
        };
        NicStatisticalQuery query = new NicStatisticalQuery(resolver, newModel(id));
        return inject(new BackendStatisticsResource<NIC, VmNetworkInterface>(entityType, guid, query));
    }

    @Override
    public NIC update(NIC resource) {
        validateParameters(resource, requiredUpdateFields);
        return performUpdate(resource, entityResolver, updateType, updateParametersProvider);
    }
}
