package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class StoragePoolIsoMapDaoImpl extends BaseDao implements StoragePoolIsoMapDao {

    private static final class StoragePoolIsoMapRowMapper implements RowMapper<StoragePoolIsoMap> {
        public static final StoragePoolIsoMapRowMapper instance = new StoragePoolIsoMapRowMapper();

        @Override
        public StoragePoolIsoMap mapRow(ResultSet rs, int rowNum) throws SQLException {
            StoragePoolIsoMap entity = new StoragePoolIsoMap();
            entity.setStorageId(getGuidDefaultEmpty(rs, "storage_id"));
            entity.setStoragePoolId(getGuid(rs, "storage_pool_id"));
            entity.setStatus(StorageDomainStatus.forValue(rs.getInt("status")));
            return entity;
        }
    }

    @Override
    public StoragePoolIsoMap get(StoragePoolIsoMapId id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_id", id.getStorageId())
                .addValue("storage_pool_id", id.getStoragePoolId());

        return getCallsHandler().executeRead("Getstorage_pool_iso_mapBystorage_idAndBystorage_pool_id",
                StoragePoolIsoMapRowMapper.instance,
                parameterSource);
    }

    @Override
    public void save(StoragePoolIsoMap map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_id",
                map.getStorageId()).addValue("storage_pool_id", map.getStoragePoolId()).addValue("status",
                map.getStatus());
        getCallsHandler().executeModification("Insertstorage_pool_iso_map", parameterSource);
    }

    @Override
    public void update(StoragePoolIsoMap map) {
        updateStatus(map.getId(), map.getStatus());
    }

    @Override
    public void remove(StoragePoolIsoMapId id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_id",
                id.getStorageId()).addValue("storage_pool_id", id.getStoragePoolId());

        getCallsHandler().executeModification("Deletestorage_pool_iso_map", parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<StoragePoolIsoMap> getAllForStoragePool(Guid storagePoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_id", Guid.Empty)
                .addValue("storage_pool_id", storagePoolId);
        return getCallsHandler().executeReadList("Getstorage_pool_iso_mapsByBystorage_pool_id",
                StoragePoolIsoMapRowMapper.instance,
                parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<StoragePoolIsoMap> getAllForStorage(Guid isoId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_id", isoId);
        return getCallsHandler().executeReadList("Getstorage_pool_iso_mapsBystorage_id",
                StoragePoolIsoMapRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<StoragePoolIsoMap> getAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateStatus(StoragePoolIsoMapId id, StorageDomainStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_id",
                id.getStorageId()).addValue("storage_pool_id", id.getStoragePoolId()).addValue("status", status);
        getCallsHandler().executeModification("Updatestorage_pool_iso_map_status", parameterSource);
    }



}
