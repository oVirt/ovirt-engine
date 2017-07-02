package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Icon;
import org.ovirt.engine.api.resource.IconResource;
import org.ovirt.engine.core.common.businessentities.VmIcon;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendIconResource extends AbstractBackendSubResource<Icon, VmIcon> implements IconResource {

    protected BackendIconResource(String id) {
        super(id, Icon.class, org.ovirt.engine.core.common.businessentities.VmIcon.class);
    }

    @Override
    public Icon get() {
        return performGet(QueryType.GetVmIcon, new IdQueryParameters(guid));
    }
}
