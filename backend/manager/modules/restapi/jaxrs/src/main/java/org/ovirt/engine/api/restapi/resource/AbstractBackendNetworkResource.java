package org.ovirt.engine.api.restapi.resource;


import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.businessentities.network;

public class AbstractBackendNetworkResource
    extends AbstractBackendSubResource<Network, network> {

    protected AbstractBackendNetworksResource parent;

    public AbstractBackendNetworkResource(String id, AbstractBackendNetworksResource parent) {
        super(id, Network.class, network.class);
        this.parent = parent;
    }

    public Network get() {
        network entity = parent.lookupNetwork(guid);
        if (entity == null) {
            return notFound();
        }
        return addLinks(map(entity));
    }

    AbstractBackendNetworksResource getParent() {
        return parent;
    }
}
