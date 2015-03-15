package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.storage.LUN_storage_server_connection_map_id;

/**
 * <code>StorageServerConnectionLunMapDAO</code> defines a type that performs CRUD operations on instances of
 * {@link LUN_storage_server_connection_map}.
 *
 *
 */
public interface StorageServerConnectionLunMapDAO extends GenericDao<LUN_storage_server_connection_map, LUN_storage_server_connection_map_id> {
    /**
     * get all maps for a given LUN id.
     *
     * @param lunId
     *            LUN id.
     * @return
     */
    List<LUN_storage_server_connection_map> getAll(final String lunId);
}
