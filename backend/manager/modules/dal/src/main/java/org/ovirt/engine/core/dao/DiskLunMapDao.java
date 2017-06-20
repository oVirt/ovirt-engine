package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.DiskLunMap;
import org.ovirt.engine.core.common.businessentities.storage.DiskLunMapId;
import org.ovirt.engine.core.compat.Guid;

public interface DiskLunMapDao extends GenericDao<DiskLunMap, DiskLunMapId> {

    DiskLunMap getDiskIdByLunId(String lunId);

    /**
     * Returns the {@link DiskLunMap} associated with the given {@code diskId}.
     * @param diskId the DiskLunMap's disk ID.
     * @return the {@link DiskLunMap} associated with the given {@code diskId}.
     */
    DiskLunMap getDiskLunMapByDiskId(Guid diskId);

    /**
     * Returns a list of {@link DiskLunMap} objects, one per each direct lun that is
     * attached to at least one vm in the storage pool noted by {@code storagePoolId}.
     * @param storagePoolId the storage pool id.
     * @return the list of {@link DiskLunMap}s.
     */
    List<DiskLunMap> getDiskLunMapsForVmsInPool(Guid storagePoolId);
}
