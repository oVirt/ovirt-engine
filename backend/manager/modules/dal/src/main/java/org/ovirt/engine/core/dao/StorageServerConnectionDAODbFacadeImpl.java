package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>StorageServerConnectionDAODbFacadeImpl</code> provides an implementation of {@link StorageServerConnectionDAO}
 * based on code from {@link DbFacade}.
 *
 *
 */
public class StorageServerConnectionDAODbFacadeImpl extends BaseDAODbFacade implements
        StorageServerConnectionDAO {

    private static final ParameterizedRowMapper<storage_server_connections> mapper = new ParameterizedRowMapper<storage_server_connections>() {
        @Override
        public storage_server_connections mapRow(ResultSet rs, int rowNum)
                throws SQLException {
                    storage_server_connections entity = new storage_server_connections();
                    entity.setconnection(rs.getString("connection"));
                    entity.setid(rs.getString("id"));
                    entity.setiqn(rs.getString("iqn"));
                    entity.setport(rs.getString("port"));
                    entity.setportal(rs.getString("portal"));
                    entity.setpassword(rs.getString("password"));
                    entity.setstorage_type(StorageType.forValue(rs
                            .getInt("storage_type")));
                    entity.setuser_name(rs.getString("user_name"));
                    entity.setMountOptions(rs.getString("mount_options"));
                    entity.setVfsType(rs.getString("vfs_type"));
                    return entity;
        }
            };

    @Override
    public storage_server_connections get(String id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);
        return getCallsHandler().executeRead("Getstorage_server_connectionsByid", mapper, parameterSource);
    }

    @Override
    public storage_server_connections getForIqn(String iqn) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("iqn", iqn);
        return getCallsHandler().executeRead("Getstorage_server_connectionsByIqn", mapper,
                parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<storage_server_connections> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();
        return getCallsHandler().executeReadList("GetAllFromstorage_server_connections", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<storage_server_connections> getAllForStoragePool(Guid pool) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", pool);
        return getCallsHandler().executeReadList("Getstorage_server_connectionsByStoragePoolId",
                mapper,
                parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<storage_server_connections> getAllForVolumeGroup(String group) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("volume_group_id", group);
        return getCallsHandler().executeReadList("Getstorage_server_connectionsByVolumeGroupId",
                mapper,
                parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<storage_server_connections> getAllForStorage(String storage) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("connection", storage);
        return getCallsHandler().executeReadList("Getstorage_server_connectionsByConnection", mapper,
                parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<storage_server_connections> getAllForConnection(
            storage_server_connections connection) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("iqn", connection.getiqn())
                .addValue("connection", connection.getconnection())
                .addValue("port", connection.getport())
                .addValue("portal", connection.getportal())
                .addValue("username", connection.getuser_name())
                .addValue("password", connection.getpassword());
      return getCallsHandler().executeReadList("Getstorage_server_connectionsByKey",mapper,parameterSource);
    }

    @Override
    public void save(storage_server_connections connection) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("connection", connection.getconnection())
                .addValue("id", connection.getid())
                .addValue("iqn", connection.getiqn())
                .addValue("port", connection.getport())
                .addValue("portal", connection.getportal())
                .addValue("password", connection.getpassword())
                .addValue("storage_type", connection.getstorage_type())
                .addValue("user_name", connection.getuser_name())
                .addValue("mount_options", connection.getMountOptions())
                .addValue("vfs_type", connection.getVfsType());

        getCallsHandler().executeModification("Insertstorage_server_connections", parameterSource);
    }

    @Override
    public void update(storage_server_connections connection) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("connection", connection.getconnection())
                .addValue("id", connection.getid())
                .addValue("iqn", connection.getiqn())
                .addValue("password", connection.getpassword())
                .addValue("storage_type", connection.getstorage_type())
                .addValue("port", connection.getport())
                .addValue("portal", connection.getportal())
                .addValue("user_name", connection.getuser_name())
                .addValue("mount_options", connection.getMountOptions())
                .addValue("vfs_type", connection.getVfsType());

        getCallsHandler().executeModification("Updatestorage_server_connections", parameterSource);
    }

    @Override
    public void remove(String id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        getCallsHandler().executeModification("Deletestorage_server_connections", parameterSource);
    }
}
