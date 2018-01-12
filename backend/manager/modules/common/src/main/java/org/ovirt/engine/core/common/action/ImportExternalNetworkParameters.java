package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ImportExternalNetworkParameters extends ImportExternalNetworkParametersBase {

    private Guid providerId;
    private String networkExternalId;

    public Guid getProviderId() {
        return providerId;
    }

    public String getNetworkExternalId() {
        return networkExternalId;
    }

    private ImportExternalNetworkParameters() {
    }

    public ImportExternalNetworkParameters(Guid providerId, String networkExternalId, Guid dataCenterId,
                                           boolean isPublicUse, boolean attachToAllClusters) {
        super(dataCenterId, isPublicUse, attachToAllClusters);
        this.providerId = providerId;
        this.networkExternalId = networkExternalId;
    }
}
