package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;

/**
 * <code>StorageServerConnectionDAODbFacadeImpl</code> provides an implementation of {@link StorageServerConnectionDAO}
 * based on code from {@link DbFacade}.
 *
 *
 */
public class StorageServerConnectionDAODbFacadeImpl extends BaseDAODbFacade implements
        StorageServerConnectionDAO {

    @Override
    public StorageServerConnections get(String id) {
        return getCallsHandler().executeRead("Getstorage_server_connectionsByid", mapper, getIdParameterSource(id));
    }

    @Override
    public StorageServerConnections getForIqn(String iqn) {
        return getCallsHandler().executeRead("Getstorage_server_connectionsByIqn", mapper,
                getCustomMapSqlParameterSource()
                        .addValue("iqn", iqn));
    }

    @Override
    public List<StorageServerConnections> getAll() {
        return getCallsHandler().executeReadList("GetAllFromstorage_server_connections",
                mapper,
                getCustomMapSqlParameterSource());
    }

    @Override
    public List<StorageServerConnections> getAllForStoragePool(Guid pool) {
        return getCallsHandler().executeReadList("Getstorage_server_connectionsByStoragePoolId",
                mapper,
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_id", pool));
    }

    @Override
    public List<StorageServerConnections> getAllForVolumeGroup(String group) {
        return getCallsHandler().executeReadList("Getstorage_server_connectionsByVolumeGroupId",
                mapper,
                getCustomMapSqlParameterSource()
                        .addValue("volume_group_id", group));
    }

    @Override
    public List<StorageServerConnections> getAllForStorage(String storage) {
        return getCallsHandler().executeReadList("Getstorage_server_connectionsByConnection", mapper,
                getCustomMapSqlParameterSource()
                        .addValue("connection", storage));
    }

    @Override
    public List<StorageServerConnections> getAllForLun(String lunId) {
        return getCallsHandler().executeReadList("Getstorage_server_connectionsByLunId", mapper,
                getCustomMapSqlParameterSource()
                        .addValue("lunId", lunId));
    }

    @Override
    public List<StorageServerConnections> getAllForConnection(
            StorageServerConnections connection) {
        return getCallsHandler().executeReadList("Getstorage_server_connectionsByKey",
                mapper,
                getCustomMapSqlParameterSource()
                        .addValue("iqn", connection.getiqn())
                        .addValue("connection", connection.getconnection())
                        .addValue("port", connection.getport())
                        .addValue("portal", connection.getportal())
                        .addValue("username", connection.getuser_name())
                        .addValue("password", DbFacadeUtils.encryptPassword(connection.getpassword())));
    }

    @Override
    public void save(StorageServerConnections connection) {
        getCallsHandler().executeModification("Insertstorage_server_connections", getFullParameterSource(connection));
    }

    @Override
    public void update(StorageServerConnections connection) {
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

    private MapSqlParameterSource getFullParameterSource(final StorageServerConnections connection) {
        return getIdParameterSource(connection.getid())
                .addValue("connection", connection.getconnection())
                .addValue("iqn", connection.getiqn())
                .addValue("port", connection.getport())
                .addValue("portal", connection.getportal())
                .addValue("password", DbFacadeUtils.encryptPassword(connection.getpassword()))
                .addValue("storage_type", connection.getstorage_type())
                .addValue("user_name", connection.getuser_name())
                .addValue("mount_options", connection.getMountOptions())
                .addValue("vfs_type", connection.getVfsType())
                .addValue("nfs_version", (connection.getNfsVersion() != null) ? connection.getNfsVersion().getValue() : null)
                .addValue("nfs_timeo", connection.getNfsTimeo())
                .addValue("nfs_retrans", connection.getNfsRetrans());
    }

    private static final ParameterizedRowMapper<StorageServerConnections> mapper =
            new ParameterizedRowMapper<StorageServerConnections>() {
                @Override
                public StorageServerConnections mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                    StorageServerConnections entity = new StorageServerConnections();
                    entity.setconnection(rs.getString("connection"));
                    entity.setid(rs.getString("id"));
                    entity.setiqn(rs.getString("iqn"));
                    entity.setport(rs.getString("port"));
                    entity.setportal(rs.getString("portal"));
                    entity.setpassword(DbFacadeUtils.decryptPassword(rs.getString("password")));
                    entity.setstorage_type(StorageType.forValue(rs
                            .getInt("storage_type")));
                    entity.setuser_name(rs.getString("user_name"));
                    entity.setMountOptions(rs.getString("mount_options"));
                    entity.setVfsType(rs.getString("vfs_type"));
                    entity.setNfsVersion((rs.getString("nfs_version") != null) ?
                            NfsVersion.forValue(rs.getString("nfs_version")) : null);
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
