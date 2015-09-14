package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Nic;
import org.ovirt.engine.api.model.Nics;
import org.ovirt.engine.api.resource.ReadOnlyDevicesResource;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendReadOnlyNicsResource
        extends AbstractBackendReadOnlyDevicesResource<Nic, Nics, VmNetworkInterface>
        implements ReadOnlyDevicesResource<Nic, Nics> {

    public BackendReadOnlyNicsResource(Guid parentId, VdcQueryType queryType, VdcQueryParametersBase queryParams) {
        super(Nic.class, Nics.class, VmNetworkInterface.class, parentId, queryType, queryParams);
    }

    @Override
    protected <T> boolean matchEntity(VmNetworkInterface entity, T id) {
        return id.equals(entity.getId());
    }
}
