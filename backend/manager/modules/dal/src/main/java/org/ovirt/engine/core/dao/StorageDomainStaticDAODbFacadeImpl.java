package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class StorageDomainStaticDAODbFacadeImpl extends DefaultGenericDaoDbFacade<storage_domain_static, Guid> implements StorageDomainStaticDAO {

    public StorageDomainStaticDAODbFacadeImpl() {
        super("storage_domain_static");
        setProcedureNameForGet("Getstorage_domain_staticByid");
        setProcedureNameForGetAll("GetAllFromstorage_domain_static");
    }

    @Override
    public storage_domain_static getByName(String name) {
        return getCallsHandler().executeRead("Getstorage_domain_staticByName",
                StorageDomainStaticRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("name", name));
    }

    @Override
    public List<storage_domain_static> getAllOfStorageType(
            StorageType type) {
        return getCallsHandler().executeReadList("Getstorage_domain_staticBystorage_pool_type",
                StorageDomainStaticRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_type", type));
    }

    @Override
    public List<storage_domain_static> getAllForStoragePoolOfStorageType(
            StorageType type, Guid pool) {
        return getCallsHandler().executeReadList("Getstorage_domain_staticBystorage_type_and_storage_pool_id",
                StorageDomainStaticRowMapper.instance,
                getStoragePoolIdParameterSource(pool)
                        .addValue("storage_type", type));
    }

    @Override
    public List<storage_domain_static> getAllForStoragePool(Guid id) {
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

        ParameterizedRowMapper<Guid> mapper = new ParameterizedRowMapper<Guid>() {
            @Override
            public Guid mapRow(ResultSet rs, int rowNum) throws SQLException {
                return Guid.createGuidFromString(rs.getString("storage_id"));
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
    protected MapSqlParameterSource createFullParametersMapper(storage_domain_static domain) {
        return getCustomMapSqlParameterSource()
                .addValue("id", domain.getId())
                .addValue("storage", domain.getstorage())
                .addValue("storage_name", domain.getstorage_name())
                .addValue("storage_type", domain.getstorage_type())
                .addValue("storage_domain_type",
                        domain.getstorage_domain_type())
                .addValue("storage_domain_format_type", domain.getStorageFormat());
    }

    @Override
    protected ParameterizedRowMapper<storage_domain_static> createEntityRowMapper() {
        return StorageDomainStaticRowMapper.instance;
    }

    private static final class StorageDomainStaticRowMapper implements ParameterizedRowMapper<storage_domain_static> {
        public static final StorageDomainStaticRowMapper instance = new StorageDomainStaticRowMapper();

        @Override
        public storage_domain_static mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            storage_domain_static entity = new storage_domain_static();
            entity.setId(Guid.createGuidFromString(rs.getString("id")));
            entity.setstorage(rs.getString("storage"));
            entity.setstorage_name(rs.getString("storage_name"));
            entity.setstorage_type(StorageType.forValue(rs
                    .getInt("storage_type")));
            entity.setstorage_domain_type(StorageDomainType.forValue(rs
                    .getInt("storage_domain_type")));
            entity.setStorageFormat(StorageFormatType.forValue(rs
                    .getString("storage_domain_format_type")));
            return entity;
        }
    }

}
