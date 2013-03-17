package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>StorageServerConnectionDAO</code> defines a type that performs CRUD operations on instances of
 * {@link StorageServerConnections}.
 *
 *
 */
public interface StorageServerConnectionDAO extends DAO {
    /**
     * Retrieves the connection with the specified id.
     *
     * @param id
     *            the connection id
     * @return the connection
     */
    StorageServerConnections get(String id);

    /**
     * Retrieves the connection for the given iqn.
     *
     * @param iqn
     *            the iqn
     * @return the connection
     */
    StorageServerConnections getForIqn(String iqn);

    /**
     * Retrieves all connections of Active/Unknown/InActive domains in the specified storage pool.
     *
     * @param pool
     *            the storage pool
     * @return the list of connections
     */
    List<StorageServerConnections> getAllConnectableStorageSeverConnection(Guid pool);

    /**
     * Retrieves all connections for the specified volume group.
     *
     * @param group
     *            the volume group
     * @return the list of connections
     */
    List<StorageServerConnections> getAllForVolumeGroup(String group);

    /**
     * Retrieves all connections for the specified storage.
     *
     * @param storage
     *            the storage
     * @return the list of connections
     */
    List<StorageServerConnections> getAllForStorage(String storage);

    /**
     * Retrieves all connections for the specified Lun.
     *
     * @param lunId
     *            the lun
     * @return the list of connections
     */
    List<StorageServerConnections> getAllForLun(String lunId);

    /**
     * Retrieves all connections for the specified connection.
     *
     * @param connection
     *            the connection
     * @return the list of connections
     */
    List<StorageServerConnections> getAllForConnection(
            StorageServerConnections connection);

    /**
     * Saves the specified connection.
     *
     * @param connection
     *            the connection
     */
    void save(StorageServerConnections connection);

    /**
     * Updates the specified connection.
     *
     * @param connection
     *            the connection
     */
    void update(StorageServerConnections connection);

    /**
     * Removes the connection with the specified id.
     *
     * @param id
     *            the connection id
     */
    void remove(String id);
}
