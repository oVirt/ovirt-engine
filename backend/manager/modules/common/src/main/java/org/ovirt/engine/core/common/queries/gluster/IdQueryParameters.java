package org.ovirt.engine.core.common.queries.gluster;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.compat.Guid;

/**
 * Parameter class for the "GetById" queries
 */
public class IdQueryParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = -4601447036978553847L;
    private Guid id;

    public IdQueryParameters(Guid id) {
        setId(id);
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid volumeId) {
        this.id = volumeId;
    }
}
