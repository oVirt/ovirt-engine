package org.ovirt.engine.core.dao.network;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;

public interface HostNicVfsConfigDao extends GenericDao<HostNicVfsConfig, Guid> {
    /**
     * Retrieves a list of all the vfsConfigs for the given host.
     * Notice: only nics which are SR-IOV enabled have vfsConfig.
     *
     * @param vfsConfigId
     *            host nic vfs config id
     * @return list of all the vfsConfigs for the given host.
     *
     */
    List<HostNicVfsConfig> getAllVfsConfigByHostId(Guid hostId);

    /**
     * Attaches the <code>network</code> to the allowed network list of the specified vfs config
     *
     * @param vfsConfigId
     *            host nic vfs config id
     * @param networkId
     *            network id
     */
    void addNetwork(Guid vfsConfigId, Guid networkId);

    /**
     * Removes the <code>network</code> from the allowed network list of the specified vfs config
     *
     * @param vfsConfigId
     *            host nic vfs config id
     * @param networkId
     *            network id
     */
    void removeNetwork(Guid vfsConfigId, Guid networkId);

    /**
     * Adds the <code>label</code> to the allowed labels list of the specified vfs config
     *
     * @param vfsConfigId
     *            host nic vfs config id
     * @param label
     */
    void addLabel(Guid vfsConfigId, String label);

    /**
     * Removes the <code>label</code> from the allowed labels list of the specified vfs config
     *
     * @param vfsConfigId
     *            host nic vfs config id
     * @param label
     */
    void removeLabel(Guid vfsConfigId, String label);
}
