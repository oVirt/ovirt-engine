package org.ovirt.engine.core.dao.network;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;

public interface NetworkAttachmentDao extends GenericDao<NetworkAttachment, Guid> {

    /**
     * Retrieves all network attachments associated with the given network interface.
     *
     * @param nicId
     *            the network interface ID
     * @return the list of network attachments
     */
    List<NetworkAttachment> getAllForNic(Guid nicId);

    NetworkAttachment getNetworkAttachmentByNicIdAndNetworkId(Guid nicId, Guid networkId);

    /**
     * Retrieves all network attachments associated with the given network.
     *
     * @param networkId
     *            the network ID
     * @return the list of network attachments
     */
    List<NetworkAttachment> getAllForNetwork(Guid networkId);

    /**
     * Retrieves all network attachments associated with the given host.
     *
     * @param hostId
     *            the host ID
     * @return the list of network attachments
     */
    List<NetworkAttachment> getAllForHost(Guid hostId);

    /**
     * Removed network attachment related to given networkId.
     */
    void removeByNetworkId(Guid networkId);
}
