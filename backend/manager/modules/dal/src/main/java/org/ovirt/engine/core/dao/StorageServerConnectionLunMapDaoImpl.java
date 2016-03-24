package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap;
import org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMapId;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>StorageServerConnectionLunMapDaoImpl</code> provides an implementation of {@link org.ovirt.engine.core.common.businessentities.storage.LUNStorageServerConnectionMap}
 * based on code from {@link DbFacade}.
 *
 *
 */
@Named
@Singleton
public class StorageServerConnectionLunMapDaoImpl extends BaseDao implements
        StorageServerConnectionLunMapDao {

    private static final class StorageServerConnectionLunMapRowMapper
            implements RowMapper<LUNStorageServerConnectionMap> {
        public static final StorageServerConnectionLunMapRowMapper instance =
                new StorageServerConnectionLunMapRowMapper();

        @Override
        public LUNStorageServerConnectionMap mapRow(ResultSet rs, int rowNum) throws SQLException {
            LUNStorageServerConnectionMap entity = new LUNStorageServerConnectionMap();
            entity.setLunId(rs.getString("lun_id"));
            entity.setStorageServerConnection(rs.getString("storage_server_connection"));
            return entity;
        }
    }

    @Override
    public LUNStorageServerConnectionMap get(LUNStorageServerConnectionMapId id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("lun_id", id.lunId).addValue(
                "storage_server_connection", id.storageServerConnection);

        return getCallsHandler().executeRead("GetLUN_storage_server_connection_mapByLUNBystorage_server_conn",
                StorageServerConnectionLunMapRowMapper.instance,
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
                StorageServerConnectionLunMapRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<LUNStorageServerConnectionMap> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetAllFromLUN_storage_server_connection_map",
                StorageServerConnectionLunMapRowMapper.instance,
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
}
