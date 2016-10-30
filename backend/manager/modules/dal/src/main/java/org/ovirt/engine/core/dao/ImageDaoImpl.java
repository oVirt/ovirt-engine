package org.ovirt.engine.core.dao;

import java.util.Date;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class ImageDaoImpl extends DefaultGenericDao<Image, Guid> implements ImageDao {

    public ImageDaoImpl() {
        super("Image");
    }

    @Override
    public void updateStatus(Guid id, ImageStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_id", id)
                .addValue("status", status);
        getCallsHandler().executeModification("UpdateImageStatus", parameterSource);
    }

    @Override
    public void updateImageVmSnapshotId(Guid id, Guid vmSnapshotId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_id", id)
                .addValue("vm_snapshot_id", vmSnapshotId);
        getCallsHandler().executeModification("UpdateImageVmSnapshotId", parameterSource);
    }

    @Override
    public void updateImageSize(Guid id, long size) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_id", id)
                .addValue("size", size)
                .addValue("lastModified", new Date());
        getCallsHandler().executeModification("UpdateImageSize", parameterSource);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(Image entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("creation_date", entity.getCreationDate())
                .addValue("it_guid", entity.getTemplateImageId())
                .addValue("size", entity.getSize())
                .addValue("ParentId", entity.getParentId())
                .addValue("imageStatus", entity.getStatus())
                .addValue("lastModified", entity.getLastModified())
                .addValue("vm_snapshot_id", entity.getSnapshotId())
                .addValue("volume_type", entity.getVolumeType())
                .addValue("volume_format", entity.getVolumeFormat())
                .addValue("qcow_compat", entity.getQcowCompat())
                .addValue("image_group_id", entity.getDiskId())
                .addValue("active", entity.isActive())
                .addValue("volume_classification", entity.getVolumeClassification().getValue());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("image_guid", id);
    }

    @Override
    protected RowMapper<Image> createEntityRowMapper() {
        return (rs, rowNum) -> {
            Image entity = new Image();
            entity.setId(getGuidDefaultEmpty(rs, "image_guid"));
            entity.setCreationDate(DbFacadeUtils.fromDate(rs.getTimestamp("creation_date")));
            entity.setTemplateImageId(getGuidDefaultEmpty(rs, "it_guid"));
            entity.setSize(rs.getLong("size"));
            entity.setParentId(getGuidDefaultEmpty(rs, "ParentId"));
            entity.setStatus(ImageStatus.forValue(rs.getInt("imageStatus")));
            entity.setLastModified(DbFacadeUtils.fromDate(rs.getTimestamp("lastModified")));
            entity.setSnapshotId(getGuidDefaultEmpty(rs, "vm_snapshot_id"));
            entity.setVolumeType(VolumeType.forValue(rs.getInt("volume_type")));
            entity.setVolumeFormat(VolumeFormat.forValue(rs.getInt("volume_format")));
            if (entity.getVolumeFormat().equals(VolumeFormat.COW)) {
                entity.setQcowCompat(QcowCompat.forValue(rs.getInt("qcow_compat")));
            }
            entity.setDiskId(getGuidDefaultEmpty(rs, "image_group_id"));
            entity.setActive((Boolean) rs.getObject("active"));
            entity.setVolumeClassification(VolumeClassification.forValue(rs.getInt("volume_classification")));
            return entity;
        };
    }

    @Override
    public void updateStatusOfImagesByImageGroupId(Guid imageGroupId, ImageStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_group_id", imageGroupId)
                .addValue("status", status);
        getCallsHandler().executeModification("UpdateStatusOfImagesByImageGroupId", parameterSource);
    }
}
