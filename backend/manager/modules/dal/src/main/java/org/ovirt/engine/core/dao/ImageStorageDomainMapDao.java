package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMapId;
import org.ovirt.engine.core.compat.Guid;

/**
 * Interface for having DB related operations on {@link ImageStorageDomainMap} entities.
 */
public interface ImageStorageDomainMapDao extends GenericDao<ImageStorageDomainMap, ImageStorageDomainMapId> {

    /**
     * Removes the {@link ImageStorageDomainMap} entries that
     * have the given image Id.
     *
     * @param imageId
     *            Id of {@link org.ovirt.engine.core.common.businessentities.storage.DiskImage} that the removed
     *            entries were created for.
     */
    void remove(Guid imageId);

    /**
     * Gets a list of {@link ImageStorageDomainMap} entries that
     * have the given id of {@link org.ovirt.engine.core.common.businessentities.StorageDomain}.
     *
     * @param storageDomainId
     *            ID of {@link org.ovirt.engine.core.common.businessentities.StorageDomain} entity that the returned
     *            entities are associated with.
     * @return list of entities
     */
    List<ImageStorageDomainMap> getAllByStorageDomainId(Guid storageDomainId);

    /**
     * Gets a list of {@link ImageStorageDomainMap} entries that
     * have the given id of {@link org.ovirt.engine.core.common.businessentities.storage.DiskImage} entity.
     *
     * @param imageId
     *            ID of {@link org.ovirt.engine.core.common.businessentities.storage.DiskImage} entity that the
     *            returned entities are associated with.
     * @return list of entities
     */
    List<ImageStorageDomainMap> getAllByImageId(Guid imageId);

    /**
     * updates images quota of a specific disk on a specific storage domain
     */
    void updateQuotaForImageAndSnapshots(Guid diskId, Guid storageDomainId, Guid quotaId);

    /**
     * updates images disk profile of a specific disk on a specific storage domain
     */
    void updateDiskProfileByImageGroupIdAndStorageDomainId(Guid diskId, Guid storageDomainId, Guid diskProfileId);
}
