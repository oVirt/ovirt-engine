package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Filter;
import org.ovirt.engine.api.model.Filters;
import org.ovirt.engine.api.resource.FilterResource;
import org.ovirt.engine.api.resource.FiltersResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.core.common.scheduling.parameters.ClusterPolicyCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendFiltersResource
        extends BackendPolicyUnitsResource<Filters, Filter>
        implements FiltersResource {

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
    public FilterResource getFilterResource(String id) {
        return inject(new BackendFilterResource(id, this));
    }

    @Override
    public Response add(Filter incoming) {
        return performAdd(incoming);
    }

    @Override
    protected ParametersProvider<Filter, ClusterPolicy> getAddParametersProvider() {
        return (model, entity) -> new ClusterPolicyCRUDParameters(entity.getId(), map(model, entity));
    }

    @Override
    protected void updateIncomingId(Filter incoming) {
        incoming.setId(incoming.getSchedulingPolicyUnit().getId());
    }

}
