package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaStorageProperties;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroupProperties;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * <code>QuotaDAODbFacadeImpl</code> implements the calling to quota stored procedures (@see QuotaDAO).
 */
public class QuotaDAODbFacadeImpl extends BaseDAODbFacade implements QuotaDAO {

    /**
     * Save <code>Quota</code> entity with specific <code>Quota</code> storage and <code>Quota</code> vdsGroup
     * limitation list.
     */
    @Override
    public void save(Quota quota) {
        saveGlobalQuota(quota);
        saveStorageSpecificQuotas(quota);
        saveVdsGroupSpecificQuotas(quota);
    }

    /**
     * Get <code>Quota</code> by name and storage pool id.
     *
     * @param quotaName
     *            - The quota name to find.
     * @param storagePoolId
     *            - The storage pool id that the quota is being searched in.
     * @return The quota entity that was found.
     */
    public Quota getQuotaByQuotaName(String quotaName, Guid storagePoolId) {
        MapSqlParameterSource quotaParameterSource = getCustomMapSqlParameterSource();
        quotaParameterSource.addValue("storage_pool_id", storagePoolId).addValue("quota_name", quotaName);
        Quota quotaEntity =
                getCallsHandler().executeRead("GetQuotaByQuotaName", getQuotaFromResultSet(), quotaParameterSource);
        return quotaEntity;
    }

    /**
     * Get specific limitation for <code>VdsGroup</code>.
     *
     * @param vdsGroupId
     *            - The vds group id, if null returns all the vds group limitations in the storage pool.
     * @param quotaId
     *            - The <code>Quota</code> id
     * @return List of QuotaStorage
     */
    public List<QuotaVdsGroup> getQuotaVdsGroupByVdsGroupGuid(Guid vdsGroupId, Guid quotaId) {
        MapSqlParameterSource parameterSource =
                createQuotaIdParameterMapper(quotaId).addValue("vds_group_id", vdsGroupId);
        List<QuotaVdsGroup> quotaVdsGroupList = getCallsHandler().executeReadList("GetQuotaVdsGroupByVdsGroupGuid",
                getVdsGroupQuotaResultSet(),
                parameterSource);
        return quotaVdsGroupList;
    }

    /**
     * Get specific limitation for storage domain.
     *
     * @param storageId
     *            - The storage id, if null returns all the storages limitation in the storage pool.
     * @param quotaId
     *            - The quota id
     * @return List of QuotaStorage
     */
    public List<QuotaStorage> getQuotaStorageByStorageGuid(Guid storageId, Guid quotaId) {
        MapSqlParameterSource parameterSource = createQuotaIdParameterMapper(quotaId).addValue("storage_id", storageId);
        List<QuotaStorage> quotaStorageList = getCallsHandler().executeReadList("GetQuotaStorageByStorageGuid",
                getQuotaStorageResultSet(),
                parameterSource);
        return quotaStorageList;
    }

    /**
     * Get full <code>Quota</code> entity.
     */
    public Quota getById(Guid quotaId) {
        MapSqlParameterSource parameterSource = createQuotaIdParameterMapper(quotaId);

        Quota quotaEntity =
                getCallsHandler().executeRead("GetQuotaByQuotaGuid", getQuotaFromResultSet(), parameterSource);

        if (quotaEntity != null) {
            quotaEntity.setQuotaVdsGroups(getQuotaVdsGroupByQuotaGuid(quotaId));
            quotaEntity.setQuotaStorages(getQuotaStorageByQuotaGuid(quotaId));
        }
        return quotaEntity;
    }

    /**
     * Get all quota storages which belong to quota with quotaId.
     */
    public List<QuotaStorage> getQuotaStorageByQuotaGuid(Guid quotaId) {
        MapSqlParameterSource parameterSource = createQuotaIdParameterMapper(quotaId);
        return getCallsHandler().executeReadList("GetQuotaStorageByQuotaGuid",
                getQuotaStorageResultSet(),
                parameterSource);
    }

    /**
     * Get all quota Vds groups, which belong to quota with quotaId.
     */
    public List<QuotaVdsGroup> getQuotaVdsGroupByQuotaGuid(Guid quotaId) {
        MapSqlParameterSource parameterSource = createQuotaIdParameterMapper(quotaId);
        return getCallsHandler().executeReadList("GetQuotaVdsGroupByQuotaGuid",
                getVdsGroupQuotaResultSet(),
                parameterSource);
    }

    /**
     * Remove quota with quota id.
     */
    public void remove(Guid id) {
        getCallsHandler().executeModification("DeleteQuotaByQuotaGuid",
                createQuotaIdParameterMapper(id));
    }

