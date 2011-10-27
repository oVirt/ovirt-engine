package org.ovirt.engine.api.restapi.resource;


import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.NetworkResource;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.network;

public class BackendNetworkResource
    extends AbstractBackendNetworkResource
    implements NetworkResource {

    public BackendNetworkResource(String id, BackendNetworksResource parent) {
        super(id, parent);
    }

    @Override
    public Network get() {
        network entity = parent.lookupNetwork(guid);
        if (entity == null) {
            return notFound();
        }
        Network network = map(entity);
        network.setDisplay(null);
        return addLinks(network);
    }

    @Override
    public Network update(Network incoming) {
        return performUpdate(incoming,
                             getParent().getNetworkIdResolver(),
                             VdcActionType.UpdateNetwork,
                             new UpdateParametersProvider());
    }

    protected class UpdateParametersProvider implements ParametersProvider<Network, network> {
        @Override
        public VdcActionParametersBase getParameters(Network incoming, network entity) {
            network updated = getMapper(modelType, network.class).map(incoming, entity);
            return new AddNetworkStoragePoolParameters(entity.getstorage_pool_id().getValue(), updated);
        }
    }
}
