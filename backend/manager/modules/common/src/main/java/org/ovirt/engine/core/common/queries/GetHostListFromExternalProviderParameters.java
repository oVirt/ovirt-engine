package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetHostListFromExternalProviderParameters extends QueryParametersBase {

    private static final long serialVersionUID = 8729202312358351850L;
    private Guid providerId;
    private boolean filterOutExistingHosts;
    private String searchFilter;

    public GetHostListFromExternalProviderParameters() {
        super();
    }

    public GetHostListFromExternalProviderParameters(Guid providerId, boolean filterOutExistingHosts, String searchFilter) {
        super();
        this.providerId = providerId;
        this.filterOutExistingHosts = filterOutExistingHosts;
        this.searchFilter = searchFilter;
    }

    public Guid getProviderId() {
        return providerId;
    }

    public void setProviderId(Guid providerId) {
        this.providerId = providerId;
    }

    public boolean isFilterOutExistingHosts() {
        return filterOutExistingHosts;
    }

    public void setFilterOutExistingHosts(boolean filterOutExistingHosts) {
        this.filterOutExistingHosts = filterOutExistingHosts;
    }

    public String getSearchFilter() {
        return searchFilter;
    }

    public void setSearchFilter(String searchFilter) {
        this.searchFilter = searchFilter;
    }

}
