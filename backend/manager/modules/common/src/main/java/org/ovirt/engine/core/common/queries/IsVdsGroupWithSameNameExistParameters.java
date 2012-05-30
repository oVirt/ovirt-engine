package org.ovirt.engine.core.common.queries;

public class IsVdsGroupWithSameNameExistParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 6145884648491618837L;

    public IsVdsGroupWithSameNameExistParameters(String name) {
        _name = name;
    }

    private String _name;

    public String getName() {
        return _name;
    }

    public IsVdsGroupWithSameNameExistParameters() {
    }
}
