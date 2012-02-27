package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetUserVmsByUserIdAndGroupsParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = 98112763182708327L;

    public GetUserVmsByUserIdAndGroupsParameters() {
    }

    public GetUserVmsByUserIdAndGroupsParameters(Guid id) {
        _id = id;
    }

    private Guid _id = new Guid();

    private boolean includeDiskData;

    public boolean getIncludeDiskData() {
        return includeDiskData;
    }

    public void setIncludeDiskData(boolean includeDiskData) {
        this.includeDiskData = includeDiskData;
    }

    public Guid getId() {
        return _id;
    }
}
