package org.ovirt.engine.core.bll.network.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ClusterCommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.dao.network.NetworkDao;

public abstract class NetworkClusterCommandBase<T extends NetworkClusterParameters> extends ClusterCommandBase<T> {

    private Network persistedNetwork;
    @Inject
    private NetworkDao networkDao;

    protected NetworkClusterCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    protected NetworkCluster getNetworkCluster() {
        return getParameters().getNetworkCluster();
    }

    protected Network getPersistedNetwork() {
        if (persistedNetwork == null) {
            persistedNetwork = networkDao.get(getNetworkCluster().getNetworkId());
        }
        return persistedNetwork;
    }

    public String getNetworkName() {
        return getPersistedNetwork().getName();
    }

    private boolean validateExternalNetwork(NetworkClusterValidatorBase validator) {
        return validate(validator.externalNetworkNotDisplay(getNetworkName()))
                && validate(validator.externalNetworkNotRequired(getNetworkName()));
    }

    protected boolean validateAttachment(NetworkClusterValidatorBase validator) {
        final Network network = getPersistedNetwork();

        boolean result = validate(validator.managementNetworkRequired(network));
        result = result && validate(validator.managementNetworkNotExternal(network));
        result = result && validate(validator.defaultRouteNetworkCannotBeExternal(network));
        result = result && validate(validator.managementNetworkChange());
        result = result && validate(validator.roleNetworkHasIp());
        result = result && (!getPersistedNetwork().isExternal() || validateExternalNetwork(validator));
        result = result && validate(validator.portIsolationCompatibleClusterLevel(getCluster(), network));
        result = result && validate(validator.portIsolationCompatibleSwitchType(getCluster(), network));

        return result;
    }
}
