package org.ovirt.engine.core.common.queries;

public class GetVdsByNameParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -5403234842490143970L;

    public GetVdsByNameParameters(String name) {
        _name = name;
    }

    private String _name;

    public String getName() {
        return _name;
    }

    public GetVdsByNameParameters() {
    }
}
