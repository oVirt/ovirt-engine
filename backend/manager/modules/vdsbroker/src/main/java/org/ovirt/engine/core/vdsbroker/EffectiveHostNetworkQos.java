package org.ovirt.engine.core.vdsbroker;

import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.dao.network.HostNetworkQosDao;

@Singleton
public class EffectiveHostNetworkQos {
    private final HostNetworkQosDao hostNetworkQosDao;

    @Inject
    public EffectiveHostNetworkQos(HostNetworkQosDao hostNetworkQosDao) {
        this.hostNetworkQosDao = hostNetworkQosDao;
    }

    public HostNetworkQos getQos(NetworkAttachment networkAttachment, Network network) {
        Objects.requireNonNull(network);

        return networkAttachment != null && networkAttachment.isQosOverridden()
            ? HostNetworkQos.fromAnonymousHostNetworkQos(networkAttachment.getHostNetworkQos())
                : getHostNetworkQosFromNetwork(network);
    }

    private HostNetworkQos getHostNetworkQosFromNetwork(Network network) {
        return hostNetworkQosDao.get(network.getQosId());
    }
}
