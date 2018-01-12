package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ImportExternalNetworkParametersBase extends ActionParametersBase {

    private Guid dataCenterId;
    private boolean isPublicUse;
    private boolean attachToAllClusters;

    public Guid getDataCenterId() {
        return dataCenterId;
    }

    public boolean isPublicUse() {
        return isPublicUse;
    }

    public boolean isAttachToAllClusters() {
        return attachToAllClusters;
    }

    protected ImportExternalNetworkParametersBase() {
    }

    public ImportExternalNetworkParametersBase(Guid dataCenterId, boolean isPublicUse, boolean attachToAllClusters) {
        this.dataCenterId = dataCenterId;
        this.isPublicUse = isPublicUse;
        this.attachToAllClusters = attachToAllClusters;
    }
}
