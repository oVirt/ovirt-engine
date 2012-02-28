package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetUserVmsByUserIdAndGroupsParameters extends VdcUserQueryParametersBase {

    private static final long serialVersionUID = 98112763182708327L;

    public GetUserVmsByUserIdAndGroupsParameters() {
    }

    public GetUserVmsByUserIdAndGroupsParameters(Guid id) {
        super(id);
    }

    private boolean includeDiskData;

    public boolean getIncludeDiskData() {
        return includeDiskData;
    }

    public void setIncludeDiskData(boolean includeDiskData) {
        this.includeDiskData = includeDiskData;
    }

    /** Deprecated, use {@link #getUserId()} instead. */
    @Deprecated
    public Guid getId() {
        return getUserId();
    }
}
