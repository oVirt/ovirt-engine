package org.ovirt.engine.core.dao.profiles;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.GenericDao;

public interface DiskProfileDao extends ProfilesDao<DiskProfile>, GenericDao<DiskProfile, Guid> {

    /**
     * Retrieves all disk profiles associated with the given storage domain id.
     *
     * @param storageDomainId
     *            the storage domain's ID
     * @return the list of disk profiles
     */
    List<DiskProfile> getAllForStorageDomain(Guid storageDomainId);

    /**
     * set null in qos field for all disk profiles attached to storage domain
     * (when detaching SD for data center, we should remove any attachment to qos, which is part of the
     * old data center)
     *
     * @param storageDomainId
     *            Detached storage domain id
     */
    void nullifyQosForStorageDomain(Guid storageDomainId);

    /**
     * Retrieves all disk profiles associated with the given storage domain id, according user's permissions.
     *
     * @param storageDomainId
     *            the storage domain's ID
     * @param userId
     *            the user's ID
     * @param isFiltered
     *            indicating whether the results should be filtered according to the user's permissions
     * @return the list of disk profiles
     */
    List<DiskProfile> getAllForStorageDomain(Guid storageDomainId, Guid userId, boolean isFiltered);

}
