package org.ovirt.engine.core.bll.network.host;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.BusinessEntityMap;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.NetworkDao;


@Singleton
public class NetworkIdNetworkNameCompleter {

    private final NetworkDao networkDao;

    @Inject
    public NetworkIdNetworkNameCompleter(NetworkDao networkDao) {
        this.networkDao = networkDao;
    }

    public void completeNetworkAttachment(NetworkAttachment networkAttachment) {
        completeNetworkAttachment(networkAttachment, null);
    }

    public void completeNetworkAttachments(List<NetworkAttachment> networkAttachments, BusinessEntityMap<Network> clusterNetworks) {
        for (NetworkAttachment networkAttachment : networkAttachments) {
            completeNetworkAttachment(networkAttachment, clusterNetworks);
        }
    }

    private void completeNetworkAttachment(NetworkAttachment networkAttachment, BusinessEntityMap<Network> clusterNetworks) {
        Guid networkId = networkAttachment.getNetworkId();
        String networkName = networkAttachment.getNetworkName();

        if (networkId == null && networkName == null) {
            return;
        }

        if (networkName != null) {
            Network network = getNetworkByName(networkName, clusterNetworks);
            if (network != null) {
                if (networkId == null) {
                    networkAttachment.setNetworkId(network.getId());
                } else {
                    //both id and name were supplied. Their coherence is not guaranteed here.
                }
            }
        } else {
            Network network = getNetworkById(networkId, clusterNetworks);
            networkAttachment.setNetworkName(network.getName());
        }
    }

    private Network getNetworkById(Guid networkId, BusinessEntityMap<Network> clusterNetworks) {
        if (clusterNetworks != null) {
            return clusterNetworks.get(networkId);
        } else {
            return networkDao.get(networkId);
        }
    }

    private Network getNetworkByName(String networkName, BusinessEntityMap<Network> clusterNetworks) {
        if (clusterNetworks != null) {
            return clusterNetworks.get(networkName);
        } else {
            return networkDao.getByName(networkName);
        }
    }
}
