package org.ovirt.engine.core.bll.network.cluster.function;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.utils.linq.Function;

@Singleton
@Named
final class NetworkClusterParameterTransformer implements Function<NetworkCluster, NetworkClusterParameters> {

    @Override
    public NetworkClusterParameters eval(NetworkCluster networkCluster) {
        return new NetworkClusterParameters(networkCluster);
    }
}
