package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.compat.Guid;

public interface UnregisteredOVFDataDao extends Dao {

    /**
     * Retrieves the entity with the given entityId and storage domain id.<BR/>
     * If the Storage Domain id is null, then return all the unregistered entities with the entityId.
     *
     * @param entityId
     *            The Entity Id.
     * @param storageDomainId
     *            The Storage Domain Id.
     * @return The entity instance, or {@code null} if not found.
     */
    public List<OvfEntityData> getByEntityIdAndStorageDomain(Guid entityId, Guid storageDomainId);

    /**
     * Retrieves all the entities of the given type related to the storage Domain Id.
     *
     * @param storageDomainId
     *            The Storage Domain Id.
     * @param entityType
     *            The entity type (VM/Template).
     * @return List of all the OvfEntityData related to the storage Domain Id, or an empty list if none is found.
     */
    public List<OvfEntityData> getAllForStorageDomainByEntityType(Guid storageDomainId, VmEntityType entityType);

    /**
     * Insert new entity to the unregistered table.
     */
    public void saveOVFData(OvfEntityData ovfEntityData);

    /**
     * Remove an entity from the unregistered table.
     *
     * @param entityId
     *            The Entity Id.
     * @param storageDomainId
     *            The Storage Domain Id.
     */
    public void removeEntity(Guid entityId, Guid storageDomainId);
}
