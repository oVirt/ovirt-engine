package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMapId;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class ImageStorageDomainMapDaoImpl extends BaseDao implements ImageStorageDomainMapDao {

    @Override
    public void save(ImageStorageDomainMap entity) {
        getCallsHandler().executeModification("Insertimage_storage_domain_map",
                getCustomMapSqlParameterSource().addValue("image_id",
                        entity.getImageId()).addValue("storage_domain_id",
                        entity.getStorageDomainId())
                        .addValue("quota_id", entity.getQuotaId())
                        .addValue("disk_profile_id", entity.getDiskProfileId()));
    }

    @Override
    public void update(ImageStorageDomainMap entity) {
        throw new UnsupportedOperationException();
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
    public List<ImageStorageDomainMap> getAllByStorageDomainId(Guid storageDomainId) {
        return getCallsHandler().executeReadList("Getimage_storage_domain_mapBystorage_domain_id",
                IMAGE_STORAGE_DOMAIN_MAP_MAPPER,
                getCustomMapSqlParameterSource().addValue("storage_domain_id",
                        storageDomainId));
    }

    @Override
    public List<ImageStorageDomainMap> getAllByImageId(Guid imageId) {
        return getCallsHandler().executeReadList("Getimage_storage_domain_mapByimage_id",
                IMAGE_STORAGE_DOMAIN_MAP_MAPPER,
                getCustomMapSqlParameterSource().addValue("image_id",
                        imageId));
    }

    @Override
    public ImageStorageDomainMap get(ImageStorageDomainMapId id) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ImageStorageDomainMap> getAll() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateQuotaForImageAndSnapshots(Guid diskId, Guid storageDomainId, Guid quotaId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("disk_id", diskId)
                .addValue("storage_domain_id", storageDomainId)
                .addValue("quota_id", quotaId);
        getCallsHandler().executeModification("updateQuotaForImageAndSnapshots", parameterSource);
    }

    @Override
    public void updateDiskProfileByImageGroupIdAndStorageDomainId(Guid diskId, Guid storageDomainId, Guid diskProfileId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_group_id", diskId)
                .addValue("storage_domain_id", storageDomainId)
                .addValue("disk_profile_id", diskProfileId);
        getCallsHandler().executeModification("UpdateDiskProfileByImageGroupId", parameterSource);
    }

    private static final RowMapper<ImageStorageDomainMap> IMAGE_STORAGE_DOMAIN_MAP_MAPPER = (rs, rowNum) -> {
        ImageStorageDomainMap entity = new ImageStorageDomainMap();
        entity.setImageId(getGuidDefaultEmpty(rs, "image_id"));
        entity.setStorageDomainId(getGuidDefaultEmpty(rs, "storage_domain_id"));
        entity.setQuotaId(getGuid(rs, "quota_id"));
        entity.setDiskProfileId(getGuid(rs, "disk_profile_id"));
        return entity;
    };
}
