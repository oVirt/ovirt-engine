package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

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
     * Get specific limitation for <code>VdsGroup</code>.
     *
     * @param vdsGroupId
     *            - The vds group id, if null returns all the vds group limitations in the storage pool.
     * @param quotaId
     *            - The <code>Quota</code> id
     * @return List of QuotaStorage
     */
    @Override
    public List<QuotaVdsGroup> getQuotaVdsGroupByVdsGroupGuid(Guid vdsGroupId, Guid quotaId) {
        return getQuotaVdsGroupByVdsGroupGuid(vdsGroupId, quotaId, true);
    }

    /**
     * Get specific limitation for <code>VdsGroup</code>.
     *
     * @param vdsGroupId
     *            - The vds group id, if null returns all the vds group limitations in the storage pool.
     * @param quotaId
     *            - The <code>Quota</code> id
     * @param allowEmpty
     *            - Whether to return empty quotas or not
     * @return List of QuotaStorage
     */
    @Override
    public List<QuotaVdsGroup> getQuotaVdsGroupByVdsGroupGuid(Guid vdsGroupId, Guid quotaId, boolean allowEmpty) {
        MapSqlParameterSource parameterSource =
                createQuotaIdParameterMapper(quotaId)
                        .addValue("vds_group_id", vdsGroupId)
                        .addValue("allow_empty", allowEmpty);
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
            quotaEntity.setQuotaVdsGroups(getQuotaVdsGroupByQuotaGuid(quotaId));
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
            Map<Guid, Quota> allQuotaMap = new HashMap<Guid, Quota>();
            for (Quota quota : allThinQuota) {
                allQuotaMap.put(quota.getId(), quota);
            }

            List<QuotaStorage> quotaStorageList = getAllQuotaStorageIncludingConsumption();
            List<QuotaVdsGroup> quotaVdsGroupList = getAllQuotaVdsGroupIncludingConsumption();

            if (quotaStorageList != null) {
                for (QuotaStorage quotaStorage : quotaStorageList) {
                    Quota quota = allQuotaMap.get(quotaStorage.getQuotaId());
                    if (quota != null) {
                        if (quotaStorage.getStorageId() == null || quotaStorage.getStorageId().equals(Guid.Empty)) {
                            quota.setGlobalQuotaStorage(quotaStorage);
                        } else {
                            if (quota.getQuotaStorages() == null) {
                                quota.setQuotaStorages(new ArrayList<QuotaStorage>());
                            }
                            quota.getQuotaStorages().add(quotaStorage);
                        }
                    }
                }
            }

            if (quotaVdsGroupList != null) {
                for (QuotaVdsGroup quotaVdsGroup : quotaVdsGroupList) {
                    Quota quota = allQuotaMap.get(quotaVdsGroup.getQuotaId());
                    if (quota != null) {
                        if (quotaVdsGroup.getVdsGroupId() == null || quotaVdsGroup.getVdsGroupId().equals(Guid.Empty)) {
                            quota.setGlobalQuotaVdsGroup(quotaVdsGroup);
                        } else {
                            if (quota.getQuotaVdsGroups() == null) {
                                quota.setQuotaVdsGroups(new ArrayList<QuotaVdsGroup>());
                            }
                            quota.getQuotaVdsGroups().add(quotaVdsGroup);
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
    public List<QuotaVdsGroup> getQuotaVdsGroupByQuotaGuid(Guid quotaId) {
        MapSqlParameterSource parameterSource = createQuotaIdParameterMapper(quotaId);
        return getCallsHandler().executeReadList("GetQuotaVdsGroupByQuotaGuid",
                getVdsGroupQuotaResultSet(),
                parameterSource);
    }

    @Override
    public List<QuotaVdsGroup> getAllQuotaVdsGroupIncludingConsumption() {
        MapSqlParameterSource parameterSource = new MapSqlParameterSource();
        return getCallsHandler().executeReadList("calculateAllVdsGroupUsage",
                getVdsGroupQuotaResultSet(),
                parameterSource);
    }

    /**
     * Get all quota Vds groups, which belong to quota with quotaId.
     * In case no quota Vds Groups are returned, a fictitious QuotaVdsGroup is returned,
     * with an {@link Guid#Empty} Vds Id and a <code>null</code> name.
     */
    @Override
    public List<QuotaVdsGroup> getQuotaVdsGroupByQuotaGuidWithGeneralDefault(Guid quotaId) {
        return getQuotaVdsGroupByVdsGroupGuid(null, quotaId, false);
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
    public List<Quota> getAllRelevantQuotasForVdsGroup(Guid vdsGroupId, long engineSessionSeqId, boolean isFiltered) {
        MapSqlParameterSource quotaParameterSource = getCustomMapSqlParameterSource();
        quotaParameterSource.addValue("vds_group_id", vdsGroupId)
                .addValue("engine_session_seq_id", engineSessionSeqId)
                .addValue("is_filtered", isFiltered);
        List<Quota> quotas =
                getCallsHandler().executeReadList("getAllThinQuotasByVDSGroupId",
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
        saveVdsGroupSpecificQuotas(quota);
    }

    /**
     * Return initialized entity with quota Vds group result set.
     */
    private RowMapper<QuotaVdsGroup> getVdsGroupQuotaResultSet() {
        RowMapper<QuotaVdsGroup> mapperQuotaLimitation = new RowMapper<QuotaVdsGroup>() {
            @Override
            public QuotaVdsGroup mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                QuotaVdsGroup entity = new QuotaVdsGroup();
                entity.setQuotaId(getGuidDefaultEmpty(rs, "quota_id"));
                entity.setQuotaVdsGroupId(getGuidDefaultEmpty(rs, "quota_vds_group_id"));
                entity.setVdsGroupId(getGuidDefaultEmpty(rs, "vds_group_id"));
                entity.setVdsGroupName(rs.getString("vds_group_name"));
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
                    QuotaVdsGroup vdsGroupEntity = new QuotaVdsGroup();
                    vdsGroupEntity.setMemSizeMB((Long) rs.getObject("mem_size_mb"));
                    vdsGroupEntity.setMemSizeMBUsage((Long) rs.getObject("mem_size_mb_usage"));
                    vdsGroupEntity.setVirtualCpu((Integer) rs.getObject("virtual_cpu"));
                    vdsGroupEntity.setVirtualCpuUsage((Integer) rs.getObject("virtual_cpu_usage"));
                    entity.setGlobalQuotaVdsGroup(vdsGroupEntity);
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
        entity.setThresholdVdsGroupPercentage((Integer) rs.getObject("threshold_vds_group_percentage"));
        entity.setThresholdStoragePercentage((Integer) rs.getObject("threshold_storage_percentage"));
        entity.setGraceVdsGroupPercentage((Integer) rs.getObject("grace_vds_group_percentage"));
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
                        .addValue("vds_group_id", null)
                        .addValue("storage_size_gb", quotaStorage.getStorageSizeGB())
                        .addValue("virtual_cpu", null)
                        .addValue("mem_size_mb", null);
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
                        .addValue("storage_id", null)
                        .addValue("storage_size_gb", null)
                        .addValue("virtual_cpu", quotaVdsGroup.getVirtualCpu())
                        .addValue("mem_size_mb", quotaVdsGroup.getMemSizeMB());
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
                        .addValue("storage_id", null)
                        .addValue("storage_size_gb",
                                quota.getGlobalQuotaStorage() != null ? quota.getGlobalQuotaStorage()
                                        .getStorageSizeGB() : null)
                        .addValue("virtual_cpu",
                                quota.getGlobalQuotaVdsGroup() != null ? quota.getGlobalQuotaVdsGroup().getVirtualCpu()
                                        : null)
                        .addValue("mem_size_mb",
                                quota.getGlobalQuotaVdsGroup() != null ? quota.getGlobalQuotaVdsGroup().getMemSizeMB()
                                        : null);
        return quotaParameterMap;
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

    @Override
    public List<Quota> getAllWithQuery(String query) {
        return jdbcTemplate.query(query, getQuotaMetaDataFromResultSet());
    }

    @Override
    public boolean isQuotaInUse(Quota quota){

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("quota_id", quota.getId());

        Map<String, Object> dbResults =
                new SimpleJdbcCall(jdbcTemplate).withFunctionName("IsQuotaInUse").execute(
                        parameterSource);

        String resultKey = dialect.getFunctionReturnKey();
        return dbResults.get(resultKey) != null && (Boolean) dbResults.get(resultKey);
    }

    @Override
    public List<Integer> getNonCountableQutoaVmStatuses() {
        return getCallsHandler().executeReadList("getNonCountableQutoaVmStatuses", getIntegerMapper(), null);
    }
}
