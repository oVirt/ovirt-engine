package org.ovirt.engine.core.common.queries;

public class GetAdGroupsAttachedToTimeLeasedVmPoolParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 2696702562297712449L;

    public GetAdGroupsAttachedToTimeLeasedVmPoolParameters(int id) {
        _id = id;
    }

    private int _id;

    public int getId() {
        return _id;
    }

    public GetAdGroupsAttachedToTimeLeasedVmPoolParameters() {
    }
}
