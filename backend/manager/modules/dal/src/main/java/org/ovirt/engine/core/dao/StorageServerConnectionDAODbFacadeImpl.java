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

    @Override
    public storage_server_connections get(String id) {
        return getCallsHandler().executeRead("Getstorage_server_connectionsByid", mapper, getIdParameterSource(id));
    }

    @Override
    public storage_server_connections getForIqn(String iqn) {
        return getCallsHandler().executeRead("Getstorage_server_connectionsByIqn", mapper,
                getCustomMapSqlParameterSource()
                        .addValue("iqn", iqn));
    }

    @Override
    public List<storage_server_connections> getAll() {
        return getCallsHandler().executeReadList("GetAllFromstorage_server_connections",
                mapper,
                getCustomMapSqlParameterSource());
    }

    @Override
    public List<storage_server_connections> getAllForStoragePool(Guid pool) {
        return getCallsHandler().executeReadList("Getstorage_server_connectionsByStoragePoolId",
                mapper,
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_id", pool));
    }

    @Override
    public List<storage_server_connections> getAllForVolumeGroup(String group) {
        return getCallsHandler().executeReadList("Getstorage_server_connectionsByVolumeGroupId",
                mapper,
                getCustomMapSqlParameterSource()
                        .addValue("volume_group_id", group));
    }

    @Override
    public List<storage_server_connections> getAllForStorage(String storage) {
        return getCallsHandler().executeReadList("Getstorage_server_connectionsByConnection", mapper,
                getCustomMapSqlParameterSource()
                        .addValue("connection", storage));
    }

    @Override
    public List<storage_server_connections> getAllForLun(String lunId) {
        return getCallsHandler().executeReadList("Getstorage_server_connectionsByLunId", mapper,
                getCustomMapSqlParameterSource()
                        .addValue("lunId", lunId));
    }

    @Override
    public List<storage_server_connections> getAllForConnection(
            storage_server_connections connection) {
        return getCallsHandler().executeReadList("Getstorage_server_connectionsByKey",
                mapper,
                getCustomMapSqlParameterSource()
                        .addValue("iqn", connection.getiqn())
                        .addValue("connection", connection.getconnection())
                        .addValue("port", connection.getport())
                        .addValue("portal", connection.getportal())
                        .addValue("username", connection.getuser_name())
                        .addValue("password", connection.getpassword()));
    }

    @Override
    public void save(storage_server_connections connection) {
        getCallsHandler().executeModification("Insertstorage_server_connections", getFullParameterSource(connection));
    }

    @Override
    public void update(storage_server_connections connection) {
        getCallsHandler().executeModification("Updatestorage_server_connections", getFullParameterSource(connection));
    }

    @Override
    public void remove(String id) {
        getCallsHandler().executeModification("Deletestorage_server_connections", getIdParameterSource(id));
    }

    private MapSqlParameterSource getIdParameterSource(String id) {
        return getCustomMapSqlParameterSource()
                .addValue("id", id);
    }

    private MapSqlParameterSource getFullParameterSource(final storage_server_connections connection) {
        return getIdParameterSource(connection.getid())
                .addValue("connection", connection.getconnection())
                .addValue("iqn", connection.getiqn())
                .addValue("port", connection.getport())
                .addValue("portal", connection.getportal())
                .addValue("password", connection.getpassword())
                .addValue("storage_type", connection.getstorage_type())
                .addValue("user_name", connection.getuser_name())
                .addValue("mount_options", connection.getMountOptions())
                .addValue("vfs_type", connection.getVfsType())
                .addValue("nfs_version", connection.getNfsVersion())
                .addValue("nfs_timeo", connection.getNfsTimeo())
                .addValue("nfs_retrans", connection.getNfsRetrans());
    }

    private static final ParameterizedRowMapper<storage_server_connections> mapper =
            new ParameterizedRowMapper<storage_server_connections>() {
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
                    entity.setNfsVersion(getShort(rs, "nfs_version"));
                    entity.setNfsRetrans(getShort(rs, "nfs_retrans"));
                    entity.setNfsTimeo(getShort(rs, "nfs_timeo"));
                    return entity;
                }

            };

    /**
     * Get a Short (therefore a number or a null) from a ResultSet.
     * @param rs        resultset
     * @param column    column name
     * @return  the number, of null if the data in the DB is NULL
     * @throws SQLException
     */
    static Short getShort(final ResultSet rs, final String column) throws SQLException {
        short ret = rs.getShort(column);
        if (ret == 0 && rs.wasNull()) {
            return null;
        }
        return ret;
    }
}
