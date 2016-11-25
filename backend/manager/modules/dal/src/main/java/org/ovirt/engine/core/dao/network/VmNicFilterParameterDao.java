package org.ovirt.engine.core.dao.network;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.VmNicFilterParameter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;

public interface VmNicFilterParameterDao extends GenericDao<VmNicFilterParameter, Guid> {

    /**
     * Retrieves a filter parameter.
     *
     * @param id
     *            the filter parameter's ID
     * @param userId
     *            the id of the user performing the query
     * @param filtered
     *            does the query should be filtered by the user
     *
     * @return the filter parameter
     */

    public VmNicFilterParameter get(Guid id, Guid userId, boolean filtered);

    /**
     * Retrieves all filter parameters.
     *
     * @param userId
     *            the id of the user performing the query
     * @param filtered
     *            does the query should be filtered by the user
     *
     * @return the list of filter parameters
     */
    List<VmNicFilterParameter> getAll(Guid userId, boolean filtered);

    /**
     * Retrieves all filter parameters for the given vm interface.
     *
     * @param vmInterfaceId
     *            the VmNic id
     * @return the list of filter parameters
     */
    List<VmNicFilterParameter> getAllForVmNic(Guid vmInterfaceId);

    /**
     * Retrieves all filter parameters for the given vm interface
     *
     * @param vmInterfaceId
     *            the network's ID
     * @param userId
     *            the id of the user performing the query
     * @param filtered
     *            does the query should be filtered by the user
     * @return the list of filter parameters
     */
    List<VmNicFilterParameter> getAllForVmNic(Guid vmInterfaceId, Guid userId, boolean filtered);
}
