package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code VdsStaticDao} defines a type that performs CRUD operations on instances of {@link VdsStatic}.
 */
public interface VdsStaticDao extends GenericDao<VdsStatic, Guid> {
    /**
     * Retrieves the instance for the given host name.
     *
     * @param hostname
     *            the host name
     * @return the instance
     */
    VdsStatic getByHostName(String hostname);

    /**
     * Retrieves the instance for the given vds name.
     * @param vdsName
     *            the vds name
     * @return the instance
     */
    VdsStatic getByVdsName(String vdsName);

    /**
     * Finds all instances with the given ip address.
     * @param address
     *            the ip address
     * @return the list of instances
     */
    List<VdsStatic> getAllWithIpAddress(String address);

    /**
     * Retrieves all instances associated with the specified VDS group.
     *
     * @param cluster
     *            the group id
     * @return the list of instances
     */
    List<VdsStatic> getAllForCluster(Guid cluster);

    /**
     * Retrieves for the parameter VM, return all pinned host names.
     *
     * @param vm
     *            the target vm's guid
     * @return the list of host names
     */
    List<String> getAllHostNamesPinnedToVm(Guid vm);

    /**
     * Get all VdsStatic with the given ids
     */
    List<VdsStatic> getByIds(List<Guid> ids);

    /**
     * Separate update method for {@code last_stored_kernel_cmdline} column to prevent
     * race overwrites.
     */
    void updateLastStoredKernelCmdline(Guid vdsStaticId, String lastStoredKernelCmdline);

    /**
     * Separate update method for {@code kernel_cmdline} column to prevent
     * race overwrites.
     */
    void updateKernelCmdlines(Guid vdsStaticId, VdsStatic staticData);

    /**
     * Separate update method for {@code reinstall_required} column
     * @param vdsStaticId the vds static id
     * @param reinstallRequired flag indicating if vds needs to be reinstalled
     */
    void updateReinstallRequired(Guid vdsStaticId, boolean reinstallRequired);

    /**
     * Checks if exists a host with the given status in the cluster that do not have the network attached to its NIC's.
     *
     * @param clusterId cluster id
     * @param networkName network name
     * @param hostStatus host status
     *
     * @return <code>true</code> if such host exists otherwise <code>false</code>.
     */
    boolean checkIfExistsHostThatMissesNetworkInCluster(Guid clusterId, String networkName, VDSStatus hostStatus);
}
