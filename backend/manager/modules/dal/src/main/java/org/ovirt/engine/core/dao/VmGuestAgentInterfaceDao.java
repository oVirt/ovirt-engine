package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmGuestAgentInterface;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@link VmGuestAgentInterfaceDao} defines a type for performing CRUD operations on instances of
 * {@link VmGuestAgentInterface}.
 */
public interface VmGuestAgentInterfaceDao extends Dao {

    /**
     * Returns a list of the VmGuestAgentInterfaces for the given VM Id
     * @param vmId
     *            the VM's ID
     * @return the list of VmGuestAgentInterfaces
     */
    List<VmGuestAgentInterface> getAllForVm(Guid vmId);

    /**
     * Returns a list of the VmGuestAgentInterfaces for the given VM Id
     * @param vmId
     *            the VM's ID
     * @param userId
     *            the ID of the user requesting the information
     * @param filtered
     *            Whether the results should be filtered according to the user's permissions
     * @return the list of VmGuestAgentInterfaces
     */
    List<VmGuestAgentInterface> getAllForVm(Guid vmId, Guid userId, boolean filtered);

    /**
     * Removes all the VmGuestAgentInterfaces of the given VMs
     * @param vmIds
     *            the VMs' IDs
     */
    void removeAllForVms(Collection<Guid> vmIds);

    /**
     * Persists the given VmGuestAgentInterface
     * @param vmGuestAgentInterface
     *            the VmGuestAgentInterface
     */
    void save(VmGuestAgentInterface vmGuestAgentInterface);
}
