package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class IdAndBooleanQueryParameters extends IdQueryParameters {
    private boolean filterResult = true;

    public IdAndBooleanQueryParameters() {
    }

    public IdAndBooleanQueryParameters(Guid id, boolean filterResult) {
        super(id);
        this.filterResult = filterResult;
    }

    public boolean isFilterResult() {
        return filterResult;
    }

    public void setFilterResult(boolean filterResult) {
        this.filterResult = filterResult;
    }
}
