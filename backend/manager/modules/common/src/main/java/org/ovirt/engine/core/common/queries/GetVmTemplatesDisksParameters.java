package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmTemplatesDisksParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 4859370588871811757L;
    private Guid id = new Guid();

    public GetVmTemplatesDisksParameters(Guid vmTemplateId) {
        id = vmTemplateId;
    }

    public GetVmTemplatesDisksParameters() {
    }

    public Guid getId() {
        return id;
    }
}
