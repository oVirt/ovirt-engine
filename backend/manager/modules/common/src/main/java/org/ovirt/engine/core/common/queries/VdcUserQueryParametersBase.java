package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

/**
 * A base class for the parameters of queries which have a user ID as a parameter.
 */
public class VdcUserQueryParametersBase extends VdcQueryParametersBase {

    private static final long serialVersionUID = 2364338337580478810L;

    private Guid userId;

    public VdcUserQueryParametersBase() {
        super();
    }

    public VdcUserQueryParametersBase(Guid userId) {
        this.userId = userId;
    }

    public Guid getUserId() {
        return userId;
    }
}
