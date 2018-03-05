package org.ovirt.engine.core.common.queries;


public class GetUserVmsByUserIdAndGroupsParameters extends QueryParametersBase {

    private static final long serialVersionUID = -7339942735100317616L;

    public GetUserVmsByUserIdAndGroupsParameters() {
    }

    private boolean includeDiskData;

    public boolean getIncludeDiskData() {
        return includeDiskData;
    }

    public void setIncludeDiskData(boolean includeDiskData) {
        this.includeDiskData = includeDiskData;
    }
}
