package org.ovirt.engine.core.common.action;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;

public class InternalImportExternalNetworkParameters extends ImportExternalNetworkParametersBase {

    private String providerName;
    private Network externalNetwork;

    public String getProviderName() {
        return providerName;
    }

    public Network getExternalNetwork() {
        return externalNetwork;
    }

    private InternalImportExternalNetworkParameters() {
    }

    public InternalImportExternalNetworkParameters(String providerName, Network externalNetwork, Guid dataCenterId,
                                                   boolean isPublicUse, boolean attachToAllClusters) {
        super(dataCenterId, isPublicUse, attachToAllClusters);
        this.providerName = providerName;
        this.externalNetwork = Objects.requireNonNull(externalNetwork);
    }
}
