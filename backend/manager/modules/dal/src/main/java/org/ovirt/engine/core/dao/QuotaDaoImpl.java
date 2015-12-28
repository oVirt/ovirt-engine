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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

/**
 * <code>QuotaDaoImpl</code> implements the calling to quota stored procedures (@see QuotaDao).
 */
@Named
@Singleton
public class QuotaDaoImpl extends BaseDao implements QuotaDao {

    /**
     * Save <code>Quota</code> entity with specific <code>Quota</code> storage and <code>Quota</code> cluster
     * limitation list.
     */
    @Override
    public void save(Quota quota) {
        saveGlobalQuota(quota);
        saveStorageSpecificQuotas(quota);
        saveClusterSpecificQuotas(quota);
    }

    /**
     * Get <code>Quota</code> by name.
     *
     * @param quotaName
     *            - The quota name to find.
     * @return The quota entity that was found.
     */
    @Override
    public Quota getQuotaByQuotaName(String quotaName) {
        MapSqlParameterSource quotaParameterSource = getCustomMapSqlParameterSource();
        quotaParameterSource.addValue("quota_name", quotaName);
        Quota quotaEntity =
                getCallsHandler().executeRead("GetQuotaByQuotaName", getQuotaFromResultSet(), quotaParameterSource);
        return quotaEntity;
    }

    /**
     * Get list of <code>Quotas</code> which are consumed by ad element id in storage pool (if not storage pool id not
     * null).
     *
     * @param adElementId
     *            - The user ID or group ID.
     * @param storagePoolId
     *            - The storage pool Id to search the quotas in (If null search all over the setup).
     *            @param recursive
     *            - Find by
     * @return All quotas for user.
     */
    @Override
    public List<Quota> getQuotaByAdElementId(Guid adElementId, Guid storagePoolId, boolean recursive) {
        MapSqlParameterSource quotaParameterSource = getCustomMapSqlParameterSource();
        quotaParameterSource.addValue("ad_element_id", adElementId);
        quotaParameterSource.addValue("storage_pool_id", storagePoolId);
        quotaParameterSource.addValue("recursive", recursive);
        List<Quota> quotaEntityList =
                getCallsHandler().executeReadList("GetQuotaByAdElementId",
                        getQuotaMetaDataFromResultSet(),
                        quotaParameterSource);
        return quotaEntityList;
    }

    /**
     * Get specific limitation for <code>Cluster</code>.
     *
     * @param clusterId
     *            - The vds group id, if null returns all the vds group limitations in the storage pool.
     * @param quotaId
     *            - The <code>Quota</code> id
     * @return List of QuotaStorage
     */
    @Override
    public List<QuotaCluster> getQuotaClusterByClusterGuid(Guid clusterId, Guid quotaId) {
        return getQuotaClusterByClusterGuid(clusterId, quotaId, true);
    }

