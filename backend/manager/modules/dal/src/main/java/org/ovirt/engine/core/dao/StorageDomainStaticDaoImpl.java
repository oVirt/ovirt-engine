package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class StorageDomainStaticDaoImpl extends DefaultGenericDao<StorageDomainStatic, Guid> implements StorageDomainStaticDao {

    public StorageDomainStaticDaoImpl() {
        super("storage_domain_static");
        setProcedureNameForGet("Getstorage_domain_staticByid");
        setProcedureNameForGetAll("GetAllFromstorage_domain_static");
    }

    @Override
    public StorageDomainStatic getByName(String name) {
        return getCallsHandler().executeRead("Getstorage_domain_staticByName",
                StorageDomainStaticRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("name", name));
    }

    @Override
    public StorageDomainStatic getByName(String name, Guid userId, boolean filtered) {
        return getCallsHandler().executeRead("Getstorage_domain_staticByNameFiltered",
                StorageDomainStaticRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("name", name)
                        .addValue("user_id", userId)
                        .addValue("is_filtered", filtered));
    }

    @Override
    public List<StorageDomainStatic> getAllForStoragePool(Guid id) {
        return getCallsHandler().executeReadList("Getstorage_domain_staticBystorage_pool_id",
                StorageDomainStaticRowMapper.instance,
                getStoragePoolIdParameterSource(id));
    }

    private MapSqlParameterSource getStoragePoolIdParameterSource(Guid id) {
        return getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", id);
    }

    @Override
    public List<Guid> getAllIds(Guid pool, StorageDomainStatus status) {
        MapSqlParameterSource parameterSource = getStoragePoolIdParameterSource(pool)
                .addValue("status", status.getValue());

        RowMapper<Guid> mapper = new RowMapper<Guid>() {
            @Override
            public Guid mapRow(ResultSet rs, int rowNum) throws SQLException {
                return getGuidDefaultEmpty(rs, "storage_id");
            }
        };

        return getCallsHandler().executeReadList("GetStorageDomainIdsByStoragePoolIdAndStatus", mapper, parameterSource);
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource()
                .addValue("id", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(StorageDomainStatic domain) {
        return getCustomMapSqlParameterSource()
                .addValue("id", domain.getId())
                .addValue("storage", domain.getStorage())
                .addValue("storage_name", domain.getStorageName())
                .addValue("storage_description", domain.getDescription())
                .addValue("storage_comment", domain.getComment())
                .addValue("storage_type", domain.getStorageType())
                .addValue("storage_domain_type",
                        domain.getStorageDomainType())
                .addValue("storage_domain_format_type", domain.getStorageFormat())
                .addValue("last_time_used_as_master", domain.getLastTimeUsedAsMaster())
                .addValue("wipe_after_delete", domain.getWipeAfterDelete())
                .addValue("warning_low_space_indicator", domain.getWarningLowSpaceIndicator())
                .addValue("critical_space_action_blocker", domain.getCriticalSpaceActionBlocker());
    }

    @Override
    protected RowMapper<StorageDomainStatic> createEntityRowMapper() {
        return StorageDomainStaticRowMapper.instance;
    }

    private static final class StorageDomainStaticRowMapper implements RowMapper<StorageDomainStatic> {
        public static final StorageDomainStaticRowMapper instance = new StorageDomainStaticRowMapper();

        @Override
        public StorageDomainStatic mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            StorageDomainStatic entity = new StorageDomainStatic();
            entity.setId(getGuidDefaultEmpty(rs, "id"));
            entity.setStorage(rs.getString("storage"));
            entity.setStorageName(rs.getString("storage_name"));
            entity.setDescription(rs.getString("storage_description"));
            entity.setComment(rs.getString("storage_comment"));
            entity.setStorageType(StorageType.forValue(rs
                    .getInt("storage_type")));
            entity.setStorageDomainType(StorageDomainType.forValue(rs
                    .getInt("storage_domain_type")));
            entity.setStorageFormat(StorageFormatType.forValue(rs
                    .getString("storage_domain_format_type")));
            entity.setLastTimeUsedAsMaster(rs.getLong("last_time_used_as_master"));
            entity.setWipeAfterDelete(rs.getBoolean("wipe_after_delete"));
            entity.setWarningLowSpaceIndicator((Integer) rs.getObject("warning_low_space_indicator"));
            entity.setCriticalSpaceActionBlocker((Integer) rs.getObject("critical_space_action_blocker"));
            return entity;
        }
    }

}
