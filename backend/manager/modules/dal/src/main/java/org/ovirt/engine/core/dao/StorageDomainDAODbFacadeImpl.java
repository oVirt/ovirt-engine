package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
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
        return getCallsHandler().executeReadList("GetAllFromstorage_domains",
                StorageDomainRowMapper.instance,
                getCustomMapSqlParameterSource());
    }

    @Override
    public List<Guid> getAllStorageDomainsByImageGroup(Guid imageGroupId) {
        return getCallsHandler().executeReadList("Getstorage_domainsId_By_imageGroupId",
                new ParameterizedRowMapper<Guid>() {
                    @Override
                    public Guid mapRow(ResultSet rs, int rowNum)
                            throws SQLException {
                        return Guid.createGuidFromString(rs.getString("storage_id"));
                    }
                },
                getCustomMapSqlParameterSource()
                        .addValue("image_group_id", imageGroupId));
    }

    @Override
    public void remove(Guid id) {
        getCallsHandler().executeModification("Force_Delete_storage_domain", getCustomMapSqlParameterSource()
                .addValue("storage_domain_id", id));
    }

    @Override
    public void addImageStorageDomainMap(image_storage_domain_map image_group_storage_domain_map) {
        getCallsHandler().executeModification("Insertimage_storage_domain_map",
                getCustomMapSqlParameterSource().addValue("image_id",
                        image_group_storage_domain_map.getimage_id()).addValue("storage_domain_id",
                        image_group_storage_domain_map.getstorage_domain_id()));
    }

    @Override
    public void removeImageStorageDomainMap(image_storage_domain_map image_group_storage_domain_map) {
        getCallsHandler().executeModification("Deleteimage_storage_domain_map",
                getCustomMapSqlParameterSource().addValue("image_id",
                        image_group_storage_domain_map.getimage_id()).addValue("storage_domain_id",
                        image_group_storage_domain_map.getstorage_domain_id()));
    }

    @Override
    public List<image_storage_domain_map> getAllImageStorageDomainMapsForStorageDomain(Guid storage_domain_id) {
        return getCallsHandler().executeReadList("Getimage_storage_domain_mapBystorage_domain_id",
                new ImageStorageDomainRowMapper(),
                getCustomMapSqlParameterSource().addValue("storage_domain_id",
                        storage_domain_id));
    }

    @Override
    public void removeImageStorageDomainMap(Guid image_id) {
        getCallsHandler().executeModification("Deleteimage_storage_domain_map_by_image_id",
                getCustomMapSqlParameterSource().addValue("image_id",
                        image_id));
    }

    @Override
    public ArrayList<Guid> getAllImageStorageDomainIdsForImage(Guid image_id) {
        List<image_storage_domain_map> image_storage_domain_maps = getAllImageStorageDomainMapsForImage(image_id);
        ArrayList<Guid> guids = new ArrayList<Guid>();
        for (image_storage_domain_map image_storage_domain_map : image_storage_domain_maps) {
            guids.add(image_storage_domain_map.getstorage_domain_id());
        }
        return guids;
    }

    @Override
    public List<image_storage_domain_map> getAllImageStorageDomainMapsForImage(Guid image_id) {
        return getCallsHandler().executeReadList("Getimage_storage_domain_mapByimage_id",
                new ImageStorageDomainRowMapper(),
                getCustomMapSqlParameterSource().addValue("image_id",
                        image_id));
    }

    /**
     * Row mapper to map a returned row to a {@link image_storage_domain_map} object.
     */
    private final class ImageStorageDomainRowMapper implements ParameterizedRowMapper<image_storage_domain_map> {
        @Override
        public image_storage_domain_map mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            image_storage_domain_map entity = new image_storage_domain_map();
            entity.setimage_id(Guid.createGuidFromString(rs.getString("image_id")));
            entity.setstorage_domain_id(Guid.createGuidFromString(rs.getString("storage_domain_id")));
            return entity;
        }
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
