package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>StorageServerConnectionDAO</code> defines a type that performs CRUD operations on instances of
 * {@link storage_server_connections}.
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
    storage_server_connections get(String id);

    /**
     * Retrieves the connection for the given iqn.
     *
     * @param iqn
     *            the iqn
     * @return the connection
     */
    storage_server_connections getForIqn(String iqn);

    /**
     * Retrieves all connections.
     *
     * @return the list of connections
     */
    List<storage_server_connections> getAll();

    /**
     * Retrieves all connections for the specified storage pool.
     *
     * @param pool
     *            the storage pool
     * @return the list of connections
     */
    List<storage_server_connections> getAllForStoragePool(Guid pool);

    /**
     * Retrieves all connections for the specified volume group.
     *
     * @param group
     *            the volume group
     * @return the list of connections
     */
    List<storage_server_connections> getAllForVolumeGroup(String group);

    /**
     * Retrieves all connections for the specified storage.
     *
     * @param storage
     *            the storage
     * @return the list of connections
     */
    List<storage_server_connections> getAllForStorage(String storage);

    /**
     * Retrieves all connections for the specified Lun.
     *
     * @param lunId
     *            the lun
     * @return the list of connections
     */
    List<storage_server_connections> getAllForLun(String lunId);

    /**
     * Retrieves all connections for the specified connection.
     *
     * @param connection
     *            the connection
     * @return the list of connections
     */
    List<storage_server_connections> getAllForConnection(
            storage_server_connections connection);

    /**
     * Saves the specified connection.
     *
     * @param connection
     *            the connection
     */
    void save(storage_server_connections connection);

    /**
     * Updates the specified connection.
     *
     * @param connection
     *            the connection
     */
    void update(storage_server_connections connection);

    /**
     * Removes the connection with the specified id.
     *
     * @param id
     *            the connection id
     */
    void remove(String id);
}
