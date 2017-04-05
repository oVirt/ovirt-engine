package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Network;

public class BackendDataCenterNetworkResource extends BackendNetworkResource {

    private BackendDataCenterNetworksResource parent;

    public BackendDataCenterNetworkResource(String id, BackendDataCenterNetworksResource parent) {
        super(id, parent);
        this.parent = parent;
    }

    @Override
    public Network get() {
        org.ovirt.engine.core.common.businessentities.network.Network entity = getNetwork();
        if (entity == null) {
            return notFound();
        }
        return addLinks(map(entity));
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
}
