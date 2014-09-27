package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.bll.VdsGroupCommandBase;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;

public abstract class NetworkClusterCommandBase<T extends NetworkClusterParameters> extends VdsGroupCommandBase<T> {

    private Network persistedNetwork;

    protected NetworkClusterCommandBase(T parameters) {
        super(parameters);
        setVdsGroupId(parameters.getVdsGroupId());
    }

    protected NetworkCluster getNetworkCluster() {
        return getParameters().getNetworkCluster();
    }

    protected Network getPersistedNetwork() {
        if (persistedNetwork == null) {
            persistedNetwork = getNetworkDAO().get(getNetworkCluster().getNetworkId());
        }
        return persistedNetwork;
    }

    public String getNetworkName() {
        return getPersistedNetwork().getName();
    }

    private boolean validateExternalNetwork(NetworkClusterValidatorBase validator) {
        return validate(validator.externalNetworkSupported())
                && validate(validator.externalNetworkNotDisplay(getNetworkName()))
                && validate(validator.externalNetworkNotRequired(getNetworkName()));
    }

    protected boolean validateAttachment(NetworkClusterValidatorBase validator) {
        final Network network = getPersistedNetwork();
        return validate(validator.managementNetworkRequired(network))
               && validate(validator.managementNetworkNotExternal(network))
               && validate(validator.managementNetworkChange())
               && validate(validator.migrationPropertySupported())
               && (!network.isExternal() || validateExternalNetwork(validator));
    }
}
