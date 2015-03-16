package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetExternalSubnetsOnProviderByExternalNetworkQueryParameters
        extends GetEntitiesWithPermittedActionParameters {

    private static final long serialVersionUID = 3354488201485043334L;
    private Guid providerId;
    private String networkId;

    public void setProviderId(Guid providerId) {
        this.providerId = providerId;
    }

    public Guid getProviderId() {
        return providerId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getNetworkId() {
        return networkId;
    }
}
