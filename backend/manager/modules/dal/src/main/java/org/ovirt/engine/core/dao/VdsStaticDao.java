package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VdsStaticDao</code> defines a type that performs CRUD operations on instances of {@link VDS}.
 *
 *
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
     * Separate update method for {@code last_stored_kernel_cmdline} column to prevent
     * race overwrites.
     */
    void updateLastStoredKernelCmdline(Guid vdsStaticId, String lastStoredKernelCmdline);
 }
