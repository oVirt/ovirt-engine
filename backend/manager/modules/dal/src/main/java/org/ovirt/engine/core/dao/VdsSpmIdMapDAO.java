package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.vds_spm_id_map;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VdsSpmIdMapDAO</code> defines a type that performs CRUD operations on instances of {@link vds_spm_iso_map}.
 */
public interface VdsSpmIdMapDAO extends GenericDao<vds_spm_id_map, Guid> {
    /**
     * Gets the map for a given vds id
     *
     * @param vdsId vds id
     * @return vds_spm_id_map
     */
    vds_spm_id_map get(Guid vdsId);

    /**
     * Gets the map for a given storage pool and vds id
     *
     * @param storagePoolId storage pool id
     * @param spmId vds spm id
     * @return vds_spm_id_map
     */
    vds_spm_id_map get(Guid storagePoolId, int spmId);

    /**
     * Gets all maps for a given storage pool.
     *
     * @param storagePoolId storage pool id.
     * @return list of vds_spm_id_map
     */
    List<vds_spm_id_map> getAll(Guid storagePoolId);

    /**
     * Removes entry for the specified vds on the specified pool
     *
     * @param vdsId vds id.
     * @param storagePoolId storage pool id.
     */
    void removeByVdsAndStoragePool(Guid vdsId, Guid storagePoolId);
}
