package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.DataCenterNetworkResource;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;

public class BackendDataCenterNetworkResource
    extends AbstractBackendNetworkResource
    implements DataCenterNetworkResource {

    private BackendDataCenterNetworksResource parent;

    public BackendDataCenterNetworkResource(String id, BackendDataCenterNetworksResource parent) {
        super(id, parent, ActionType.RemoveNetwork);
        this.parent = parent;
    }

    @Override
    public Network get() {
        org.ovirt.engine.core.common.businessentities.network.Network entity = getNetwork();
        if (entity == null) {
            return notFound();
        }
        return addLinks(map(entity), LinkHelper.NO_PARENT);
    }

    private org.ovirt.engine.core.common.businessentities.network.Network getNetwork() {
        List<org.ovirt.engine.core.common.businessentities.network.Network> networks = parent.getNetworks();
        for (org.ovirt.engine.core.common.businessentities.network.Network entity : networks) {
            if (entity.getId().toString().equals(id)) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public BackendDataCenterNetworksResource getParent() {
        return parent;
    }

    @Override
    public Network update(Network incoming) {
        return performUpdate(
            incoming,
            getParent().getNetworkIdResolver(),
            ActionType.UpdateNetwork,
            new UpdateParametersProvider()
        );
    }

    private class UpdateParametersProvider
        implements ParametersProvider<Network, org.ovirt.engine.core.common.businessentities.network.Network> {

        @Override
        public ActionParametersBase getParameters(
                Network incoming,
                org.ovirt.engine.core.common.businessentities.network.Network entity) {
            org.ovirt.engine.core.common.businessentities.network.Network updated = getMapper(
                Network.class,
                org.ovirt.engine.core.common.businessentities.network.Network.class
            ).map(incoming, entity);
            return new AddNetworkStoragePoolParameters(entity.getDataCenterId(), updated);
        }
    }

    @Override
    protected ActionParametersBase getRemoveParameters(
            org.ovirt.engine.core.common.businessentities.network.Network entity) {
        return new RemoveNetworkParameters(entity.getId());
    }
}
