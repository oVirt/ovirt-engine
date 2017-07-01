package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

/**
 * A base class for the parameters of queries which have a user ID as a parameter.
 */
public class UserQueryParametersBase extends QueryParametersBase {

    private static final long serialVersionUID = 2364338337580478810L;

    private Guid userId;

    public UserQueryParametersBase() {
        super();
    }

    public UserQueryParametersBase(Guid userId) {
        this.userId = userId;
    }

    public Guid getUserId() {
        return userId;
    }
}
