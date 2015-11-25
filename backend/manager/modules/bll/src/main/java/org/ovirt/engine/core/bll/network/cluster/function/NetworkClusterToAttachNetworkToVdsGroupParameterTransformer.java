package org.ovirt.engine.core.bll.network.cluster.function;

import java.util.Objects;
import java.util.function.Function;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.dao.network.NetworkDao;

@Singleton
@Named
final class NetworkClusterToAttachNetworkToVdsGroupParameterTransformer
        implements Function<NetworkCluster, AttachNetworkToVdsGroupParameter> {

    private final NetworkDao networkDao;

    @Inject
    NetworkClusterToAttachNetworkToVdsGroupParameterTransformer(NetworkDao networkDao) {
        Objects.requireNonNull(networkDao, "networkDao cannot be null");
        this.networkDao = networkDao;
    }

    @Override
    public AttachNetworkToVdsGroupParameter apply(NetworkCluster networkCluster) {
        final Network network = networkDao.get(networkCluster.getNetworkId());
        network.setCluster(networkCluster);
        return new AttachNetworkToVdsGroupParameter(networkCluster.getClusterId(), network);
    }
}
