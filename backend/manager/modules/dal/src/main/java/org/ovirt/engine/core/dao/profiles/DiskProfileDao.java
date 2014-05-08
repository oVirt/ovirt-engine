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
}
