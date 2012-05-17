package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;

public class GetDiskByDiskIdParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = -8638015216365532764L;

    public GetDiskByDiskIdParameters(Guid diskId) {
        this.diskId = diskId;
    }

    private Guid diskId = Guid.Empty;

    public Guid getDiskId() {
        return diskId;
    }

    public GetDiskByDiskIdParameters() {
    }
}
