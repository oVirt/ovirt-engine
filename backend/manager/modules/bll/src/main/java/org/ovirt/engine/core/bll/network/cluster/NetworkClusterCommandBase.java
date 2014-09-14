package org.ovirt.engine.core.bll.network.cluster;

import org.ovirt.engine.core.bll.VdsGroupCommandBase;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.NetworkUtils;

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

    private boolean validateExternalNetwork(NetworkClusterValidator validator) {
        return validate(validator.externalNetworkSupported())
                && validate(validator.externalNetworkNotDisplay(getNetworkName()))
                && validate(validator.externalNetworkNotRequired(getNetworkName()));
    }

    protected abstract Version getClusterVersion();

    protected boolean validateAttachment() {
        NetworkClusterValidator validator = new NetworkClusterValidator(getNetworkCluster(), getClusterVersion());
        return (!NetworkUtils.isManagementNetwork(getNetworkName())
                || validate(validator.managementNetworkAttachment(getNetworkName())))
                && validate(validator.migrationPropertySupported(getNetworkName()))
                && (!getPersistedNetwork().isExternal() || validateExternalNetwork(validator));
    }

}
