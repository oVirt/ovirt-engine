package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainOvfInfoStatus;
import org.ovirt.engine.core.compat.Guid;


public interface StorageDomainOvfInfoDao extends GenericDao<StorageDomainOvfInfo, Guid> {
    /**
     * Updates the ovf updated flag of the given domain ids to the given values
     *
     *
     * @param ids
     *            - storage domains ids
     * @param status
     *            - ovf info status of the given domains
     * @param exceptStatus
     *            - ovf info status to leave as is when updating
     */
    public void updateOvfUpdatedInfo(Collection<Guid> ids, StorageDomainOvfInfoStatus status, StorageDomainOvfInfoStatus exceptStatus);

    /**
     * Get the storage domain ids with the given ovfs stored on them
     *
     * @param ovfIds
     *          - ovf ids collection
     */
    public List<Guid> loadStorageDomainIdsForOvfIds(Collection<Guid> ovfIds);

    /**
     * Get the StorageDomainOvfInfo records for the domain with the given id
     *
     * @param guid
     *          - domain id
     */
    public List<StorageDomainOvfInfo> getAllForDomain(Guid guid);
}
