package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.ovirt.engine.core.common.businessentities.StorageDomainOwnerType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;

public class StoragePoolIsoMapDAODbFacadeImpl extends BaseDAODbFacade implements StoragePoolIsoMapDAO {

    @Override
    public storage_pool_iso_map get(StoragePoolIsoMapId id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_id", id.getStorageId())
                .addValue("storage_pool_id", id.getStoragePoolId());

        ParameterizedRowMapper<storage_pool_iso_map> mapper = new ParameterizedRowMapper<storage_pool_iso_map>() {
            @Override
            public storage_pool_iso_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                storage_pool_iso_map entity = new storage_pool_iso_map();
                entity.setstorage_id(Guid.createGuidFromString(rs.getString("storage_id")));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs.getString("storage_pool_id")));
                entity.setstatus(StorageDomainStatus.forValue(rs.getInt("status")));
                entity.setowner(StorageDomainOwnerType.forValue(rs.getInt("owner")));
                return entity;
            }
        };

        return getCallsHandler().executeRead("Getstorage_pool_iso_mapBystorage_idAndBystorage_pool_id",
                mapper,
                parameterSource);
    }

    @Override
    public void save(storage_pool_iso_map map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_id",
                map.getstorage_id()).addValue("storage_pool_id", map.getstorage_pool_id()).addValue("status",
                map.getstatus()).addValue("owner", map.getowner());
        getCallsHandler().executeModification("Insertstorage_pool_iso_map", parameterSource);
    }

    @Override
    public void update(storage_pool_iso_map map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_id",
                map.getstorage_id()).addValue("storage_pool_id", map.getstorage_pool_id()).addValue("status",
                map.getstatus()).addValue("owner", map.getowner());

        getCallsHandler().executeModification("Updatestorage_pool_iso_map", parameterSource);
    }

    @Override
    public void remove(StoragePoolIsoMapId id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_id",
                id.getStorageId()).addValue("storage_pool_id", id.getStoragePoolId());

        getCallsHandler().executeModification("Deletestorage_pool_iso_map", parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<storage_pool_iso_map> getAllForStoragePool(Guid storagePoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_id", Guid.Empty)
                .addValue("storage_pool_id", storagePoolId);

        ParameterizedRowMapper<storage_pool_iso_map> mapper = new ParameterizedRowMapper<storage_pool_iso_map>() {
            @Override
            public storage_pool_iso_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                storage_pool_iso_map entity = new storage_pool_iso_map();
                entity.setstorage_id(Guid.createGuidFromString(rs.getString("storage_id")));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs.getString("storage_pool_id")));
                entity.setstatus(StorageDomainStatus.forValue(rs.getInt("status")));
                entity.setowner(StorageDomainOwnerType.forValue(rs.getInt("owner")));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("Getstorage_pool_iso_mapsByBystorage_pool_id", mapper,
                parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<storage_pool_iso_map> getAllForStorage(Guid isoId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_id", isoId);

        ParameterizedRowMapper<storage_pool_iso_map> mapper = new ParameterizedRowMapper<storage_pool_iso_map>() {
            @Override
            public storage_pool_iso_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                storage_pool_iso_map entity = new storage_pool_iso_map();
                entity.setstorage_id(Guid.createGuidFromString(rs.getString("storage_id")));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs.getString("storage_pool_id")));
                entity.setstatus(StorageDomainStatus.forValue(rs.getInt("status")));
                entity.setowner(StorageDomainOwnerType.forValue(rs.getInt("owner")));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("Getstorage_pool_iso_mapsBystorage_id", mapper,
                parameterSource);
    }

    @Override
    public List<storage_pool_iso_map> getAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateStatus(StoragePoolIsoMapId id, StorageDomainStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_id",
                id.getStorageId()).addValue("storage_pool_id", id.getStoragePoolId()).addValue("status", status);
        getCallsHandler().executeModification("Updatestorage_pool_iso_map_status", parameterSource);
    }



}
