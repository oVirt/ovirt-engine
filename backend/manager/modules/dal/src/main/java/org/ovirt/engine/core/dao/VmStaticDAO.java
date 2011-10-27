package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.compat.Guid;

public interface VmStaticDAO extends GenericDao<VmStatic, Guid> {

    /**
     * Gets all static VMs by name.
     *
     * @param name
     *            the vm name
     * @return the list of vms
     */
    List<VmStatic> getAllByName(String name);

    /**
     * Gets all static VMs by Storage Pool Id.
     *
     * @param spId
     *            storage pool id
     * @return the list of vms
     */
    List<VmStatic> getAllByStoragePoolId(Guid spId);

    /**
     * Retrieves all static VMs for the specified VDS group.
     *
     * @param vdsGroup
     *            the VDS group
     * @return the list of VMs
     */
    List<VmStatic> getAllByVdsGroup(Guid vdsGroup);

    /**
     * Not really sure what this method's doing.
     *
     * @param vds
     *            the VDS id
     * @return the list of VMs
     */
    List<VmStatic> getAllWithFailbackByVds(Guid vds);

    /**
     * Retrieves all static VMs for the specified group and name.
     *
     * @param group
     *            the group
     * @param name
     *            the name
     * @return the list of vms
     */
    List<VmStatic> getAllByGroupAndNetworkName(Guid group, String name);

    /**
     * Get the names of VMs pinned to the specified host.
     *
     * @param host
     *            The host's id.
     * @return The names of the VMs which are pinned to the host, or empty if none.
     */
    List<String> getAllNamesPinnedToHost(Guid host);
}
