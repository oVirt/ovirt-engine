package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.lang.NotImplementedException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map;
import org.ovirt.engine.core.common.businessentities.LUN_storage_server_connection_map_id;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

/**
 * <code>StorageServerConnectionLunMapDAODbFacadeImpl</code> provides an implementation of {@link LUN_storage_server_connection_map}
 * based on code from {@link DbFacade}.
 *
 *
 */
public class StorageServerConnectionLunMapDAODbFacadeImpl extends BaseDAODbFacade implements
        StorageServerConnectionLunMapDAO {

    @Override
    public LUN_storage_server_connection_map get(LUN_storage_server_connection_map_id id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("lUN_id", id.lunId).addValue(
                "storage_server_connection", id.storageServerConnection);

        ParameterizedRowMapper<LUN_storage_server_connection_map> mapper =
                new ParameterizedRowMapper<LUN_storage_server_connection_map>() {
                    @Override
                    public LUN_storage_server_connection_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                        LUN_storage_server_connection_map entity = new LUN_storage_server_connection_map();
                        entity.setLunId(rs.getString("LUN_id"));
                        entity.setstorage_server_connection(rs.getString("storage_server_connection"));
                        return entity;
                    }
                };

        return getCallsHandler().executeRead("GetLUN_storage_server_connection_mapByLUNBystorage_server_conn",
                mapper,
                parameterSource);
    }

    @Override
    public void save(LUN_storage_server_connection_map map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("lUN_id", map.getLunId())
                .addValue("storage_server_connection", map.getstorage_server_connection());

        getCallsHandler().executeModification("InsertLUN_storage_server_connection_map", parameterSource);
    }

    @Override
    public List<LUN_storage_server_connection_map> getAll(final String lunId) {
        return LinqUtils.filter(getAllLUNStorageServerConnection(),
                new Predicate<LUN_storage_server_connection_map>() {
                    @Override
                    public boolean eval(LUN_storage_server_connection_map a) {
                        return a.getLunId().equals(lunId);
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private List<LUN_storage_server_connection_map> getAllLUNStorageServerConnection() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<LUN_storage_server_connection_map> mapper =
                new ParameterizedRowMapper<LUN_storage_server_connection_map>() {
                    @Override
                    public LUN_storage_server_connection_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                        LUN_storage_server_connection_map entity = new LUN_storage_server_connection_map();
                        entity.setLunId(rs.getString("LUN_id"));
                        entity.setstorage_server_connection(rs.getString("storage_server_connection"));
                        return entity;
                    }
                };

        return getCallsHandler().executeReadList("GetAllFromLUN_storage_server_connection_map", mapper, parameterSource);
    }

    @Override
    public List<LUN_storage_server_connection_map> getAll() {
        throw new NotImplementedException();
    }

    @Override
    public void update(LUN_storage_server_connection_map entity) {
        throw new NotImplementedException();
    }

    @Override
    public void remove(LUN_storage_server_connection_map_id id) {
        throw new NotImplementedException();
    }
}
