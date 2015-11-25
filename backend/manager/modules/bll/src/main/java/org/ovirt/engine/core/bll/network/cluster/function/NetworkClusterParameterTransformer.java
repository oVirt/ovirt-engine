package org.ovirt.engine.core.bll.network.cluster.function;

import java.util.function.Function;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;

@Singleton
@Named
final class NetworkClusterParameterTransformer implements Function<NetworkCluster, NetworkClusterParameters> {

    @Override
    public NetworkClusterParameters apply(NetworkCluster networkCluster) {
        return new NetworkClusterParameters(networkCluster);
    }
}
