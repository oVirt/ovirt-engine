package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

/**
 * {@code QuotaDaoImpl} implements the calling to quota stored procedures ({@link QuotaDao}).
 */
@Named
@Singleton
public class QuotaDaoImpl extends BaseDao implements QuotaDao {

    /**
     * Save {@code Quota} entity with specific {@code Quota} storage and {@code Quota} cluster limitation list.
     */
    @Override
    public void save(Quota quota) {
        saveGlobalQuota(quota);
        saveStorageSpecificQuotas(quota);
        saveClusterSpecificQuotas(quota);
    }

    /**
     * Get {@code Quota} by name.
     *
     * @param quotaName
     *            - The quota name to find.
     * @param storagePoolId
     *            - Id of the storage pool to which the quota belongs
     * @return The quota entity that was found.
     */
    @Override
    public Quota getQuotaByQuotaName(String quotaName, Guid storagePoolId) {
        MapSqlParameterSource quotaParameterSource = getCustomMapSqlParameterSource();
        quotaParameterSource.addValue("quota_name", quotaName);
        quotaParameterSource.addValue("storage_pool_id", storagePoolId);
        return getCallsHandler().executeRead("GetQuotaByQuotaName", getQuotaFromResultSet(), quotaParameterSource);
    }

    /**
     * Get list of {@code Quota}s which are consumed by ad element id in storage pool (if not storage pool id not
     * null).
     *
     * @param adElementId
     *            - The user ID or group ID.
     * @param storagePoolId
     *            - The storage pool Id to search the quotas in (If null search all over the setup).
     * @param recursive
     *            - Find by
     * @return All quotas for user.
     */
    @Override
    public List<Quota> getQuotaByAdElementId(Guid adElementId, Guid storagePoolId, boolean recursive) {
        MapSqlParameterSource quotaParameterSource = getCustomMapSqlParameterSource();
        quotaParameterSource.addValue("ad_element_id", adElementId);
        quotaParameterSource.addValue("storage_pool_id", storagePoolId);
        quotaParameterSource.addValue("recursive", recursive);
        return getCallsHandler().executeReadList("GetQuotaByAdElementId",
                getQuotaMetaDataFromResultSet(),
                quotaParameterSource);
    }

    /**
     * Get specific limitation for {@code Cluster}.
     *
     * @param clusterId
     *            - The vds group id, if null returns all the vds group limitations in the storage pool.
     * @param quotaId
     *            - The {@code Quota} id
     * @return List of QuotaStorage
     */
    @Override
    public List<QuotaCluster> getQuotaClusterByClusterGuid(Guid clusterId, Guid quotaId) {
        return getQuotaClusterByClusterGuid(clusterId, quotaId, true);
    }

    /**
     * Get specific limitation for {@code Cluster}.
     *
     * @param clusterId
     *            - The vds group id, if null returns all the vds group limitations in the storage pool.
     * @param quotaId
     *            - The {@code Quota} id
     * @param allowEmpty
     *            - Whether to return empty quotas or not
     * @return List of QuotaStorage
     */
    @Override
    public List<QuotaCluster> getQuotaClusterByClusterGuid(Guid clusterId, Guid quotaId, boolean allowEmpty) {
        MapSqlParameterSource parameterSource =
                createQuotaIdParameterMapper(quotaId)
                        .addValue("cluster_id", clusterId)
                        .addValue("allow_empty", allowEmpty);
        return getCallsHandler().executeReadList("GetQuotaClusterByClusterGuid",
                getClusterQuotaResultSet(),
                parameterSource);
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
    @Override
    public List<QuotaStorage> getQuotaStorageByStorageGuid(Guid storageId, Guid quotaId) {
        return getQuotaStorageByStorageGuid(storageId, quotaId, true);
    }

    /**
     * Get specific limitation for storage domain.
     *
     * @param storageId
     *            - The storage id, if null returns all the storages limitation in the storage pool.
     * @param quotaId
     *            - The quota id
     * @param allowEmpty
     *            - Whether to return empty quotas or not
     * @return List of QuotaStorage
     */
    @Override
    public List<QuotaStorage> getQuotaStorageByStorageGuid(Guid storageId, Guid quotaId, boolean allowEmpty) {
        MapSqlParameterSource parameterSource =
                createQuotaIdParameterMapper(quotaId).addValue("storage_id", storageId).addValue("allow_empty",
                        allowEmpty);
        return getCallsHandler().executeReadList("GetQuotaStorageByStorageGuid",
                getQuotaStorageResultSet(),
                parameterSource);
    }

    @Override
    public List<QuotaStorage> getAllQuotaStorageIncludingConsumption() {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        return getCallsHandler().executeReadList("calculateAllStorageUsage",
                getQuotaStorageResultSet(),
                parameterSource);
    }

    /**
     * Returns all the Quota storages in the storage pool if v_storage_id is null, if v_storage_id is not null then a
     * specific quota storage will be returned.
     */
    @Override
    public List<Quota> getQuotaByStoragePoolGuid(Guid storagePoolId) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("storage_pool_id", storagePoolId);
        return getCallsHandler().executeReadList("GetQuotaByStoragePoolGuid",
                getQuotaFromResultSet(),
                parameterSource);
    }

