package org.ovirt.engine.core.common.queries;


public class GetUserVmsByUserIdAndGroupsParameters extends VdcUserQueryParametersBase {

    private static final long serialVersionUID = 98112763182708327L;

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
