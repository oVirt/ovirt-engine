package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMapId;

/**
 * <code>StorageServerConnectionLunMapDAO</code> defines a type that performs CRUD operations on instances of
 * {@link org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap}.
 *
 *
 */
public interface StorageServerConnectionLunMapDAO extends GenericDao<LUNStorageServerConnectionMap, LUNStorageServerConnectionMapId> {
    /**
     * get all maps for a given LUN id.
     *
     * @param lunId
     *            LUN id.
     * @return
     */
    List<LUNStorageServerConnectionMap> getAll(final String lunId);
}
