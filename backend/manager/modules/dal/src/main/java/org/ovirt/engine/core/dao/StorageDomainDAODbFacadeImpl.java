package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.image_group_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
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
        Guid returnValue = Guid.Empty;
        List<storage_domains> domains = getAllForStoragePool(pool);
        for (storage_domains domain : domains) {
            if (domain.getstorage_domain_type() == StorageDomainType.Master) {
                returnValue = domain.getId();
                break;
            }
        }
        return returnValue;
    }

    @Override
    public storage_domains get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        ParameterizedRowMapper<storage_domains> mapper = new StorageDomainRowMapper();
        return getCallsHandler().executeRead("Getstorage_domains_By_id", mapper, parameterSource);
    }

    @Override
    public storage_domains getForStoragePool(Guid id, NGuid storagepool) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id).addValue("storage_pool_id", storagepool);

        ParameterizedRowMapper<storage_domains> mapper = new StorageDomainRowMapper();
        return getCallsHandler().executeRead("Getstorage_domains_By_id_and_by_storage_pool_id", mapper
                , parameterSource);
    }

    @Override
    public List<storage_domains> getAllForConnection(String connection) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("connection", connection);

        ParameterizedRowMapper<storage_domains> mapper = new StorageDomainRowMapper();

        return getCallsHandler().executeReadList("Getstorage_domains_By_connection", mapper,
                parameterSource);
    }

    @Override
    public List<storage_domains> getAllForStoragePool(Guid pool) {
        return getAllForStoragePool(pool, null, false);
    }

    @Override
    public List<storage_domains> getAllForStoragePool(Guid pool, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", pool).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        ParameterizedRowMapper<storage_domains> mapper = new StorageDomainRowMapper();

        return getCallsHandler().executeReadList("Getstorage_domains_By_storagePoolId", mapper,
                parameterSource);
    }

    @Override
    public List<storage_domains> getAllForImageGroup(NGuid group) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_group_id", group);

        ParameterizedRowMapper<storage_domains> mapper = new StorageDomainRowMapper();

        return getCallsHandler().executeReadList("Getstorage_domains_By_imageGroupId", mapper,
                parameterSource);
    }

    @Override
    public List<storage_domains> getAllForStorageDomain(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_domain_id", id);

        ParameterizedRowMapper<storage_domains> mapper = new StorageDomainRowMapper();
        return getCallsHandler().executeReadList("Getstorage_domains_List_By_storageDomainId",
                mapper, parameterSource);
    }

    @Override
    public List<storage_domains> getAllWithQuery(String query) {
        ParameterizedRowMapper<storage_domains> mapper = new StorageDomainRowMapper();

        return new SimpleJdbcTemplate(jdbcTemplate).query(query, mapper);
    }

    @Override
    public List<storage_domains> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<storage_domains> mapper = new StorageDomainRowMapper();

        return getCallsHandler().executeReadList("GetAllFromstorage_domains", mapper, parameterSource);
    }

    @Override
    public List<Guid> getAllStorageDomainsByImageGroup(Guid imageGroupId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_group_id", imageGroupId);

        ParameterizedRowMapper<Guid> mapper = new ParameterizedRowMapper<Guid>() {
            @Override
            public Guid mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                return Guid.createGuidFromString(rs.getString("storage_id"));
            }
        };

        return getCallsHandler().executeReadList("Getstorage_domainsId_By_imageGroupId", mapper, parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_domain_id", id);
        getCallsHandler().executeModification("Force_Delete_storage_domain", parameterSource);
    }

    @Override
    public image_group_storage_domain_map getImageGroupStorageDomainMapForImageGroupAndStorageDomain(image_group_storage_domain_map image_group_storage_domain_map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("image_group_id",
                image_group_storage_domain_map.getimage_group_id()).addValue("storage_domain_id",
                image_group_storage_domain_map.getstorage_domain_id());

        ParameterizedRowMapper<image_group_storage_domain_map> mapper =
                new ParameterizedRowMapper<image_group_storage_domain_map>() {
                    @Override
                    public image_group_storage_domain_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                        image_group_storage_domain_map entity = new image_group_storage_domain_map();
                        entity.setimage_group_id(Guid.createGuidFromString(rs.getString("image_group_id")));
                        entity.setstorage_domain_id(Guid.createGuidFromString(rs.getString("storage_domain_id")));
                        return entity;
                    }
                };

        return getCallsHandler().executeRead("Getimage_grp_storage_domain_mapByimg_grp_idAndstorage_domain",
                mapper,
                parameterSource);
    }

    @Override
    public void addImageGroupStorageDomainMap(image_group_storage_domain_map image_group_storage_domain_map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("image_group_id",
                image_group_storage_domain_map.getimage_group_id()).addValue("storage_domain_id",
                image_group_storage_domain_map.getstorage_domain_id());

        getCallsHandler().executeModification("Insertimage_group_storage_domain_map", parameterSource);
    }

    @Override
    public void removeImageGroupStorageDomainMap(image_group_storage_domain_map image_group_storage_domain_map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("image_group_id",
                image_group_storage_domain_map.getimage_group_id()).addValue("storage_domain_id",
                image_group_storage_domain_map.getstorage_domain_id());

        getCallsHandler().executeModification("Deleteimage_group_storage_domain_map", parameterSource);
    }

    @Override
    public List<image_group_storage_domain_map> getAllImageGroupStorageDomainMapsForStorageDomain(Guid storage_domain_id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_domain_id",
                storage_domain_id);

        ParameterizedRowMapper<image_group_storage_domain_map> mapper =
                new ParameterizedRowMapper<image_group_storage_domain_map>() {
                    @Override
                    public image_group_storage_domain_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                        image_group_storage_domain_map entity = new image_group_storage_domain_map();
                        entity.setimage_group_id(Guid.createGuidFromString(rs.getString("image_group_id")));
                        entity.setstorage_domain_id(Guid.createGuidFromString(rs.getString("storage_domain_id")));
                        return entity;
                    }
                };

        return getCallsHandler().executeReadList("Getimage_group_storage_domain_mapBystorage_domain_id",
                mapper,
                parameterSource);
    }

    @Override
    public List<image_group_storage_domain_map> getAllImageGroupStorageDomainMapsForImage(Guid image_group_id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("image_group_id",
                image_group_id);

        ParameterizedRowMapper<image_group_storage_domain_map> mapper =
                new ParameterizedRowMapper<image_group_storage_domain_map>() {
                    @Override
                    public image_group_storage_domain_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                        image_group_storage_domain_map entity = new image_group_storage_domain_map();
                        entity.setimage_group_id(Guid.createGuidFromString(rs.getString("image_group_id")));
                        entity.setstorage_domain_id(Guid.createGuidFromString(rs.getString("storage_domain_id")));
                        return entity;
                    }
                };

        return getCallsHandler().executeReadList("Getimage_group_storage_domain_mapByimage_group_id",
                mapper,
                parameterSource);
    }

    /**
     * Row mapper to map a returned row to a {@link storage_domains} object.
     */
    private final class StorageDomainRowMapper implements ParameterizedRowMapper<storage_domains> {
        @Override
        public storage_domains mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            storage_domains entity = new storage_domains();
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
            return entity;
        }
    }

    @Override
    public List<storage_domains> getAllByStoragePoolAndConnection(Guid storagePoolId, String connection) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource()
                        .addValue("storage_pool_id", storagePoolId)
                        .addValue("connection", connection);

        ParameterizedRowMapper<storage_domains> mapper = new StorageDomainRowMapper();

        return getCallsHandler().executeReadList("Getstorage_domains_By_storage_pool_id_and_connection",
                mapper,
                parameterSource);
    }
}
