package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Filter;
import org.ovirt.engine.api.resource.FilterResource;

public class BackendFilterResource extends BackendPolicyUnitResource<Filter> implements
        FilterResource {

    protected BackendFilterResource(String id) {
        super(id, Filter.class);
    }

}
