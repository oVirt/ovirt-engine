package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.ovirt.engine.core.common.businessentities.Image;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class ImageDaoDbFacadeImpl extends DefaultGenericDaoDbFacade<Image, Guid> implements ImageDao {

    public ImageDaoDbFacadeImpl() {
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
                .addValue("image_group_id", entity.getDiskId())
                .addValue("active", entity.isActive());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("image_guid", id);
    }

    @Override
    protected RowMapper<Image> createEntityRowMapper() {
        return ImageRowMapper.instance;
    }

    private static class ImageRowMapper implements RowMapper<Image> {

        public static ImageRowMapper instance = new ImageRowMapper();

        private ImageRowMapper() {
        }

        @Override
        public Image mapRow(ResultSet rs, int rowNum) throws SQLException {
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
            entity.setDiskId(getGuidDefaultEmpty(rs, "image_group_id"));
            entity.setActive((Boolean) rs.getObject("active"));
            return entity;
        }
    }

    @Override
    public void updateStatusOfImagesByImageGroupId(Guid imageGroupId, ImageStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_group_id", imageGroupId)
                .addValue("status", status);
        getCallsHandler().executeModification("UpdateStatusOfImagesByImageGroupId", parameterSource);
    }
}
