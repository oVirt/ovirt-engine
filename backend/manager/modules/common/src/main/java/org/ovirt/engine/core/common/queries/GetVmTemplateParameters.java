package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmTemplateParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 8906662143775124331L;

    private Guid _id;
    private String _name;

    public GetVmTemplateParameters(Guid id) {
        _id = id;
    }

    public GetVmTemplateParameters(String name) {
        this(Guid.Empty);
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
