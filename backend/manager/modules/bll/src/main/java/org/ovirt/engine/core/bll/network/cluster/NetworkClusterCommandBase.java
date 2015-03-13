package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.bll.VdsGroupCommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;

public abstract class NetworkClusterCommandBase<T extends NetworkClusterParameters> extends VdsGroupCommandBase<T> {

    private Network persistedNetwork;

    protected NetworkClusterCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    protected NetworkClusterCommandBase(T parameters) {
        super(parameters);
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

        boolean result = validate(validator.managementNetworkRequired(network));
        result = result && validate(validator.managementNetworkNotExternal(network));
        result = result && validate(validator.managementNetworkChange());
        result = result && validate(validator.migrationPropertySupported());
        result = result && validate(validator.glusterNetworkSupported());
        result = result && (!getPersistedNetwork().isExternal() || validateExternalNetwork(validator));

        return result;
    }
}
