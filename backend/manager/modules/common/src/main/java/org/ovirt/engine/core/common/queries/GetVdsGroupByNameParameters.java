package org.ovirt.engine.core.common.queries;

public class GetVdsGroupByNameParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1225647647659382659L;

    public GetVdsGroupByNameParameters(String name) {
        _name = name;
    }

    private String _name;

    public String getName() {
        return _name;
    }

    public GetVdsGroupByNameParameters() {
    }
}
