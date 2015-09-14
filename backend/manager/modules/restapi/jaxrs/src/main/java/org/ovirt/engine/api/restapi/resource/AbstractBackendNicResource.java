package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendNicsResource.SUB_COLLECTIONS;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.resource.NicResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendNicResource extends BackendDeviceResource<Nic, Nics, VmNetworkInterface> implements NicResource {

    protected AbstractBackendNicResource(
            String id,
            AbstractBackendReadOnlyDevicesResource<Nic, Nics, VmNetworkInterface> collection,
            VdcActionType updateType,
            ParametersProvider<Nic, VmNetworkInterface> updateParametersProvider,
            String[] requiredUpdateFields,
            String... subCollections) {
        super(
            Nic.class,
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
        return inject(new BackendStatisticsResource<Nic, VmNetworkInterface>(entityType, guid, query));
    }

    @Override
    public Nic update(Nic resource) {
        validateParameters(resource, requiredUpdateFields);
        return performUpdate(resource, entityResolver, updateType, updateParametersProvider);
    }
}