    /**
     * Update <Code>quota</Code>, by updating the quota meta data and remove all its limitations and add the limitations
     * from the quota parameter.
     */
    public void update(Quota quota) {
        getCallsHandler().executeModification("UpdateQuotaMetaData",
                createQuotaMetaDataParameterMapper(quota));
        getCallsHandler().executeModification("DeleteQuotaLimitationByQuotaGuid",
                createQuotaIdParameterMapper(quota.getId()));
        getCallsHandler().executeModification("InsertQuotaLimitation", getFullQuotaParameterMap(quota));
        saveStorageSpecificQuotas(quota);
        saveVdsGroupSpecificQuotas(quota);
    }

    /**
     * Return initialized entity with quota Vds group result set.
     */
    private <T extends QuotaStorageProperties> ParameterizedRowMapper<QuotaVdsGroup> getVdsGroupQuotaResultSet() {
        ParameterizedRowMapper<QuotaVdsGroup> mapperQuotaLimitation = new ParameterizedRowMapper<QuotaVdsGroup>() {
            @Override
            public QuotaVdsGroup mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                QuotaVdsGroup entity = new QuotaVdsGroup();
                entity.setQuotaId(Guid.createGuidFromString(rs.getString("quota_id")));
                entity.setQuotaVdsGroupId(Guid.createGuidFromString(rs.getString("quota_vds_group_id")));
                entity.setVdsGroupId(Guid.createGuidFromString(rs.getString("vds_group_id")));
                entity.setVdsGroupName(rs.getString("vds_group_name"));
                mapVdsGroupResultSet(rs, entity);
                return entity;
            }
        };
        return mapperQuotaLimitation;
    }

    /**
     * Returns initialized entity with quota Storage result set.
     */
    private ParameterizedRowMapper<QuotaStorage> getQuotaStorageResultSet() {
        ParameterizedRowMapper<QuotaStorage> mapperQuotaLimitation = new ParameterizedRowMapper<QuotaStorage>() {
            @Override
            public QuotaStorage mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                QuotaStorage entity = new QuotaStorage();
                entity.setQuotaId(Guid.createGuidFromString(rs.getString("quota_id")));
                entity.setQuotaStorageId(Guid.createGuidFromString(rs.getString("quota_storage_id")));
                entity.setStorageId(Guid.createGuidFromString(rs.getString("storage_id")));
                entity.setStorageName(rs.getString("storage_name"));
                mapStorageResultSet(rs, entity);
                return entity;
            }
        };
        return mapperQuotaLimitation;
    }

    /**
     * Returns initialized entity with quota meta data result set.
     */
    private ParameterizedRowMapper<Quota> getQuotaFromResultSet() {
        ParameterizedRowMapper<Quota> mapper = new ParameterizedRowMapper<Quota>() {
            @Override
            public Quota mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                Quota entity = new Quota();
                entity.setId(Guid.createGuidFromString(rs.getString("quota_id")));
                entity.setStoragePoolId(Guid.createGuidFromString(rs.getString("storage_pool_id")));
                entity.setStoragePoolName(rs.getString("storage_pool_name"));
                entity.setQuotaName((String) rs.getObject("quota_name"));
                entity.setDescription((String) rs.getObject("description"));
                entity.setThresholdVdsGroupPercentage((Integer) rs.getObject("threshold_vds_group_percentage"));
                entity.setThresholdStoragePercentage((Integer) rs.getObject("threshold_storage_percentage"));
                entity.setGraceVdsGroupPercentage((Integer) rs.getObject("grace_vds_group_percentage"));
                entity.setGraceStoragePercentage((Integer) rs.getObject("grace_storage_percentage"));
                mapVdsGroupResultSet(rs, entity);
                mapStorageResultSet(rs, entity);
                return entity;
            }
        };
        return mapper;
    }

    private void mapStorageResultSet(ResultSet rs, QuotaStorageProperties entity) throws SQLException {
        entity.setStorageSizeGB((Long) rs.getObject("storage_size_gb"));
        entity.setStorageSizeGBUsage((Double) rs.getObject("storage_size_gb_usage"));
    }

    private void mapVdsGroupResultSet(ResultSet rs, QuotaVdsGroupProperties entity) throws SQLException {
        entity.setMemSizeMB((Long) rs.getObject("mem_size_mb"));
        entity.setMemSizeMBUsage((Long) rs.getObject("mem_size_mb_usage"));
        entity.setVirtualCpu((Integer) rs.getObject("virtual_cpu"));
        entity.setVirtualCpuUsage((Integer) rs.getObject("virtual_cpu_usage"));
    }

    private MapSqlParameterSource createQuotaIdParameterMapper(Guid quotaId) {
        MapSqlParameterSource quotaParameterSource = getCustomMapSqlParameterSource()
                .addValue("id", quotaId);
        return quotaParameterSource;
    }

    /**
     * Build quota storage parameter map, for quota limitation table, to indicate specific limitation on storage domain.
     *
     * @param quotaId
     *            - The global quota id which the storage is referencing to
     * @param quotaStorage
     *            - The business entity which reflects the limitation on the specific storage.
     * @return - Parameter Map
     */
    private MapSqlParameterSource getQuotaStorageParameterMap(Guid quotaId, QuotaStorage quotaStorage) {
        MapSqlParameterSource storageQuotaParameterMap =
                createQuotaIdParameterMapper(quotaStorage.getQuotaStorageId()).addValue("quota_id",
                        quotaId)
                        .addValue("storage_id", quotaStorage.getStorageId())
                        .addValue("vds_group_id", null);
        addQuotaStorageLimitMapper(quotaStorage, storageQuotaParameterMap);

        // Add null to storage parameter map to indicate the limit is only for specific storage.
        addQuotaVdsGroupLimitMapper(null, storageQuotaParameterMap);
        return storageQuotaParameterMap;
    }

    /**
     * Build quota vds group parameter map, for quota limitation table, to indicate specific limitation on specific
     * <code>VdsGroup</code>.
     *
     * @param quotaId
     *            - The global quota id which the <code>VdsGroup</code> is referencing to
     * @param quotaVdsGroup
     *            - The business entity which reflects the limitation on the specific vdsGroup.
     * @return - <code>VdsGroup</code> Parameter Map
     */
    private MapSqlParameterSource getQuotaVdsGroupParameterMap(Guid quotaId, QuotaVdsGroup quotaVdsGroup) {
        MapSqlParameterSource vdsGroupQuotaParameterMap =
                createQuotaIdParameterMapper(quotaVdsGroup.getQuotaVdsGroupId()).addValue("quota_id", quotaId)
                        .addValue("vds_group_id", quotaVdsGroup.getVdsGroupId())
                        .addValue("storage_id", null);

        // Add null to vds group parameter map to indicate the limit is only for specific vdsGroup.
        addQuotaStorageLimitMapper(null, vdsGroupQuotaParameterMap);
        addQuotaVdsGroupLimitMapper(quotaVdsGroup, vdsGroupQuotaParameterMap);
        return vdsGroupQuotaParameterMap;
    }

    /**
     * Build parameter map, for quota limitation table, to indicate global limitation on <code>StoragePool</code>.
     *
     * @param quota
     *            - The global quota.
     * @return - Global quota Parameter Map.
     */
    private MapSqlParameterSource getFullQuotaParameterMap(Quota quota) {
        MapSqlParameterSource quotaParameterMap =
                getCustomMapSqlParameterSource()
                        .addValue("id", quota.getId())
                        .addValue("quota_id", quota.getId())
                        .addValue("vds_group_id", null)
                        .addValue("storage_id", null);
        addQuotaStorageLimitMapper(quota, quotaParameterMap);
        addQuotaVdsGroupLimitMapper(quota, quotaParameterMap);
        return quotaParameterMap;
    }

    private MapSqlParameterSource addQuotaStorageLimitMapper(QuotaStorageProperties quotaStorage,
            MapSqlParameterSource map) {
        return map.addValue("storage_size_gb",
                quotaStorage != null ? quotaStorage.getStorageSizeGB() : null);
    }

    private MapSqlParameterSource addQuotaVdsGroupLimitMapper(QuotaVdsGroupProperties quotaVdsGroup,
            MapSqlParameterSource map) {
        return map.addValue("virtual_cpu", quotaVdsGroup != null ? quotaVdsGroup.getVirtualCpu() : null)
                .addValue("mem_size_mb", quotaVdsGroup != null ? quotaVdsGroup.getMemSizeMB() : null);
    }

    private MapSqlParameterSource createQuotaMetaDataParameterMapper(Quota quota) {
        return createQuotaIdParameterMapper(quota.getId()).addValue("storage_pool_id", quota.getStoragePoolId())
                .addValue("quota_name", quota.getQuotaName())
                .addValue("description", quota.getDescription())
                .addValue("threshold_vds_group_percentage", quota.getThresholdVdsGroupPercentage())
                .addValue("threshold_storage_percentage", quota.getThresholdStoragePercentage())
                .addValue("grace_vds_group_percentage", quota.getGraceVdsGroupPercentage())
                .addValue("grace_storage_percentage", quota.getGraceStoragePercentage());
    }

    private void saveGlobalQuota(Quota quota) {
        getCallsHandler().executeModification("InsertQuota", createQuotaMetaDataParameterMapper(quota));
        getCallsHandler().executeModification("InsertQuotaLimitation", getFullQuotaParameterMap(quota));
    }

    private void saveVdsGroupSpecificQuotas(Quota quota) {
        // Add quota specific vds group limitations.
        for (QuotaVdsGroup quotaVdsGroup : quota.getQuotaVdsGroups()) {
            getCallsHandler().executeModification("InsertQuotaLimitation",
                    getQuotaVdsGroupParameterMap(quota.getId(), quotaVdsGroup));
        }
    }

    private void saveStorageSpecificQuotas(Quota quota) {
        // Add quota specific storage domains limitations.
        for (QuotaStorage quotaStorage : quota.getQuotaStorages()) {
            getCallsHandler().executeModification("InsertQuotaLimitation",
                    getQuotaStorageParameterMap(quota.getId(), quotaStorage));
        }
    }
}
