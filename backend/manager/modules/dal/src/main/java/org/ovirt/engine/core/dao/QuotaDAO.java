package org.ovirt.engine.core.dao;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>QuotaDAO</code> is an interface for operations implements the calling to quota stored procedures. (@see
 * QuotaDAODbFacadeImpl)
 */
public interface QuotaDAO extends DAO {

    /**
     * Saves the Quota definition.
     *
     * @param quota
     *            the Quota
     */
    public void save(Quota quota);

    /**
     * Saves Quota by Quota Id.
     *
     * @param id
     *            the Quota Id
     */
    public Quota getById(Guid id);

    /**
     * Removes the Quota with the specified id.
     *
     * @param id
     *            the quota id
     */
    public void remove(Guid id);

    /**
     * Update Quota, by re-inserting its sub Quota lists, and update the global quota parameters.
     *
     * @param quota
     *            - The Quota to update.
     */
    public void update(Quota quota);

    /**
     * Get specific limitation for <code>VdsGroup</code>.
     *
     * @param vdsGroupId
     *            - The vds group id, if null returns all the vds group limitations in the storage pool.
     * @param quotaId
     *            - The <code>Quota</code> id
     * @return List of QuotaStorage
     */
    public List<QuotaVdsGroup> getQuotaVdsGroupByVdsGroupGuid(Guid vdsGroupId, Guid quotaId);

    /**
     * Get specific limitation for storage domain.
     *
     * @param storageId
     *            - The storage id, if null returns all the storages limitation in the storage pool.
     * @param quotaId
     *            - The quota id
     * @return List of QuotaStorage
     */
    public List<QuotaStorage> getQuotaStorageByStorageGuid(Guid storageId, Guid quotaId);

    /**
     * Get <code>Quota</code> by name and storage pool id.
     *
     * @param quotaName
     *            - The quota name to find.
     * @param storagePoolId
     *            - The storage pool id that the quota is being searched in.
     * @return The quota entity that was found.
     */
    public Quota getQuotaByQuotaName(String quotaName, Guid storagePoolId);

    /**
     * Get all quota storages which belong to quota with quotaId.
     */
    public List<QuotaStorage> getQuotaStorageByQuotaGuid(Guid quotaId);

    /**
     * Get all quota vds groups, which belong to quota with quotaId.
     */
    public List<QuotaVdsGroup> getQuotaVdsGroupByQuotaGuid(Guid quotaId);
}
