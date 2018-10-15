package org.ovirt.engine.api.restapi.resource;


import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AssignedVnicProfilesResource;
import org.ovirt.engine.api.resource.NetworkLabelsResource;
import org.ovirt.engine.api.resource.NetworkResource;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendNetworkResource extends AbstractBackendNetworkResource implements NetworkResource {

    public BackendNetworkResource(String id, BackendNetworksResource parent) {
        super(id, parent, ActionType.RemoveNetwork);
    }

    @Override
    public Network get() {
        org.ovirt.engine.core.common.businessentities.network.Network entity = parent.lookupNetwork(guid);
        if (entity == null) {
            return notFound();
        }
        Network network = map(entity);
        network.setDisplay(null);
        return addLinks(network, LinkHelper.NO_PARENT);
    }

    @Override
    protected Network addParents(Network model) {
        return parent.addParents(model);
    }

    @Override
    public Network update(Network incoming) {
        return performUpdate(incoming,
                             getParent().getNetworkIdResolver(),
                             ActionType.UpdateNetwork,
                             new UpdateParametersProvider());
    }

    protected class UpdateParametersProvider implements ParametersProvider<Network, org.ovirt.engine.core.common.businessentities.network.Network> {
        @Override
        public ActionParametersBase getParameters(Network incoming, org.ovirt.engine.core.common.businessentities.network.Network entity) {
            org.ovirt.engine.core.common.businessentities.network.Network updated = getMapper(modelType, org.ovirt.engine.core.common.businessentities.network.Network.class).map(incoming, entity);
            return new AddNetworkStoragePoolParameters(entity.getDataCenterId(), updated);
        }
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                QueryType.GetPermissionsForObject,
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
    protected ActionParametersBase getRemoveParameters(org.ovirt.engine.core.common.businessentities.network.Network entity) {
        return new RemoveNetworkParameters(entity.getId());
    }
}
