package org.ovirt.engine.core.dao.network;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;

public interface VnicProfileDao extends GenericDao<VnicProfile, Guid> {

    /**
     * Retrieves all vnic profiles associated with the given network.
     *
     * @param networkId
     *            the network's ID
     * @return the list of vnic profiles
     */
    List<VnicProfile> getAllForNetwork(Guid networkId);
}
