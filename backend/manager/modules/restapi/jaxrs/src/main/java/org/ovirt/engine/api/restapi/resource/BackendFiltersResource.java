package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Filter;
import org.ovirt.engine.api.model.Filters;
import org.ovirt.engine.api.resource.FilterResource;
import org.ovirt.engine.api.resource.FiltersResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendFiltersResource extends BackendPolicyUnitsResource<Filters, Filter> implements FiltersResource {

    protected BackendFiltersResource(Guid schedulingPolicyId) {
        super(schedulingPolicyId, Filter.class);
    }

    @Override
    public Filters list() {
        ClusterPolicy clusterPolicy = getClusterPolicy();
        Filters filters = new Filters();
        if (clusterPolicy.getFilters() != null) {
            for (Guid filterGuid : clusterPolicy.getFilters()) {
                Filter filter = new Filter();
                filter.setId(filterGuid.toString());
                filters.getFilters().add(addLinks(map(clusterPolicy, filter)));
            }
        }
        return filters;
    }

    @Override
    public FilterResource getSubResource(String id) {
        return inject(new BackendFilterResource(id, schedulingPolicyId));
    }

    @SingleEntityResource
    public FilterResource getFilterSubResource(String id) {
        return getSubResource(id);
    }

    @Override
    public Filter add(Filter incoming) {
        return performAdd(incoming);
    }

    @Override
    protected ParametersProvider<Filter, ClusterPolicy> getAddParametersProvider() {
        return new ParametersProvider<Filter, ClusterPolicy>() {
            @Override
            public VdcActionParametersBase getParameters(Filter model, ClusterPolicy entity) {
                return new ClusterPolicyCRUDParameters(entity.getId(), map(model, entity));
            }
        };
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

    @Override
    protected void updateIncomingId(Filter incoming) {
        incoming.setId(incoming.getSchedulingPolicyUnit().getId());
    }

}
