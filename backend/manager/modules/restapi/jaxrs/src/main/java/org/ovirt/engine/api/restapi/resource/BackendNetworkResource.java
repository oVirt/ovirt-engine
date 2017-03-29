package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedVnicProfilesResource;
import org.ovirt.engine.api.resource.NetworkLabelsResource;
import org.ovirt.engine.api.resource.NetworkResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendNetworkResource
    extends AbstractBackendSubResource<Network, org.ovirt.engine.core.common.businessentities.network.Network>
    implements NetworkResource {

    public BackendNetworkResource(String id) {
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
                BackendNetworkResource.this.map(incoming, entity);
            return new AddNetworkStoragePoolParameters(entity.getDataCenterId(), updated);
        }
    }

    private class UpdatedNetworkResolver extends EntityIdResolver<Guid> {
        @Override
        public org.ovirt.engine.core.common.businessentities.network.Network lookupEntity(Guid ignore)
            throws BackendFailureException {
            // We already know the identifier of the network, so we can use it directly, ignoring the argument.
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
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                VdcQueryType.GetPermissionsForObject,
                new GetPermissionsForObjectParameters(guid),
                Network.class,
                VdcObjectType.Network));
    }

    @Override
    public AssignedVnicProfilesResource getVnicProfilesResource() {
        return inject(new BackendAssignedVnicProfilesResource(id));
    }

    @Override
    public NetworkLabelsResource getNetworkLabelsResource() {
        return inject(new BackendNetworkLabelsResource(asGuid(id)));
    }

    @Override
    protected Network addParents(Network model) {
        return BackendNetworkHelper.addParents(model);
    }
}
