package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.businessentities.ImageStorageDomainMapId;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;

public class ImageStorageDomainMapDaoDbFacadeImpl extends BaseDAODbFacade implements ImageStorageDomainMapDao {

    @Override
    public void save(image_storage_domain_map entity) {
        getCallsHandler().executeModification("Insertimage_storage_domain_map",
                getCustomMapSqlParameterSource().addValue("image_id",
                        entity.getimage_id()).addValue("storage_domain_id",
                        entity.getstorage_domain_id()));
    }

    @Override
    public void update(image_storage_domain_map entity) {
        throw new NotImplementedException();
    }

    @Override
    public void remove(ImageStorageDomainMapId id) {
        getCallsHandler().executeModification("Deleteimage_storage_domain_map",
                getCustomMapSqlParameterSource().addValue("image_id",
                        id.getImageId()).addValue("storage_domain_id",
                        id.getStorageDomainId()));
    }

    @Override
    public void remove(Guid imageId) {
        getCallsHandler().executeModification("Deleteimage_storage_domain_map_by_image_id",
                getCustomMapSqlParameterSource().addValue("image_id",
                        imageId));
    }

    @Override
    public List<image_storage_domain_map> getAllByStorageDomainId(Guid storageDomainId) {
        return getCallsHandler().executeReadList("Getimage_storage_domain_mapBystorage_domain_id",
                IMAGE_STORAGE_DOMAIN_MAP_MAPPER,
                getCustomMapSqlParameterSource().addValue("storage_domain_id",
                        storageDomainId));
    }

    @Override
    public List<image_storage_domain_map> getAllByImageId(Guid imageId) {
        return getCallsHandler().executeReadList("Getimage_storage_domain_mapByimage_id",
                IMAGE_STORAGE_DOMAIN_MAP_MAPPER,
                getCustomMapSqlParameterSource().addValue("image_id",
                        imageId));
    }

    @Override
    public image_storage_domain_map get(ImageStorageDomainMapId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<image_storage_domain_map> getAll() {
        throw new UnsupportedOperationException();
    }

    private static final RowMapper<image_storage_domain_map> IMAGE_STORAGE_DOMAIN_MAP_MAPPER =
            new RowMapper<image_storage_domain_map>() {

                @Override
                public image_storage_domain_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                    image_storage_domain_map entity = new image_storage_domain_map();
                    entity.setimage_id(Guid.createGuidFromStringDefaultEmpty(rs.getString("image_id")));
                    entity.setstorage_domain_id(Guid.createGuidFromStringDefaultEmpty(rs.getString("storage_domain_id")));
                    return entity;
                }
            };
}
