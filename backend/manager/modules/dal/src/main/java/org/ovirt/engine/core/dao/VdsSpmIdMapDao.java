package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsSpmIdMap;
import org.ovirt.engine.core.compat.Guid;

/**
 * {@code VdsSpmIdMapDao} defines a type that performs CRUD operations on instances of {@link VdsSpmIdMap}.
 */
public interface VdsSpmIdMapDao extends GenericDao<VdsSpmIdMap, Guid> {
    /**
     * Gets the map for a given vds id
     *
     * @param vdsId vds id
     * @return VdsSpmIdMap
     */
    VdsSpmIdMap get(Guid vdsId);

    /**
     * Gets the map for a given storage pool and vds id
     *
     * @param storagePoolId storage pool id
     * @param spmId vds spm id
     * @return VdsSpmIdMap
     */
    VdsSpmIdMap get(Guid storagePoolId, int spmId);

    /**
     * Gets all maps for a given storage pool.
     *
     * @param storagePoolId storage pool id.
     * @return list of VdsSpmIdMap
     */
    List<VdsSpmIdMap> getAll(Guid storagePoolId);

    /**
     * Removes entry for the specified vds on the specified pool
     *
     * @param vdsId vds id.
     * @param storagePoolId storage pool id.
     */
    void removeByVdsAndStoragePool(Guid vdsId, Guid storagePoolId);
}
