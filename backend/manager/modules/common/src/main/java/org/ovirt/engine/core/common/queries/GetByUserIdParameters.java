package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

/**
 * class for all getById queries by user ids TODO re-factor commands that duplicates this functionality. extend this
 * class if more than id is needed
 */
public class GetByUserIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1092832045219008933L;
    private Guid userId;

    public GetByUserIdParameters() {
    }

    public GetByUserIdParameters(Guid userId) {
        this.userId = userId;
    }

    public void setUserId(Guid userId) {
        this.userId = userId;
    }

    public Guid getUserId() {
        return userId;
    }

}
