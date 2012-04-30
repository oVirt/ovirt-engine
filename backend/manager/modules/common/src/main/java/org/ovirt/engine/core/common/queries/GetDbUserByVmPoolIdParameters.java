package org.ovirt.engine.core.common.queries;

public class GetDbUserByVmPoolIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 789118868522923324L;

    public GetDbUserByVmPoolIdParameters(int id) {
        _id = id;
    }

    private int _id;

    public int getId() {
        return _id;
    }

    public GetDbUserByVmPoolIdParameters() {
    }
}
