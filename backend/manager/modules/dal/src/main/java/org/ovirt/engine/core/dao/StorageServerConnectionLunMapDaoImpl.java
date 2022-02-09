package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMapId;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code StorageServerConnectionLunMapDaoImpl} provides an implementation of {@link StorageServerConnectionLunMapDao}.
 */
@Named
@Singleton
public class StorageServerConnectionLunMapDaoImpl extends BaseDao implements
        StorageServerConnectionLunMapDao {

    private static final RowMapper<LUNStorageServerConnectionMap> storageServerConnectionLunMapRowMapper = (rs, rowNum) -> {
        LUNStorageServerConnectionMap entity = new LUNStorageServerConnectionMap();
        entity.setLunId(rs.getString("lun_id"));
        entity.setStorageServerConnection(rs.getString("storage_server_connection"));
        return entity;
    };

    @Override
    public LUNStorageServerConnectionMap get(LUNStorageServerConnectionMapId id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("lun_id", id.lunId).addValue(
                "storage_server_connection", id.storageServerConnection);

        return getCallsHandler().executeRead("GetLUN_storage_server_connection_mapByLUNBystorage_server_conn",
                storageServerConnectionLunMapRowMapper,
                parameterSource);
    }

    @Override
    public void save(LUNStorageServerConnectionMap map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("lun_id", map.getLunId())
                .addValue("storage_server_connection", map.getStorageServerConnection());

        getCallsHandler().executeModification("InsertLUN_storage_server_connection_map", parameterSource);
    }

    @Override
    public List<LUNStorageServerConnectionMap> getAll(final String lunId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("lun_id", lunId);

        return getCallsHandler().executeReadList("GetLUN_storage_server_connection_mapByLUN",
                storageServerConnectionLunMapRowMapper,
                parameterSource);
    }

    @Override
    public List<LUNStorageServerConnectionMap> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetAllFromLUN_storage_server_connection_map",
                storageServerConnectionLunMapRowMapper,
                parameterSource);
    }

    @Override
    public void update(LUNStorageServerConnectionMap entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(LUNStorageServerConnectionMapId id) {
        throw new UnsupportedOperationException();
    }

    public void removeServerConnectionByIdAndLunId(String lunId, String storageServerConnection) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("lun_id", lunId)
                .addValue("storage_server_connection", storageServerConnection);
        getCallsHandler().executeModification("DeleteLUN_storage_server_connection_map", parameterSource);
    }
}
