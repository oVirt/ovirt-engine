package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.Version;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <code>StoragePoolDAODbFacadeImpl</code> provides a concrete implementation of {@link StoragePoolDAO} based on code
 * from {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 */
@SuppressWarnings("synthetic-access")
public class StoragePoolDAODbFacadeImpl extends BaseDAODbFacade implements StoragePoolDAO {

    private static final class StoragePoolRawMapper implements ParameterizedRowMapper<storage_pool> {
        @Override
        public storage_pool mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            storage_pool entity = new storage_pool();
            entity.setdescription(rs.getString("description"));
            entity.setId(Guid.createGuidFromString(rs.getString("id")));
            entity.setname(rs.getString("name"));
            entity.setstorage_pool_type(StorageType.forValue(rs
                    .getInt("storage_pool_type")));
            entity.setstatus(StoragePoolStatus.forValue(rs.getInt("status")));
            entity.setmaster_domain_version(rs
                    .getInt("master_domain_version"));
            entity.setspm_vds_id(NGuid.createGuidFromString(rs
                    .getString("spm_vds_id")));
            entity.setcompatibility_version(new Version(rs
                    .getString("compatibility_version")));
            entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
            entity.setStoragePoolFormatType(getStorageFormatTypeForPool(rs));
            return entity;
        }
    }

    private static final ParameterizedRowMapper<storage_pool> mapper = new StoragePoolRawMapper();

    private static StorageFormatType getStorageFormatTypeForPool(ResultSet rs) throws SQLException {
        return StorageFormatType.forValue(rs.getString("storage_pool_format_type"));
    }

    @Override
    public storage_pool get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public storage_pool get(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        ParameterizedRowMapper<storage_pool> mapper = new StoragePoolRawMapper();

        return getCallsHandler().executeRead("Getstorage_poolByid", mapper, parameterSource);
    }

    @Override
    public storage_pool getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("name", name);

        ParameterizedRowMapper<storage_pool> mapper = new ParameterizedRowMapper<storage_pool>() {
            @Override
            public storage_pool mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                storage_pool entity = new storage_pool();
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs.getString("id")));
                entity.setname(rs.getString("name"));
                entity.setstorage_pool_type(StorageType.forValue(rs
                        .getInt("storage_pool_type")));
                entity.setstatus(StoragePoolStatus.forValue(rs.getInt("status")));
                entity.setmaster_domain_version(rs
                        .getInt("master_domain_version"));
                entity.setspm_vds_id(NGuid.createGuidFromString(rs
                        .getString("spm_vds_id")));
                entity.setcompatibility_version(new Version(rs
                        .getString("compatibility_version")));
                entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
                entity.setStoragePoolFormatType(getStorageFormatTypeForPool(rs));
                return entity;
            }
        };

        return getCallsHandler().executeRead("Getstorage_poolByName", mapper, parameterSource);
    }

    @Override
    public storage_pool getForVds(Guid vds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vdsId", vds);

        ParameterizedRowMapper<storage_pool> mapper = new ParameterizedRowMapper<storage_pool>() {
            @Override
            public storage_pool mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                storage_pool entity = new storage_pool();
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs.getString("id")));
                entity.setname(rs.getString("name"));
                entity.setstorage_pool_type(StorageType.forValue(rs
                        .getInt("storage_pool_type")));
                entity.setstatus(StoragePoolStatus.forValue(rs.getInt("status")));
                entity.setmaster_domain_version(rs
                        .getInt("master_domain_version"));
                entity.setspm_vds_id(NGuid.createGuidFromString(rs
                        .getString("spm_vds_id")));
                entity.setcompatibility_version(new Version(rs
                        .getString("compatibility_version")));
                entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
                entity.setStoragePoolFormatType(getStorageFormatTypeForPool(rs));
                return entity;
            }
        };

        return getCallsHandler().executeRead("Getstorage_poolsByVdsId", mapper, parameterSource);
    }

    @Override
    public storage_pool getForVdsGroup(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("clusterId", id);

        ParameterizedRowMapper<storage_pool> mapper = new ParameterizedRowMapper<storage_pool>() {
            @Override
            public storage_pool mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                storage_pool entity = new storage_pool();
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs.getString("id")));
                entity.setname(rs.getString("name"));
                entity.setstorage_pool_type(StorageType.forValue(rs
                        .getInt("storage_pool_type")));
                entity.setstatus(StoragePoolStatus.forValue(rs.getInt("status")));
                entity.setmaster_domain_version(rs
                        .getInt("master_domain_version"));
                entity.setspm_vds_id(NGuid.createGuidFromString(rs
                        .getString("spm_vds_id")));
                entity.setcompatibility_version(new Version(rs
                        .getString("compatibility_version")));
                entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
                entity.setStoragePoolFormatType(getStorageFormatTypeForPool(rs));
                return entity;
            }
        };

        return getCallsHandler().executeRead("Getstorage_poolsByVdsGroupId", mapper,
                parameterSource);
    }

    @Override
    public List<storage_pool> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<storage_pool> getAllByStatus(StoragePoolStatus status) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("status", status.getValue());

        return getCallsHandler().executeReadList("GetAllByStatus", mapper, parameterSource);
    }

    @Override
    public List<storage_pool> getAll(Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered);

        ParameterizedRowMapper<storage_pool> mapper = new ParameterizedRowMapper<storage_pool>() {
            @Override
            public storage_pool mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                storage_pool entity = new storage_pool();
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs.getString("id")));
                entity.setname(rs.getString("name"));
                entity.setstorage_pool_type(StorageType.forValue(rs
                        .getInt("storage_pool_type")));
                entity.setstatus(StoragePoolStatus.forValue(rs.getInt("status")));
                entity.setmaster_domain_version(rs
                        .getInt("master_domain_version"));
                entity.setspm_vds_id(NGuid.createGuidFromString(rs
                        .getString("spm_vds_id")));
                entity.setcompatibility_version(new Version(rs
                        .getString("compatibility_version")));
                entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
                entity.setStoragePoolFormatType(getStorageFormatTypeForPool(rs));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAllFromstorage_pool", mapper, parameterSource);
    }

    @Override
    public List<storage_pool> getAllOfType(StorageType type) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_type", type);

        ParameterizedRowMapper<storage_pool> mapper = new ParameterizedRowMapper<storage_pool>() {
            @Override
            public storage_pool mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                storage_pool entity = new storage_pool();
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs.getString("id")));
                entity.setname(rs.getString("name"));
                entity.setstorage_pool_type(StorageType.forValue(rs
                        .getInt("storage_pool_type")));
                entity.setstatus(StoragePoolStatus.forValue(rs.getInt("status")));
                entity.setmaster_domain_version(rs
                        .getInt("master_domain_version"));
                entity.setspm_vds_id(NGuid.createGuidFromString(rs
                        .getString("spm_vds_id")));
                entity.setcompatibility_version(new Version(rs
                        .getString("compatibility_version")));
                entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
                entity.setStoragePoolFormatType(getStorageFormatTypeForPool(rs));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("Getstorage_poolsByType", mapper, parameterSource);
    }

    @Override
    public List<storage_pool> getAllForStorageDomain(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_domain_id", id);

        ParameterizedRowMapper<storage_pool> mapper = new ParameterizedRowMapper<storage_pool>() {
            @Override
            public storage_pool mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                storage_pool entity = new storage_pool();
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs.getString("id")));
                entity.setname(rs.getString("name"));
                entity.setstorage_pool_type(StorageType.forValue(rs
                        .getInt("storage_pool_type")));
                entity.setStoragePoolFormatType(getStorageFormatTypeForPool(rs));
                entity.setstatus(StoragePoolStatus.forValue(rs.getInt("status")));
                entity.setmaster_domain_version(rs
                        .getInt("master_domain_version"));
                entity.setspm_vds_id(NGuid.createGuidFromString(rs
                        .getString("spm_vds_id")));
                entity.setcompatibility_version(new Version(rs
                        .getString("compatibility_version")));
                entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("Getstorage_poolsByStorageDomainId", mapper,
                parameterSource);
    }

    @Override
    public List<storage_pool> getAllWithQuery(String query) {
        ParameterizedRowMapper<storage_pool> mapper = new ParameterizedRowMapper<storage_pool>() {
            @Override
            public storage_pool mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                storage_pool entity = new storage_pool();
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs.getString("id")));
                entity.setname(rs.getString("name"));
                entity.setstorage_pool_type(StorageType.forValue(rs
                        .getInt("storage_pool_type")));
                entity.setStoragePoolFormatType(getStorageFormatTypeForPool(rs));
                entity.setstatus(StoragePoolStatus.forValue(rs.getInt("status")));
                entity.setmaster_domain_version(rs
                        .getInt("master_domain_version"));
                entity.setspm_vds_id(NGuid.createGuidFromString(rs
                        .getString("spm_vds_id")));
                entity.setcompatibility_version(new Version(rs
                        .getString("compatibility_version")));
                entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
                return entity;
            }
        };

        return new SimpleJdbcTemplate(jdbcTemplate).query(query, mapper);
    }

    @Override
    public void save(storage_pool pool) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", pool.getdescription())
                .addValue("id", pool.getId())
                .addValue("name", pool.getname())
                .addValue("storage_pool_type", pool.getstorage_pool_type())
                .addValue("status", pool.getstatus())
                .addValue("master_domain_version",
                        pool.getmaster_domain_version())
                .addValue("spm_vds_id", pool.getspm_vds_id())
                .addValue("quota_enforcement_type", pool.getQuotaEnforcementType())
                .addValue("compatibility_version",
                        pool.getcompatibility_version());

        getCallsHandler().executeModification("Insertstorage_pool",
                parameterSource);
    }

    @Override
    public void update(storage_pool pool) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", pool.getdescription())
                .addValue("id", pool.getId())
                .addValue("name", pool.getname())
                .addValue("storage_pool_type", pool.getstorage_pool_type())
                .addValue("status", pool.getstatus())
                .addValue("storage_pool_format_type", pool.getStoragePoolFormatType())
                .addValue("master_domain_version",
                        pool.getmaster_domain_version())
                .addValue("spm_vds_id", pool.getspm_vds_id())
                .addValue("compatibility_version",
                        pool.getcompatibility_version())
                .addValue("quota_enforcement_type",
                        pool.getQuotaEnforcementType().getValue());

        getCallsHandler().executeModification("Updatestorage_pool", parameterSource);
    }

    @Override
    public void updatePartial(storage_pool pool) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("description", pool.getdescription())
                .addValue("id", pool.getId())
                .addValue("name", pool.getname())
                .addValue("storage_pool_type", pool.getstorage_pool_type())
                .addValue("storage_pool_format_type", pool.getStoragePoolFormatType())
                .addValue("compatibility_version",
                        pool.getcompatibility_version())
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
    public List<storage_pool> getDataCentersWithPermittedActionOnClusters(Guid userId, ActionGroup actionGroup) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", userId).addValue("action_group_id", actionGroup.getId());

        StoragePoolRawMapper mapper = new StoragePoolRawMapper();

        return getCallsHandler().executeReadList(
                "fn_perms_get_storage_pools_with_permitted_action_on_vds_groups",
                mapper, parameterSource);
    }

    @Override
    public int increaseStoragePoolMasterVersion(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);
        return getCallsHandler().executeModificationReturnResult("IncreaseStoragePoolMasterVersion", parameterSource);
    }

}
