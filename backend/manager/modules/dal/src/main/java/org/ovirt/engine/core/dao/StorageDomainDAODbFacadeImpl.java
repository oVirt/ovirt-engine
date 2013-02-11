package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <code>StorageDomainDAODbFacadeImpl</code> provides an implementation of {@link StorageDomainDAO} based on code from
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 */
@SuppressWarnings("synthetic-access")
public class StorageDomainDAODbFacadeImpl extends BaseDAODbFacade implements StorageDomainDAO {

    @Override
    public Guid getMasterStorageDomainIdForPool(Guid pool) {
        return getStorageDomainIdForPoolByType(pool, StorageDomainType.Master);
    }

    @Override
    public Guid getIsoStorageDomainIdForPool(Guid pool) {
        return getStorageDomainIdForPoolByType(pool, StorageDomainType.ISO);
    }

    @Override
    public storage_domains get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public storage_domains get(Guid id, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("Getstorage_domains_By_id",
                StorageDomainRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public storage_domains getForStoragePool(Guid id, NGuid storagepool) {
        return getCallsHandler().executeRead("Getstorage_domains_By_id_and_by_storage_pool_id",
                StorageDomainRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("id", id).addValue("storage_pool_id", storagepool));
    }

    @Override
    public List<storage_domains> getAllForConnection(String connection) {
        return getCallsHandler().executeReadList("Getstorage_domains_By_connection", StorageDomainRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("connection", connection));
    }

    @Override
    public List<storage_domains> getAllByConnectionId(Guid connectionId) {
        return getCallsHandler().executeReadList("GetAllFromStorageDomainsByConnectionId",
                StorageDomainRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("connection_id", connectionId));
    }

    @Override
    public List<storage_domains> getAllForStoragePool(Guid pool) {
        return getAllForStoragePool(pool, null, false);
    }

    @Override
    public List<storage_domains> getAllForStoragePool(Guid pool, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("Getstorage_domains_By_storagePoolId",
                StorageDomainRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_id", pool)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public List<storage_domains> getAllForStorageDomain(Guid id) {
        return getCallsHandler().executeReadList("Getstorage_domains_List_By_storageDomainId",
                StorageDomainRowMapper.instance, getCustomMapSqlParameterSource()
                        .addValue("storage_domain_id", id));
    }

    @Override
    public List<storage_domains> getAllWithQuery(String query) {
        return new SimpleJdbcTemplate(jdbcTemplate).query(query, StorageDomainRowMapper.instance);
    }

    @Override
    public List<storage_domains> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<storage_domains> getAll(Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetAllFromstorage_domains",
                StorageDomainRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public void remove(Guid id) {
        getCallsHandler().executeModification("Force_Delete_storage_domain", getCustomMapSqlParameterSource()
                .addValue("storage_domain_id", id));
    }

    @Override
    public List<storage_domains> getAllStorageDomainsByImageId(Guid imageId) {
        return getCallsHandler().executeReadList("Getstorage_domains_List_By_ImageId",
                StorageDomainRowMapper.instance,getCustomMapSqlParameterSource()
                .addValue("image_id", imageId));
    }

    /**
     * Row mapper to map a returned row to a {@link storage_domains} object.
     */
    private static final class StorageDomainRowMapper implements ParameterizedRowMapper<storage_domains> {
        // single instance
        public static final StorageDomainRowMapper instance = new StorageDomainRowMapper();

        @Override
        public storage_domains mapRow(final ResultSet rs, final int rowNum)
                throws SQLException {
            final storage_domains entity = new storage_domains();
            entity.setId(Guid.createGuidFromString(rs.getString("id")));
            entity.setstorage(rs.getString("storage"));
            entity.setstorage_name(rs.getString("storage_name"));
            entity.setDescription(rs.getString("storage_description"));
            entity.setstorage_pool_id(NGuid.createGuidFromString(rs.getString("storage_pool_id")));
            entity.setstorage_type(StorageType.forValue(rs.getInt("storage_type")));
            entity.setstorage_pool_name(rs.getString("storage_pool_name"));
            entity.setstorage_domain_type(StorageDomainType.forValue(rs.getInt("storage_domain_type")));
            entity.setStorageFormat(StorageFormatType.forValue(rs.getString("storage_domain_format_type")));
            entity.setavailable_disk_size((Integer) rs.getObject("available_disk_size"));
            entity.setused_disk_size((Integer) rs.getObject("used_disk_size"));
            entity.setcommitted_disk_size(rs.getInt("commited_disk_size"));
            entity.setstatus(StorageDomainStatus.forValue(rs.getInt("status")));
            entity.setstorage_domain_shared_status(
                    StorageDomainSharedStatus.forValue(rs.getInt("storage_domain_shared_status")));
            entity.setAutoRecoverable(rs.getBoolean("recoverable"));
            entity.setLastTimeUsedAsMaster(rs.getLong("last_time_used_as_master"));
            return entity;
        }
    }

    @Override
    public List<storage_domains> getAllByStoragePoolAndConnection(Guid storagePoolId, String connection) {
        return getCallsHandler().executeReadList("Getstorage_domains_By_storage_pool_id_and_connection",
                StorageDomainRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_id", storagePoolId)
                        .addValue("connection", connection));
    }

    @Override
    public List<storage_domains> listFailedAutorecoverables() {
        return getCallsHandler().executeReadList("GetFailingStorage_domains", StorageDomainRowMapper.instance, null);
    }

    @Override
    public List<storage_domains> getPermittedStorageDomainsByStoragePool(Guid userId, ActionGroup actionGroup, Guid storagePoolId) {
        return getCallsHandler().executeReadList("Getstorage_domains_by_storage_pool_id_with_permitted_action",
                StorageDomainRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("user_id", userId)
                        .addValue("action_group_id", actionGroup.getId())
                        .addValue("storage_pool_id", storagePoolId));
    }

    @Override
    public storage_domains getPermittedStorageDomainsById(Guid userId, ActionGroup actionGroup, Guid storageDomainId) {
        return getCallsHandler().executeRead("Getstorage_domain_by_id_with_permitted_action",
                StorageDomainRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("user_id", userId)
                        .addValue("action_group_id", actionGroup.getId())
                        .addValue("storage_id", storageDomainId));
    }

    /**
     * Gets the storage domain id of the given type for the given storage pool
     *
     * @param pool
     * @param type
     * @return
     */
    private Guid getStorageDomainIdForPoolByType(Guid pool, StorageDomainType type) {
        Guid returnValue = Guid.Empty;
        List<storage_domains> domains = getAllForStoragePool(pool);
        for (storage_domains domain : domains) {
            if (domain.getstorage_domain_type() == type) {
                returnValue = domain.getId();
                break;
            }
        }
        return returnValue;
    }
}
