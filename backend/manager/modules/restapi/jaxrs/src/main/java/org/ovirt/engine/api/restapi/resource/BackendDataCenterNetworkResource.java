package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import org.ovirt.engine.api.model.Network;

public class BackendDataCenterNetworkResource extends BackendNetworkResource {

    BackendDataCenterNetworksResource _parent;

    public BackendDataCenterNetworkResource(String id, BackendDataCenterNetworksResource parent) {
        super(id, parent);
        this._parent = parent;
    }

    @Override
    public Network get() {
        org.ovirt.engine.core.common.businessentities.network.Network entity = getNetworks();
        if (entity == null) {
            return notFound();
        }
        return addLinks(map(entity));
    }

    private org.ovirt.engine.core.common.businessentities.network.Network getNetworks() {
        List<org.ovirt.engine.core.common.businessentities.network.Network> networks = _parent.getNetworks();
        for (org.ovirt.engine.core.common.businessentities.network.Network entity : networks) {
            if (entity.getId().toString().equals(id)) {
                return entity;
            }
        }
        return null;
    }

    @Override
    public BackendDataCenterNetworksResource getParent() {
        return _parent;
    }
}
