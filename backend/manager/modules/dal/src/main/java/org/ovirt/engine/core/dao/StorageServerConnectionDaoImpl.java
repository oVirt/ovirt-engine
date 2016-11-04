package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.NfsVersion;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code StorageServerConnectionDaoImpl} provides an implementation of {@link StorageServerConnectionDao}.
 */
@Named
@Singleton
public class StorageServerConnectionDaoImpl extends BaseDao implements
        StorageServerConnectionDao {

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
        List<String> statusesVals =
                statuses.stream().map(status -> Integer.toString(status.getValue())).collect(Collectors.toList());
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
                        .addValue("iqn", connection.getIqn())
                        .addValue("connection", connection.getConnection())
                        .addValue("port", connection.getPort())
                        .addValue("portal", connection.getPortal())
                        .addValue("username", connection.getUserName()));
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
        return getIdParameterSource(connection.getId())
                .addValue("connection", connection.getConnection())
                .addValue("iqn", connection.getIqn())
                .addValue("port", connection.getPort())
                .addValue("portal", connection.getPortal())
                .addValue("password", DbFacadeUtils.encryptPassword(connection.getPassword()))
                .addValue("storage_type", connection.getStorageType())
                .addValue("user_name", connection.getUserName())
                .addValue("mount_options", connection.getMountOptions())
                .addValue("vfs_type", connection.getVfsType())
                .addValue("nfs_version", (connection.getNfsVersion() != null) ? connection.getNfsVersion().getValue() : null)
                .addValue("nfs_timeo", connection.getNfsTimeo())
                .addValue("nfs_retrans", connection.getNfsRetrans())
                .addValue("gluster_volume_id", connection.getGlusterVolumeId());
    }

    private static final RowMapper<StorageServerConnections> mapper = (rs, rowNum) -> {
        StorageServerConnections entity = new StorageServerConnections();
        entity.setConnection(rs.getString("connection"));
        entity.setId(rs.getString("id"));
        entity.setIqn(rs.getString("iqn"));
        entity.setPort(rs.getString("port"));
        entity.setPortal(rs.getString("portal"));
        entity.setPassword(DbFacadeUtils.decryptPassword(rs.getString("password")));
        entity.setStorageType(StorageType.forValue(rs.getInt("storage_type")));
        entity.setUserName(rs.getString("user_name"));
        entity.setMountOptions(rs.getString("mount_options"));
        entity.setVfsType(rs.getString("vfs_type"));
        entity.setNfsVersion((rs.getString("nfs_version") != null) ?
                NfsVersion.forValue(rs.getString("nfs_version")) : null);
        entity.setNfsRetrans(getShort(rs, "nfs_retrans"));
        entity.setNfsTimeo(getShort(rs, "nfs_timeo"));
        entity.setGlusterVolumeId(getGuid(rs, "gluster_volume_id"));
        return entity;
    };

    /**
     * Get a Short (therefore a number or a null) from a ResultSet.
     * @param rs        resultset
     * @param column    column name
     * @return  the number, of null if the data in the DB is NULL
     */
    static Short getShort(final ResultSet rs, final String column) throws SQLException {
        short ret = rs.getShort(column);
        if (ret == 0 && rs.wasNull()) {
            return null;
        }
        return ret;
    }
}
