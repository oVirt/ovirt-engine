package org.ovirt.engine.core.dao.network;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DAO;
import org.ovirt.engine.core.dao.SearchDAO;

public interface NetworkViewDao extends DAO, SearchDAO<NetworkView> {

    /**
     * Retrieves all networks for the given provider.
     *
     * @param id
     *            the provider's ID
     * @return the list of networks
     */
    List<NetworkView> getAllForProvider(Guid id);
}
