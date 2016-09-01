package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMapId;

/**
 * {@code StorageServerConnectionLunMapDao} defines a type that performs CRUD operations on instances of
 * {@link LUNStorageServerConnectionMap}.
 */
public interface StorageServerConnectionLunMapDao extends GenericDao<LUNStorageServerConnectionMap, LUNStorageServerConnectionMapId> {
    /**
     * get all maps for a given LUN id.
     *
     * @param lunId
     *            LUN id.
     */
    List<LUNStorageServerConnectionMap> getAll(final String lunId);
}