    @Override
    public Quota getDefaultQuotaForStoragePool(Guid storagePoolId) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("storage_pool_id", storagePoolId);
        return getCallsHandler().executeRead("GetDefaultQuotaForStoragePool",
                getQuotaFromResultSet(),
                parameterSource);
    }

    /**
     * Get full {@code Quota} entity.
     */
    @Override
    public Quota getById(Guid quotaId) {
        MapSqlParameterSource parameterSource = createQuotaIdParameterMapper(quotaId);

        Quota quotaEntity =
                getCallsHandler().executeRead("GetQuotaByQuotaGuid", getQuotaFromResultSet(), parameterSource);

        if (quotaEntity != null) {
            quotaEntity.setQuotaClusters(getQuotaClusterByQuotaGuid(quotaId));
            quotaEntity.setQuotaStorages(getQuotaStorageByQuotaGuid(quotaId));
        }
        return quotaEntity;
    }

    @Override
    public int getQuotaCount() {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        return getCallsHandler().executeRead
                ("getQuotaCount", SingleColumnRowMapper.newInstance(Long.class), parameterSource).intValue();
    }

    /**
     * Get all the full quotas. Including consumption data. This call is very heavy and should be used really and with
     * caution. It was created to support cache initialization
     *
     * @return all quota in DB (including consumption calculation)
     */
    @Override
    public List<Quota> getAllQuotaIncludingConsumption() {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        // get thin quota (only basic quota meta data)
        List<Quota> allThinQuota = getCallsHandler().executeReadList("getAllThinQuota", getQuotaMetaDataFromResultSet(), parameterSource);

        if (!allThinQuota.isEmpty()){
            Map<Guid, Quota> allQuotaMap = new HashMap<>();
            for (Quota quota : allThinQuota) {
                allQuotaMap.put(quota.getId(), quota);
            }

            List<QuotaStorage> quotaStorageList = getAllQuotaStorageIncludingConsumption();
            List<QuotaCluster> quotaClusterList = getAllQuotaClusterIncludingConsumption();

            for (QuotaStorage quotaStorage : quotaStorageList) {
                Quota quota = allQuotaMap.get(quotaStorage.getQuotaId());
                if (quota != null) {
                    if (quotaStorage.getStorageId() == null || quotaStorage.getStorageId().equals(Guid.Empty)) {
                        quota.setGlobalQuotaStorage(quotaStorage);
                    } else {
                        if (quota.getQuotaStorages() == null) {
                            quota.setQuotaStorages(new ArrayList<>());
                        }
                        quota.getQuotaStorages().add(quotaStorage);
                    }
                }
            }

            for (QuotaCluster quotaCluster : quotaClusterList) {
                Quota quota = allQuotaMap.get(quotaCluster.getQuotaId());
                if (quota != null) {
                    if (quotaCluster.getClusterId() == null || quotaCluster.getClusterId().equals(Guid.Empty)) {
                        quota.setGlobalQuotaCluster(quotaCluster);
                    } else {
                        if (quota.getQuotaClusters() == null) {
                            quota.setQuotaClusters(new ArrayList<>());
                        }
                        quota.getQuotaClusters().add(quotaCluster);
                    }
                }
            }
        }

        // The thin quota were all filled
        return allThinQuota;
    }

    /**
     * Get all quota storages which belong to quota with quotaId.
     */
    @Override
    public List<QuotaStorage> getQuotaStorageByQuotaGuid(Guid quotaId) {
        MapSqlParameterSource parameterSource = createQuotaIdParameterMapper(quotaId);
        return getCallsHandler().executeReadList("GetQuotaStorageByQuotaGuid",
                getQuotaStorageResultSet(),
                parameterSource);
    }

    /**
     * Get all quota storages which belong to quota with quotaId.
     */
    @Override
    public List<QuotaStorage> getQuotaStorageByQuotaGuidWithGeneralDefault(Guid quotaId) {
        return getQuotaStorageByStorageGuid(null, quotaId, false);
    }

    /**
     * Get all quota Vds groups, which belong to quota with quotaId.
     */
    @Override
    public List<QuotaCluster> getQuotaClusterByQuotaGuid(Guid quotaId) {
        MapSqlParameterSource parameterSource = createQuotaIdParameterMapper(quotaId);
        return getCallsHandler().executeReadList("GetQuotaClusterByQuotaGuid",
                getClusterQuotaResultSet(),
                parameterSource);
    }

    @Override
    public List<QuotaCluster> getAllQuotaClusterIncludingConsumption() {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        return getCallsHandler().executeReadList("calculateAllClusterUsage",
                getClusterQuotaResultSet(),
                parameterSource);
    }

    /**
     * Get all quota Vds groups, which belong to quota with quotaId.
     * In case no quota Vds Groups are returned, a fictitious QuotaCluster is returned,
     * with an {@link Guid#Empty} Vds Id and a {@code null} name.
     */
    @Override
    public List<QuotaCluster> getQuotaClusterByQuotaGuidWithGeneralDefault(Guid quotaId) {
        return getQuotaClusterByClusterGuid(null, quotaId, false);
    }

    @Override
    public List<Quota> getAllRelevantQuotasForStorage(Guid storageId, long engineSessionSeqId, boolean isFiltered) {
        MapSqlParameterSource quotaParameterSource = getCustomMapSqlParameterSource();
        quotaParameterSource.addValue("storage_id", storageId)
                .addValue("engine_session_seq_id", engineSessionSeqId)
                .addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("getAllThinQuotasByStorageId",
                getQuotaMetaDataFromResultSet(),
                quotaParameterSource);
    }

    @Override
    public List<Quota> getAllRelevantQuotasForCluster(Guid clusterId, long engineSessionSeqId, boolean isFiltered) {
        MapSqlParameterSource quotaParameterSource = getCustomMapSqlParameterSource();
        quotaParameterSource.addValue("cluster_id", clusterId)
                .addValue("engine_session_seq_id", engineSessionSeqId)
                .addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("getAllThinQuotasByClusterId",
                getQuotaMetaDataFromResultSet(),
                quotaParameterSource);
    }

    /**
     * Remove quota with quota id.
     */
    @Override
    public void remove(Guid id) {
        getCallsHandler().executeModification("DeleteQuotaByQuotaGuid",
                createQuotaIdParameterMapper(id));
    }

    /**
     * Update {@code Quota}, by updating the quota meta data and remove all its limitations and add the limitations
     * from the quota parameter.
     */
    @Override
    public void update(Quota quota) {
        getCallsHandler().executeModification("UpdateQuotaMetaData",
                createQuotaMetaDataParameterMapper(quota));
        getCallsHandler().executeModification("DeleteQuotaLimitationByQuotaGuid",
                createQuotaIdParameterMapper(quota.getId()));
        getCallsHandler().executeModification("InsertQuotaLimitation", getFullQuotaParameterMap(quota));
        saveStorageSpecificQuotas(quota);
        saveClusterSpecificQuotas(quota);
    }

    /**
     * Return initialized entity with quota Vds group result set.
     */
    private RowMapper<QuotaCluster> getClusterQuotaResultSet() {
        return (rs, rowNum) -> {
            QuotaCluster entity = new QuotaCluster();
            entity.setQuotaId(getGuidDefaultEmpty(rs, "quota_id"));
            entity.setQuotaClusterId(getGuidDefaultEmpty(rs, "quota_cluster_id"));
            entity.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));
            entity.setClusterName(rs.getString("cluster_name"));
            entity.setMemSizeMB((Long) rs.getObject("mem_size_mb"));
            entity.setMemSizeMBUsage((Long) rs.getObject("mem_size_mb_usage"));
            entity.setVirtualCpu((Integer) rs.getObject("virtual_cpu"));
            entity.setVirtualCpuUsage((Integer) rs.getObject("virtual_cpu_usage"));

            return entity;
        };
    }

    /**
     * Returns initialized entity with quota Storage result set.
     */
    private RowMapper<QuotaStorage> getQuotaStorageResultSet() {
        return (rs, rowNum) -> {
            QuotaStorage entity = new QuotaStorage();
            entity.setQuotaId(getGuidDefaultEmpty(rs, "quota_id"));
            entity.setQuotaStorageId(getGuidDefaultEmpty(rs, "quota_storage_id"));
            entity.setStorageId(getGuidDefaultEmpty(rs, "storage_id"));
            entity.setStorageName(rs.getString("storage_name"));
            entity.setStorageSizeGB((Long) rs.getObject("storage_size_gb"));
            entity.setStorageSizeGBUsage((Double) rs.getObject("storage_size_gb_usage"));
            return entity;
        };
    }

    /**
     * Returns initialized entity with quota result set.
     */
    private RowMapper<Quota> getQuotaFromResultSet() {
        return (rs, rowNum) -> {
            Quota entity = getQuotaMetaDataFromResultSet(rs);

            // Check if memory size is not null, this is an indication if global limitation for vds group exists or
            // not, since global limitation must be for all the quota vds group parameters.
            if (rs.getObject("mem_size_mb") != null) {
                // Set global vds group quota.
                QuotaCluster clusterEntity = new QuotaCluster();
                clusterEntity.setMemSizeMB((Long) rs.getObject("mem_size_mb"));
                clusterEntity.setMemSizeMBUsage((Long) rs.getObject("mem_size_mb_usage"));
                clusterEntity.setVirtualCpu((Integer) rs.getObject("virtual_cpu"));
                clusterEntity.setVirtualCpuUsage((Integer) rs.getObject("virtual_cpu_usage"));
                entity.setGlobalQuotaCluster(clusterEntity);
            }

            // Check if storage limit size is not null, this is an indication if global limitation for storage
            // exists or
            // not.
            if (rs.getObject("storage_size_gb") != null) {
                // Set global storage quota.
                QuotaStorage storageEntity = new QuotaStorage();
                storageEntity.setStorageSizeGB((Long) rs.getObject("storage_size_gb"));
                storageEntity.setStorageSizeGBUsage((Double) rs.getObject("storage_size_gb_usage"));
                entity.setGlobalQuotaStorage(storageEntity);
            }

            return entity;
        };
    }

    /**
     * Returns initialized entity with quota meta data result set.
     */
    private RowMapper<Quota> getQuotaMetaDataFromResultSet() {
        return (rs, rowNum) -> getQuotaMetaDataFromResultSet(rs);
    }

    private Quota getQuotaMetaDataFromResultSet(ResultSet rs) throws SQLException {
        Quota entity = new Quota();
        entity.setId(getGuidDefaultEmpty(rs, "quota_id"));
        entity.setStoragePoolId(getGuidDefaultEmpty(rs, "storage_pool_id"));
        entity.setStoragePoolName(rs.getString("storage_pool_name"));
        entity.setQuotaName((String) rs.getObject("quota_name"));
        entity.setDescription((String) rs.getObject("description"));
        entity.setThresholdClusterPercentage((Integer) rs.getObject("threshold_cluster_percentage"));
        entity.setThresholdStoragePercentage((Integer) rs.getObject("threshold_storage_percentage"));
        entity.setGraceClusterPercentage((Integer) rs.getObject("grace_cluster_percentage"));
        entity.setGraceStoragePercentage((Integer) rs.getObject("grace_storage_percentage"));
        entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
        entity.setDefault(rs.getBoolean("is_default"));
        return entity;
    }

    private MapSqlParameterSource createQuotaIdParameterMapper(Guid quotaId) {
        return getCustomMapSqlParameterSource().addValue("id", quotaId);
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
        return createQuotaIdParameterMapper(quotaStorage.getQuotaStorageId()).addValue("quota_id",
                quotaId)
                .addValue("storage_id", quotaStorage.getStorageId())
                .addValue("cluster_id", null)
                .addValue("storage_size_gb", quotaStorage.getStorageSizeGB())
                .addValue("virtual_cpu", null)
                .addValue("mem_size_mb", null);
    }

    /**
     * Build quota vds group parameter map, for quota limitation table, to indicate specific limitation on specific
     * {@code Cluster}.
     *
     * @param quotaId
     *            - The global quota id which the {@code Cluster} is referencing to
     * @param quotaCluster
     *            - The business entity which reflects the limitation on the specific cluster.
     * @return - {@code Cluster} Parameter Map
     */
    private MapSqlParameterSource getQuotaClusterParameterMap(Guid quotaId, QuotaCluster quotaCluster) {
        return createQuotaIdParameterMapper(quotaCluster.getQuotaClusterId()).addValue("quota_id", quotaId)
                .addValue("cluster_id", quotaCluster.getClusterId())
                .addValue("storage_id", null)
                .addValue("storage_size_gb", null)
                .addValue("virtual_cpu", quotaCluster.getVirtualCpu())
                .addValue("mem_size_mb", quotaCluster.getMemSizeMB());
    }

    /**
     * Build parameter map, for quota limitation table, to indicate global limitation on {@code StoragePool}.
     *
     * @param quota
     *            - The global quota.
     * @return - Global quota Parameter Map.
     */
    private MapSqlParameterSource getFullQuotaParameterMap(Quota quota) {
        return getCustomMapSqlParameterSource()
                .addValue("id", quota.getId())
                .addValue("quota_id", quota.getId())
                .addValue("cluster_id", null)
                .addValue("storage_id", null)
                .addValue("storage_size_gb",
                        quota.getGlobalQuotaStorage() != null ? quota.getGlobalQuotaStorage()
                                .getStorageSizeGB() : null)
                .addValue("virtual_cpu",
                        quota.getGlobalQuotaCluster() != null ? quota.getGlobalQuotaCluster().getVirtualCpu()
                                : null)
                .addValue("mem_size_mb",
                        quota.getGlobalQuotaCluster() != null ? quota.getGlobalQuotaCluster().getMemSizeMB()
                                : null);
    }

    private MapSqlParameterSource createQuotaMetaDataParameterMapper(Quota quota) {
        return createQuotaIdParameterMapper(quota.getId()).addValue("storage_pool_id", quota.getStoragePoolId())
                .addValue("quota_name", quota.getQuotaName())
                .addValue("description", quota.getDescription())
                .addValue("threshold_cluster_percentage", quota.getThresholdClusterPercentage())
                .addValue("threshold_storage_percentage", quota.getThresholdStoragePercentage())
                .addValue("grace_cluster_percentage", quota.getGraceClusterPercentage())
                .addValue("grace_storage_percentage", quota.getGraceStoragePercentage())
                .addValue("is_default", quota.isDefault());
    }

    private void saveGlobalQuota(Quota quota) {
        getCallsHandler().executeModification("InsertQuota", createQuotaMetaDataParameterMapper(quota));
        getCallsHandler().executeModification("InsertQuotaLimitation", getFullQuotaParameterMap(quota));
    }

    private void saveClusterSpecificQuotas(Quota quota) {
        // Add quota specific vds group limitations.
        for (QuotaCluster quotaCluster : quota.getQuotaClusters()) {
            getCallsHandler().executeModification("InsertQuotaLimitation",
                    getQuotaClusterParameterMap(quota.getId(), quotaCluster));
        }
    }

    private void saveStorageSpecificQuotas(Quota quota) {
        // Add quota specific storage domains limitations.
        for (QuotaStorage quotaStorage : quota.getQuotaStorages()) {
            getCallsHandler().executeModification("InsertQuotaLimitation",
                    getQuotaStorageParameterMap(quota.getId(), quotaStorage));
        }
    }

    @Override
    public List<Quota> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, getQuotaMetaDataFromResultSet());
    }

    @Override
    public boolean isQuotaInUse(Quota quota){

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("quota_id", quota.getId());

        Map<String, Object> dbResults =
                new SimpleJdbcCall(getJdbcTemplate()).withFunctionName("IsQuotaInUse").execute(
                        parameterSource);

        String resultKey = getDialect().getFunctionReturnKey();
        return dbResults.get(resultKey) != null && (Boolean) dbResults.get(resultKey);
    }

    @Override
    public List<Integer> getNonCountableQutoaVmStatuses() {
        return getCallsHandler().executeReadList
                ("getNonCountableQutoaVmStatuses", SingleColumnRowMapper.newInstance(Integer.class),
                        getCustomMapSqlParameterSource());
    }
}
