package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code StoragePoolDaoImpl} provides a concrete implementation of {@link StoragePoolDao}.
 */
@Named
@Singleton
@SuppressWarnings("synthetic-access")
public class StoragePoolDaoImpl extends BaseDao implements StoragePoolDao {
    private static final RowMapper<StoragePool> mapper = (rs, rowNum) -> {
        StoragePool entity = new StoragePool();
        entity.setdescription(rs.getString("description"));
        entity.setComment(rs.getString("free_text_comment"));
        entity.setId(getGuidDefaultEmpty(rs, "id"));
        entity.setName(rs.getString("name"));
        entity.setIsLocal(rs.getBoolean("is_local"));
        entity.setStatus(StoragePoolStatus.forValue(rs.getInt("status")));
        entity.setMasterDomainVersion(rs.getInt("master_domain_version"));
        entity.setSpmVdsId(getGuid(rs, "spm_vds_id"));
        entity.setCompatibilityVersion(new VersionRowMapper("compatibility_version").mapRow(rs, rowNum));
        entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
        entity.setStoragePoolFormatType(StorageFormatType.forValue(rs.getString("storage_pool_format_type")));
        entity.setManaged(rs.getBoolean("managed"));
        return entity;
    };

    @Override
    public StoragePool get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public StoragePool get(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);
        return getCallsHandler().executeRead("Getstorage_poolByid", mapper, parameterSource);
    }

    @Override
    public List<StoragePool> getByName(String name, boolean isCaseSensitive) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("name", name)
                .addValue("is_case_sensitive", isCaseSensitive);
        return getCallsHandler().executeReadList("Getstorage_poolByName", mapper, parameterSource);
    }

    @Override
    public StoragePool getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("name", name)
                .addValue("is_case_sensitive", true);
        return getCallsHandler().executeRead("Getstorage_poolByName", mapper, parameterSource);
    }

    @Override
    public StoragePool getForVds(Guid vds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vdsId", vds);
        return getCallsHandler().executeRead("Getstorage_poolsByVdsId", mapper, parameterSource);
    }

    @Override
    public StoragePool getForCluster(Guid cluster) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("clusterId", cluster);
        return getCallsHandler().executeRead("Getstorage_poolsByClusterId", mapper,
                parameterSource);
    }

    @Override
    public List<StoragePool> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<StoragePool> getAllByStatus(StoragePoolStatus status) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("status", status.getValue());

        return getCallsHandler().executeReadList("GetAllByStatus", mapper, parameterSource);
    }

    @Override
    public List<StoragePool> getAll(Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetAllFromstorage_pool", mapper, parameterSource);
    }

    @Override
    public List<StoragePool> getAllForStorageDomain(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_domain_id", id);
        return getCallsHandler().executeReadList("Getstorage_poolsByStorageDomainId", mapper,
                parameterSource);
    }

    @Override
    public List<StoragePool> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, mapper);
    }

    @Override
    public void save(StoragePool pool) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", pool.getdescription())
                .addValue("free_text_comment", pool.getComment())
                .addValue("id", pool.getId())
                .addValue("name", pool.getName())
                .addValue("is_local", pool.isLocal())
                .addValue("status", pool.getStatus())
                .addValue("master_domain_version",
                        pool.getMasterDomainVersion())
                .addValue("spm_vds_id", pool.getSpmVdsId())
                .addValue("quota_enforcement_type", pool.getQuotaEnforcementType())
                .addValue("compatibility_version",
                        pool.getCompatibilityVersion())
                .addValue("managed", pool.isManaged());

        getCallsHandler().executeModification("Insertstorage_pool",
                parameterSource);
    }

    @Override
    public void update(StoragePool pool) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", pool.getdescription())
                .addValue("free_text_comment", pool.getComment())
                .addValue("id", pool.getId())
                .addValue("name", pool.getName())
                .addValue("status", pool.getStatus())
                .addValue("is_local", pool.isLocal())
                .addValue("storage_pool_format_type", pool.getStoragePoolFormatType())
                .addValue("master_domain_version",
                        pool.getMasterDomainVersion())
                .addValue("spm_vds_id", pool.getSpmVdsId())
                .addValue("compatibility_version",
                        pool.getCompatibilityVersion())
                .addValue("quota_enforcement_type",
                        pool.getQuotaEnforcementType().getValue())
                .addValue("managed", pool.isManaged());

        getCallsHandler().executeModification("Updatestorage_pool", parameterSource);
    }

    @Override
    public void updatePartial(StoragePool pool) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", pool.getdescription())
                .addValue("free_text_comment", pool.getComment())
                .addValue("id", pool.getId())
                .addValue("name", pool.getName())
                .addValue("is_local", pool.isLocal())
                .addValue("storage_pool_format_type", pool.getStoragePoolFormatType())
                .addValue("compatibility_version",
                        pool.getCompatibilityVersion())
                .addValue("quota_enforcement_type",
                        pool.getQuotaEnforcementType().getValue());

        getCallsHandler().executeModification("Updatestorage_pool_partial", parameterSource);
    }

    @Override
    public void updateStatus(Guid id, StoragePoolStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id)
                .addValue("status", status);
        getCallsHandler().executeModification("Updatestorage_pool_status", parameterSource);

    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        getCallsHandler().executeModification("Deletestorage_pool", parameterSource);
    }

    @Override
    public List<StoragePool> getDataCentersWithPermittedActionOnClusters(Guid userId, ActionGroup actionGroup,
            boolean supportsVirtService, boolean supportsGlusterService) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", userId)
                .addValue("action_group_id", actionGroup.getId())
                .addValue("supports_virt_service", supportsVirtService)
                .addValue("supports_gluster_service", supportsGlusterService);

        return getCallsHandler().executeReadList(
                "fn_perms_get_storage_pools_with_permitted_action_on_clusters",
                mapper, parameterSource);
    }

    @Override
    public int increaseStoragePoolMasterVersion(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);
        return getCallsHandler().executeModificationReturnResult("IncreaseStoragePoolMasterVersion", parameterSource);
    }

    @Override
    public List<StoragePool> getDataCentersByClusterService(boolean supportsVirtService, boolean supportsGlusterService) {
        final MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();
        parameterSource
            .addValue("supports_virt_service", supportsVirtService)
            .addValue("supports_gluster_service", supportsGlusterService);
        return getCallsHandler().executeReadList("GetStoragePoolsByClusterService", mapper, parameterSource);
    }

    @Override
    public List<Guid> getDcIdByExternalNetworkId(String externalId) {
        return getCallsHandler().executeReadList("GetDcIdByExternalNetworkId",
                createGuidMapper(),
                getCustomMapSqlParameterSource().addValue("external_id", externalId));
    }
}
