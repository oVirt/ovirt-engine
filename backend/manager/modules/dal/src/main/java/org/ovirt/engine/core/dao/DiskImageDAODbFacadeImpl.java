package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.utils.GuidUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>DiskImageDAODbFacadeImpl</code> provides an implementation of {@link DiskImageDAO} that uses previously
 * developed code from {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 */
public class DiskImageDAODbFacadeImpl extends BaseDAODbFacade implements DiskImageDAO {

    @Override
    public DiskImage get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_guid", id);

        return getCallsHandler().executeRead("GetImageByImageGuid", DiskImageRowMapper.instance, parameterSource);
    }

    @Override
    public DiskImage getSnapshotById(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_guid", id);

        return getCallsHandler().executeRead("GetSnapshotByGuid", DiskImageRowMapper.instance, parameterSource);
    }

    @Override
    public List<DiskImage> getAllSnapshotsForParent(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("parent_guid", id);
        return getCallsHandler().executeReadList("GetSnapshotByParentGuid",
                DiskImageRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<DiskImage> getAllSnapshotsForStorageDomain(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_domain_id", id);

        return getCallsHandler().executeReadList("GetSnapshotsByStorageDomainId",
                DiskImageRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<DiskImage> getAllSnapshotsForVmSnapshot(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_snapshot_id", id);

        return getCallsHandler().executeReadList("GetSnapshotsByVmSnapshotId",
                DiskImageRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<DiskImage> getAllSnapshotsForImageGroup(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_group_id", id);

        return getCallsHandler().executeReadList("GetSnapshotsByImageGroupId",
                DiskImageRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<DiskImage> getAll() {
        throw new NotImplementedException();
    }

    @Override
    public DiskImage getAncestor(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_guid", id);

        return getCallsHandler().executeRead("GetAncestralImageByImageGuid",
                DiskImageRowMapper.instance,
                parameterSource);
    }

    public List<DiskImage> getImagesWithNoDisk(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId);

        return getCallsHandler().executeReadList("GetImagesWhichHaveNoDisk",
                DiskImageRowMapper.instance,
                parameterSource);
    }

    protected static class DiskImageRowMapper extends AbstractDiskRowMapper<DiskImage> {

        public static final DiskImageRowMapper instance = new DiskImageRowMapper();

        private DiskImageRowMapper() {
        }

        @Override
        public DiskImage mapRow(ResultSet rs, int rowNum) throws SQLException {
            DiskImage entity = super.mapRow(rs, rowNum);
            mapEntity(rs, entity);
            return entity;
        }

        protected void mapEntity(ResultSet rs, DiskImage entity) throws SQLException {
            entity.setCreationDate(DbFacadeUtils.fromDate(rs
                    .getTimestamp("creation_date")));
            entity.setActualSizeInBytes(rs.getLong("actual_size"));
            entity.setDescription(rs.getString("description"));
            entity.setImageId(Guid.createGuidFromString(rs
                    .getString("image_guid")));
            entity.setImageTemplateId(Guid.createGuidFromString(rs
                    .getString("it_guid")));
            entity.setSize(rs.getLong("size"));
            entity.setParentId(Guid.createGuidFromString(rs
                    .getString("ParentId")));
            entity.setImageStatus(ImageStatus.forValue(rs
                    .getInt("imageStatus")));
            entity.setLastModified(DbFacadeUtils.fromDate(rs
                    .getTimestamp("lastModified")));
            entity.setAppList(rs.getString("app_list"));
            entity.setStorageIds(GuidUtils.getGuidListFromString(rs.getString("storage_id")));
            entity.setStoragesNames(split(rs.getString("storage_name")));
            entity.setVmSnapshotId(NGuid.createGuidFromString(rs
                    .getString("vm_snapshot_id")));
            entity.setVolumeType(VolumeType.forValue(rs
                    .getInt("volume_type")));
            entity.setvolumeFormat(VolumeFormat.forValue(rs
                    .getInt("volume_format")));
            entity.setId(Guid.createGuidFromString(rs.getString("image_group_id")));
            entity.setStoragePath(split(rs.getString("storage_path")));
            entity.setStoragePoolId(NGuid.createGuidFromString(rs
                    .getString("storage_pool_id")));
            entity.setBoot(rs.getBoolean("boot"));
            entity.setReadRate(rs.getInt("read_rate"));
            entity.setWriteRate(rs.getInt("write_rate"));
            entity.setReadLatency(rs.getObject("read_latency_seconds") != null ? rs.getDouble("read_latency_seconds")
                    : null);
            entity.setWriteLatency(rs.getObject("write_latency_seconds") != null ? rs.getDouble("write_latency_seconds")
                    : null);
            entity.setFlushLatency(rs.getObject("flush_latency_seconds") != null ? rs.getDouble("flush_latency_seconds")
                    : null);
            entity.setQuotaId(Guid.createGuidFromString(rs.getString("quota_id")));
            entity.setActive(Boolean.TRUE.equals(rs.getObject("active")));
            entity.setQuotaName(rs.getString("quota_name"));
            entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
        }

        @Override
        protected DiskImage createDiskEntity() {
            return new DiskImage();
        }

        private static final String SEPARATOR = ",";

        private ArrayList<String> split(String str) {
            if (StringUtils.isEmpty(str)) {
                return null;
            }

            return new ArrayList<String>(Arrays.asList(str.split(SEPARATOR)));
        }
    }
}
