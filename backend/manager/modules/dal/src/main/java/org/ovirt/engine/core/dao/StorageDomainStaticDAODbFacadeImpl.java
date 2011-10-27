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

public class StorageDomainStaticDAODbFacadeImpl extends BaseDAODbFacade implements StorageDomainStaticDAO {

    @Override
    public storage_domain_static get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        ParameterizedRowMapper<storage_domain_static> mapper = new ParameterizedRowMapper<storage_domain_static>() {
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
        };

        return getCallsHandler().executeRead("Getstorage_domain_staticByid", mapper, parameterSource);
    }

    @Override
    public storage_domain_static getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("name", name);

        ParameterizedRowMapper<storage_domain_static> mapper = new ParameterizedRowMapper<storage_domain_static>() {
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
        };

        return getCallsHandler().executeRead("Getstorage_domain_staticByName", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<storage_domain_static> getAllOfStorageType(
            StorageType type) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_type", type);

        ParameterizedRowMapper<storage_domain_static> mapper = new ParameterizedRowMapper<storage_domain_static>() {
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
        };

        return getCallsHandler().executeReadList("Getstorage_domain_staticBystorage_pool_type", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<storage_domain_static> getAllForStoragePoolOfStorageType(
            StorageType type, Guid pool) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_type", type).addValue("storage_pool_id", pool);

        ParameterizedRowMapper<storage_domain_static> mapper = new ParameterizedRowMapper<storage_domain_static>() {
            @Override
            public storage_domain_static mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                storage_domain_static entity = new storage_domain_static();
                entity.setId(Guid.createGuidFromString(rs.getString("id")));
                entity.setstorage(rs.getString("storage"));
                entity.setstorage_name(rs.getString("storage_name"));
                // entity.setstorage_pool_id(rs.getString("storage_pool_id"));
                entity.setstorage_type(StorageType.forValue(rs
                        .getInt("storage_type")));
                entity.setstorage_pool_name(rs.getString("storage_pool_name"));
                entity.setstorage_domain_type(StorageDomainType.forValue(rs
                        .getInt("storage_domain_type")));
                entity.setStorageFormat(StorageFormatType.forValue(rs
                        .getString("storage_domain_format_type")));
                // entity.setstorage_domain_shared_status(rs.getInt("storage_domain_shared_status"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("Getstorage_domain_staticBystorage_type_and_storage_pool_id",
                mapper,
                parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<storage_domain_static> getAllForStoragePool(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", id);

        ParameterizedRowMapper<storage_domain_static> mapper = new ParameterizedRowMapper<storage_domain_static>() {
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
        };

        return getCallsHandler().executeReadList("Getstorage_domain_staticBystorage_pool_id", mapper, parameterSource);
    }

    @Override
    public void save(storage_domain_static domain) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", domain.getId())
                .addValue("storage", domain.getstorage())
                .addValue("storage_name", domain.getstorage_name())
                .addValue("storage_type", domain.getstorage_type())
                .addValue("storage_domain_type",
                        domain.getstorage_domain_type())
                .addValue("storage_domain_format_type", domain.getStorageFormat());

        getCallsHandler().executeModification("Insertstorage_domain_static", parameterSource);
    }

    @Override
    public void update(storage_domain_static domain) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", domain.getId())
                .addValue("storage", domain.getstorage())
                .addValue("storage_name", domain.getstorage_name())
                .addValue("storage_type", domain.getstorage_type())
                .addValue("storage_domain_type",
                        domain.getstorage_domain_type())
                .addValue("storage_domain_format_type", domain.getStorageFormat());

        getCallsHandler().executeModification("Updatestorage_domain_static", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        getCallsHandler().executeModification("Deletestorage_domain_static", parameterSource);
    }

    @Override
    public List<storage_domain_static> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<storage_domain_static> mapper = new ParameterizedRowMapper<storage_domain_static>() {
            @Override
            public storage_domain_static mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                storage_domain_static entity = new storage_domain_static();
                entity.setId(Guid.createGuidFromString(rs.getString("id")));
                entity.setstorage(rs.getString("storage"));
                entity.setstorage_name(rs.getString("storage_name"));
                entity.setstorage_type(StorageType.forValue(rs.getInt("storage_type")));
                entity.setstorage_domain_type(StorageDomainType.forValue(rs.getInt("storage_domain_type")));
                entity.setStorageFormat(StorageFormatType.forValue(rs.getString("storage_domain_format_type")));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAllFromstorage_domain_static", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Guid> getAllIds(Guid pool, StorageDomainStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", pool)
                .addValue("status", status.getValue());

        ParameterizedRowMapper<Guid> mapper = new ParameterizedRowMapper<Guid>() {
            @Override
            public Guid mapRow(ResultSet rs, int rowNum) throws SQLException {
                return Guid.createGuidFromString(rs.getString("storage_id"));
            }
        };

        return getCallsHandler().executeReadList("GetStorageDomainIdsByStoragePoolIdAndStatus", mapper, parameterSource);
    }
}
