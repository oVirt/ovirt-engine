package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Filter;
import org.ovirt.engine.api.resource.FilterResource;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.compat.Guid;

public class BackendFilterResource extends BackendPolicyUnitResource<Filter> implements
        FilterResource {

    protected BackendFilterResource(String id, BackendFiltersResource parent) {
        super(id, parent, Filter.class);
    }

    @Override
    protected Filter createPolicyUnitByType() {
        Filter filter = new Filter();
        filter.setId(id);
        return filter;
    }

    @Override
    protected void updateEntityForRemove(ClusterPolicy entity, Guid id) {
        int i = 0;
        boolean found = false;
        if (entity.getFilters() == null) {
            return;
        }
        for (; i < entity.getFilters().size(); i++) {
            if (entity.getFilters().get(i).equals(id)) {
                found = true;
                break;
            }
        }
        if (found) {
            entity.getFilters().remove(i);
        }
    }
}
