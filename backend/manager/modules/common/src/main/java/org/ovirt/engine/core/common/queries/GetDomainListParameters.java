package org.ovirt.engine.core.common.queries;

public class GetDomainListParameters extends VdcQueryParametersBase{
    private boolean filterInternalDomain;

    public GetDomainListParameters() {
    }

    public GetDomainListParameters(boolean filterInternalDomain) {
        this.setFilterInternalDomain(filterInternalDomain);
    }

    public void setFilterInternalDomain(boolean filterInternalDomain) {
        this.filterInternalDomain = filterInternalDomain;
    }

    public boolean getFilterInternalDomain() {
        return filterInternalDomain;
    }

}
