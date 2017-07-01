package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class for the "GetById" queries
 */
public class IdQueryParameters extends QueryParametersBase {

    private static final long serialVersionUID = -4601447036978553847L;
    private Guid id;

    public IdQueryParameters() {
    }

    public IdQueryParameters(Guid id) {
        this.id = id;
    }

    public Guid getId() {
        return id;
    }

    @Override
    public IdQueryParameters withoutRefresh() {
        super.withoutRefresh();
        return this;
    }
}
