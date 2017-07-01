package org.ovirt.engine.core.common.queries;

public class GetVmIconDefaultParameters extends QueryParametersBase {


    private int operatingSystemId;

    private GetVmIconDefaultParameters() {}

    public GetVmIconDefaultParameters(int operatingSystemId) {
        this.operatingSystemId = operatingSystemId;
    }

    public int getOperatingSystemId() {
        return operatingSystemId;
    }
}
