package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Filter;
import org.ovirt.engine.api.resource.FilterResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendFilterResource extends BackendPolicyUnitResource<Filter> implements
        FilterResource {

    protected BackendFilterResource(String id, Guid parentId) {
        super(id, parentId, Filter.class);
    }

    @Override
    protected Filter createPolicyUnitByType() {
        Filter filter = new Filter();
        filter.setId(id);
        return filter;
    }
}
