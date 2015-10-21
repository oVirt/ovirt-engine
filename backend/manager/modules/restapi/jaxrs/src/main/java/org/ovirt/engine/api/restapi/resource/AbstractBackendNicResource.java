package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

public abstract class AbstractBackendNicResource
        extends AbstractBackendActionableResource<Nic, VmNetworkInterface> {
    protected AbstractBackendNicResource(String id) {
        super(id, Nic.class, VmNetworkInterface.class);
    }
}
