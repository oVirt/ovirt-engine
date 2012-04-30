package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetVmsByVmTemplateGuidParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4308515803583157101L;

    public GetVmsByVmTemplateGuidParameters(Guid id) {
        _id = id;
    }

    private Guid _id = new Guid();

    public Guid getId() {
        return _id;
    }

    public GetVmsByVmTemplateGuidParameters() {
    }
}
