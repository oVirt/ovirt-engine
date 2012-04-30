package org.ovirt.engine.core.common.queries;

public class GetAdGroupsAttachedToVmPoolParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 6796451423817563295L;

    public GetAdGroupsAttachedToVmPoolParameters(int id) {
        _id = id;
    }

    private int _id;

    public int getId() {
        return _id;
    }

    public GetAdGroupsAttachedToVmPoolParameters() {
    }
}
