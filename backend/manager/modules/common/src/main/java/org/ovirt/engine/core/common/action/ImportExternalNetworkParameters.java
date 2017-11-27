package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ImportExternalNetworkParameters extends ActionParametersBase {

    private Guid providerId;
    private String networkExternalId;
    private Guid dataCenterId;
    private boolean isPublicUse;
    private boolean attachToAllClusters;

    public Guid getProviderId() {
        return providerId;
    }

    public String getNetworkExternalId() {
        return networkExternalId;
    }

    public Guid getDataCenterId() {
        return dataCenterId;
    }

    public boolean isPublicUse() {
        return isPublicUse;
    }

    public boolean isAttachToAllClusters() {
        return attachToAllClusters;
    }

    private ImportExternalNetworkParameters() {
    }

    public ImportExternalNetworkParameters(Guid providerId, String networkExternalId, Guid dataCenterId,
                                           boolean isPublicUse, boolean attachToAllClusters) {
        this.providerId = providerId;
        this.networkExternalId = networkExternalId;
        this.dataCenterId = dataCenterId;
        this.isPublicUse = isPublicUse;
        this.attachToAllClusters = attachToAllClusters;
    }
}
