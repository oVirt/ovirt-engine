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

    public void completeNetworkAttachment(NetworkAttachment networkAttachment, Guid dataCenterId) {
        completeNetworkAttachment(networkAttachment, null, dataCenterId);
    }

    public void completeNetworkAttachments(List<NetworkAttachment> networkAttachments, BusinessEntityMap<Network> clusterNetworks, Guid dataCenterId) {
        for (NetworkAttachment networkAttachment : networkAttachments) {
            completeNetworkAttachment(networkAttachment, clusterNetworks, dataCenterId);
        }
    }

    private void completeNetworkAttachment(NetworkAttachment networkAttachment, BusinessEntityMap<Network> clusterNetworks, Guid dataCenterId) {
        Guid networkId = networkAttachment.getNetworkId();
        String networkName = networkAttachment.getNetworkName();

        boolean networkNameSpecified = networkName != null;
        boolean networkIdSpecified = networkId != null;

        if (!networkIdSpecified && !networkNameSpecified ||
                networkIdSpecified && networkNameSpecified) {
            return;
        }

        if (networkNameSpecified) {
            Network network = getNetworkByName(networkName, clusterNetworks, dataCenterId);
            boolean networkByNameExists = network != null;
            if (networkByNameExists) {
                networkAttachment.setNetworkId(network.getId());
            }
        }

        if (networkIdSpecified) {
            Network network = getNetworkById(networkId, clusterNetworks);
            boolean networkByIdExists = network != null;
            if (networkByIdExists) {
                networkAttachment.setNetworkName(network.getName());
            }
        }
    }

    private Network getNetworkById(Guid networkId, BusinessEntityMap<Network> clusterNetworks) {
        if (clusterNetworks != null) {
            return clusterNetworks.get(networkId);
        } else {
            return networkDao.get(networkId);
        }
    }

    private Network getNetworkByName(String networkName, BusinessEntityMap<Network> clusterNetworks, Guid dataCenterId) {
        if (clusterNetworks != null) {
            return clusterNetworks.get(networkName);
        } else {
            return networkDao.getByNameAndDataCenter(networkName, dataCenterId);
        }
    }
}
