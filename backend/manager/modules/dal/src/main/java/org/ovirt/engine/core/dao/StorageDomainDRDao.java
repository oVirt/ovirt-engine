package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainDR;
import org.ovirt.engine.core.compat.Guid;

public interface StorageDomainDRDao extends Dao {

    /**
     * Retrieves the instance for a given storage domain and georep session
     *
     * @param storageDomainId
     *            the domain id
     * @param georepSessionId
     *            the associated georepsession id
     * @return the domain
     */
    public StorageDomainDR get(Guid storageDomainId, Guid georepSessionId);

    public void save(StorageDomainDR storageDomainDR);

    public void update(StorageDomainDR storageDomainDR);

    public void saveOrUpdate(StorageDomainDR storageDomainDR);

    public List<StorageDomainDR> getAllForStorageDomain(Guid storageDomainId);

    /**
     * Gets the StorageDomainDR instance associated with a geo-replication
     * session id.
     * @param geoRepSessionId geo-replication session id to query against
     * @return Domains associated with the session
     */
    public List<StorageDomainDR> getWithGeoRepSession(Guid geoRepSessionId);

    public void remove(Guid storageDomainId, Guid georepSessionId);
}