    /**
     * Get specific limitation for <code>Cluster</code>.
     *
     * @param clusterId
     *            - The vds group id, if null returns all the vds group limitations in the storage pool.
     * @param quotaId
     *            - The <code>Quota</code> id
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
        List<QuotaCluster> quotaClusterList = getCallsHandler().executeReadList("GetQuotaClusterByClusterGuid",
                getClusterQuotaResultSet(),
                parameterSource);
        return quotaClusterList;
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
        List<QuotaStorage> quotaStorageList = getCallsHandler().executeReadList("GetQuotaStorageByStorageGuid",
                getQuotaStorageResultSet(),
                parameterSource);
        return quotaStorageList;
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
        List<Quota> quotaList = getCallsHandler().executeReadList("GetQuotaByStoragePoolGuid",
                getQuotaFromResultSet(),
                parameterSource);
        return quotaList;
    }

    /**
     * Get full <code>Quota</code> entity.
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

    private static RowMapper<Long> longMapper = new RowMapper<Long>() {
        @Override
        public Long mapRow(ResultSet resultSet, int i) throws SQLException {
            return (Long) resultSet.getObject(1);
        }
    };

    @Override
    public int getQuotaCount() {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        return getCallsHandler().executeRead("getQuotaCount", longMapper, parameterSource).intValue();
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

        if (allThinQuota != null && !allThinQuota.isEmpty()){
            Map<Guid, Quota> allQuotaMap = new HashMap<>();
            for (Quota quota : allThinQuota) {
                allQuotaMap.put(quota.getId(), quota);
            }

            List<QuotaStorage> quotaStorageList = getAllQuotaStorageIncludingConsumption();
            List<QuotaCluster> quotaClusterList = getAllQuotaClusterIncludingConsumption();

            if (quotaStorageList != null) {
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
            }

            if (quotaClusterList != null) {
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
     * with an {@link Guid#Empty} Vds Id and a <code>null</code> name.
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
        List<Quota> quotas =
                getCallsHandler().executeReadList("getAllThinQuotasByStorageId",
                        getQuotaMetaDataFromResultSet(),
                        quotaParameterSource);
        return quotas;
    }

    @Override
    public List<Quota> getAllRelevantQuotasForCluster(Guid clusterId, long engineSessionSeqId, boolean isFiltered) {
        MapSqlParameterSource quotaParameterSource = getCustomMapSqlParameterSource();
        quotaParameterSource.addValue("cluster_id", clusterId)
                .addValue("engine_session_seq_id", engineSessionSeqId)
                .addValue("is_filtered", isFiltered);
        List<Quota> quotas =
                getCallsHandler().executeReadList("getAllThinQuotasByClusterId",
                        getQuotaMetaDataFromResultSet(),
                        quotaParameterSource);
        return quotas;
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
     * Update <Code>quota</Code>, by updating the quota meta data and remove all its limitations and add the limitations
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
        RowMapper<QuotaCluster> mapperQuotaLimitation = new RowMapper<QuotaCluster>() {
            @Override
            public QuotaCluster mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
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
            }
        };
        return mapperQuotaLimitation;
    }

    /**
     * Returns initialized entity with quota Storage result set.
     */
    private RowMapper<QuotaStorage> getQuotaStorageResultSet() {
        RowMapper<QuotaStorage> mapperQuotaLimitation = new RowMapper<QuotaStorage>() {
            @Override
            public QuotaStorage mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                QuotaStorage entity = new QuotaStorage();
                entity.setQuotaId(getGuidDefaultEmpty(rs, "quota_id"));
                entity.setQuotaStorageId(getGuidDefaultEmpty(rs, "quota_storage_id"));
                entity.setStorageId(getGuidDefaultEmpty(rs, "storage_id"));
                entity.setStorageName(rs.getString("storage_name"));
                entity.setStorageSizeGB((Long) rs.getObject("storage_size_gb"));
                entity.setStorageSizeGBUsage((Double) rs.getObject("storage_size_gb_usage"));
                return entity;
            }
        };
        return mapperQuotaLimitation;
    }

    /**
     * Returns initialized entity with quota result set.
     */
    private RowMapper<Quota> getQuotaFromResultSet() {
        RowMapper<Quota> mapper = new RowMapper<Quota>() {
            @Override
            public Quota mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
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
            }
        };
        return mapper;
    }

    /**
     * Returns initialized entity with quota meta data result set.
     */
    private RowMapper<Quota> getQuotaMetaDataFromResultSet() {
        RowMapper<Quota> mapper = new RowMapper<Quota>() {
            @Override
            public Quota mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                return getQuotaMetaDataFromResultSet(rs);
            }
        };
        return mapper;
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
        return entity;
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
                        .addValue("cluster_id", null)
                        .addValue("storage_size_gb", quotaStorage.getStorageSizeGB())
                        .addValue("virtual_cpu", null)
                        .addValue("mem_size_mb", null);
        return storageQuotaParameterMap;
    }

    /**
     * Build quota vds group parameter map, for quota limitation table, to indicate specific limitation on specific
     * <code>Cluster</code>.
     *
     * @param quotaId
     *            - The global quota id which the <code>Cluster</code> is referencing to
     * @param quotaCluster
     *            - The business entity which reflects the limitation on the specific cluster.
     * @return - <code>Cluster</code> Parameter Map
     */
    private MapSqlParameterSource getQuotaClusterParameterMap(Guid quotaId, QuotaCluster quotaCluster) {
        MapSqlParameterSource clusterQuotaParameterMap =
                createQuotaIdParameterMapper(quotaCluster.getQuotaClusterId()).addValue("quota_id", quotaId)
                        .addValue("cluster_id", quotaCluster.getClusterId())
                        .addValue("storage_id", null)
                        .addValue("storage_size_gb", null)
                        .addValue("virtual_cpu", quotaCluster.getVirtualCpu())
                        .addValue("mem_size_mb", quotaCluster.getMemSizeMB());
        return clusterQuotaParameterMap;
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
        return quotaParameterMap;
    }

    private MapSqlParameterSource createQuotaMetaDataParameterMapper(Quota quota) {
        return createQuotaIdParameterMapper(quota.getId()).addValue("storage_pool_id", quota.getStoragePoolId())
                .addValue("quota_name", quota.getQuotaName())
                .addValue("description", quota.getDescription())
                .addValue("threshold_cluster_percentage", quota.getThresholdClusterPercentage())
                .addValue("threshold_storage_percentage", quota.getThresholdStoragePercentage())
                .addValue("grace_cluster_percentage", quota.getGraceClusterPercentage())
                .addValue("grace_storage_percentage", quota.getGraceStoragePercentage());
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
        return getCallsHandler().executeReadList("getNonCountableQutoaVmStatuses", getIntegerMapper(), null);
    }
}
