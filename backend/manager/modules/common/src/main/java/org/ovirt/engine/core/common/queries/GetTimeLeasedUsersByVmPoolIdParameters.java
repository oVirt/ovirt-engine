package org.ovirt.engine.core.common.queries;

public class GetTimeLeasedUsersByVmPoolIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -7330156039187698528L;

    public GetTimeLeasedUsersByVmPoolIdParameters(int id) {
        _id = id;
    }

    private int _id;

    public int getId() {
        return _id;
    }

    public GetTimeLeasedUsersByVmPoolIdParameters() {
    }
}
