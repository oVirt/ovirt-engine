package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.StorageType;
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
    public List<StorageServerConnections> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();
        return getCallsHandler().executeReadList("GetAllstorage_server_connections", mapper, parameterSource);
    }

    @Override
    public StorageServerConnections get(String id) {
        return getCallsHandler().executeRead("Getstorage_server_connectionsByid", mapper, getIdParameterSource(id));
    }

    @Override
    public List<StorageServerConnections> getByIds(List<String> ids) {
        return getCallsHandler().executeReadList("GetStorageServerConnectionsByIds",
                mapper, getCustomMapSqlParameterSource().addValue("ids", StringUtils.join(ids, ",")));
    }

    @Override
    public StorageServerConnections getForIqn(String iqn) {
        return getCallsHandler().executeRead("Getstorage_server_connectionsByIqn", mapper,
                getCustomMapSqlParameterSource()
                        .addValue("iqn", iqn));
    }

    @Override
    public List<StorageServerConnections> getAllConnectableStorageSeverConnection(Guid pool) {
        return getConnectableStorageConnectionsByStorageType(pool, null);
    }

    @Override
    public List<StorageServerConnections> getConnectableStorageConnectionsByStorageType(Guid pool,
            StorageType storageType) {
        return getStorageConnectionsByStorageTypeAndStatus(pool,
                storageType,
                EnumSet.of(StorageDomainStatus.Active, StorageDomainStatus.Inactive, StorageDomainStatus.Unknown));
    }

    @Override
    public List<StorageServerConnections> getStorageConnectionsByStorageTypeAndStatus(Guid pool,
                                                                                      StorageType storageType, Set<StorageDomainStatus> statuses) {
        List<String> statusesVals = new LinkedList<>();
        for (StorageDomainStatus status : statuses) {
            statusesVals.add(Integer.toString(status.getValue()));
        }
        return getCallsHandler().executeReadList("GetStorageConnectionsByStorageTypeAndStatus",
                mapper,
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_id", pool)
                        .addValue("storage_type", (storageType != null) ? storageType.getValue() : null)
                        .addValue("statuses", StringUtils.join(statusesVals, ",")));
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
        // NOTE - any change to this stored procedure parameters should require a change in
        // the StorageServerConnections class, as those fields can be set only with null or a
        // actual value (empty string can't be used).
        return getCallsHandler().executeReadList("Getstorage_server_connectionsByKey",
                mapper,
                getCustomMapSqlParameterSource()
                        .addValue("iqn", connection.getiqn())
                        .addValue("connection", connection.getconnection())
                        .addValue("port", connection.getport())
                        .addValue("portal", connection.getportal())
                        .addValue("username", connection.getuser_name()));
    }

    @Override
    public List<StorageServerConnections> getAllForDomain(Guid domainId) {
        return getCallsHandler().executeReadList("GetStorageServerConnectionsForDomain", mapper,
                        getCustomMapSqlParameterSource().addValue("storage_domain_id", domainId));
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

    private static final RowMapper<StorageServerConnections> mapper =
            new RowMapper<StorageServerConnections>() {
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
