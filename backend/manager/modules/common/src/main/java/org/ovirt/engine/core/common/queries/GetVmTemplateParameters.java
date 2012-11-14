package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetVmTemplateParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 8906662143775124331L;

    private Guid _id = new Guid();
    private String _name;

    public GetVmTemplateParameters(Guid id) {
        _id = id;
    }

    public GetVmTemplateParameters(String name) {
        _name = name;
    }

    public Guid getId() {
        return _id;
    }

    public String getName() {
        return _name;
    }

    public GetVmTemplateParameters() {
    }
}
