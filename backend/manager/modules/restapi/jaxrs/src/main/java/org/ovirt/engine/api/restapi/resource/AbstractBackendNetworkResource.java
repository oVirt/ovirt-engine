package org.ovirt.engine.api.restapi.resource;


import org.ovirt.engine.api.model.Network;

public class AbstractBackendNetworkResource
    extends AbstractBackendSubResource<Network, org.ovirt.engine.core.common.businessentities.network.Network> {

    protected AbstractBackendNetworksResource parent;

    public AbstractBackendNetworkResource(String id, AbstractBackendNetworksResource parent, String... subCollections) {
        super(id, Network.class, org.ovirt.engine.core.common.businessentities.network.Network.class, subCollections);
        this.parent = parent;
    }

    public Network get() {
        org.ovirt.engine.core.common.businessentities.network.Network entity = parent.lookupNetwork(guid);
        if (entity == null) {
            return notFound();
        }
        return addLinks(map(entity));
    }

    AbstractBackendNetworksResource getParent() {
        return parent;
    }

    @Override
    protected Network doPopulate(Network model, org.ovirt.engine.core.common.businessentities.network.Network entity) {
        return model;
    }
}
