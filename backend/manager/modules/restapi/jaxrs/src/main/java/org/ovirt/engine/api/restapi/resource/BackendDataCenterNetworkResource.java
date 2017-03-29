package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.DataCenterNetworkResource;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDataCenterNetworkResource
    extends AbstractBackendSubResource<Network, org.ovirt.engine.core.common.businessentities.network.Network>
    implements DataCenterNetworkResource {

    public BackendDataCenterNetworkResource(String id) {
        super(id, Network.class, org.ovirt.engine.core.common.businessentities.network.Network.class);
    }

    @Override
    public Network get() {
        return performGet(VdcQueryType.GetNetworkById, new IdQueryParameters(guid));
    }

    @Override
    public Network update(Network incoming) {
        return performUpdate(
            incoming,
            new UpdatedNetworkResolver(),
            VdcActionType.UpdateNetwork,
            new UpdateParametersProvider()
        );
    }

    private class UpdateParametersProvider
        implements ParametersProvider<Network, org.ovirt.engine.core.common.businessentities.network.Network> {

        @Override
        public VdcActionParametersBase getParameters(Network incoming,
                org.ovirt.engine.core.common.businessentities.network.Network entity) {
            org.ovirt.engine.core.common.businessentities.network.Network updated =
                BackendDataCenterNetworkResource.this.map(incoming, entity);
            return new AddNetworkStoragePoolParameters(entity.getDataCenterId(), updated);
        }
    }

    private class UpdatedNetworkResolver extends EntityIdResolver<Guid> {
        @Override
        public org.ovirt.engine.core.common.businessentities.network.Network lookupEntity(Guid ignore)
            throws BackendFailureException {
            return getEntity(
                org.ovirt.engine.core.common.businessentities.network.Network.class,
                VdcQueryType.GetNetworkById,
                new IdQueryParameters(guid),
                id
            );
        }
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.RemoveNetwork, new RemoveNetworkParameters(guid));
    }

    @Override
    protected Network addParents(Network model) {
        return BackendNetworkHelper.addParents(model);
    }
}
