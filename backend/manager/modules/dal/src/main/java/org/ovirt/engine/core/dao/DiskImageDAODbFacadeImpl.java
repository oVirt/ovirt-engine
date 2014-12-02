package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Named;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.utils.GuidUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>DiskImageDAODbFacadeImpl</code> provides an implementation of {@link DiskImageDAO} that uses previously
 * developed code from {@link org.ovirt.engine.core.dal.dbbroker.DbFacade}.
 */
@Named
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
    public List<DiskImage> getAllSnapshotsForLeaf(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_guid", id);
        return getCallsHandler().executeReadList("GetSnapshotByLeafGuid",
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
    public DiskImage getDiskSnapshotForVmSnapshot(Guid diskId, Guid vmSnapshotId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_snapshot_id", vmSnapshotId)
                .addValue("image_group_id", diskId);

        return getCallsHandler().executeRead("GetDiskSnapshotForVmSnapshot",
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
    public List<DiskImage> getAttachedDiskSnapshotsToVm(Guid vmId, Boolean isPlugged) {
        return getCallsHandler().executeReadList("GetAttachedDiskSnapshotsToVm", DiskImageRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("vm_guid", vmId).addValue("is_plugged", isPlugged));
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

    @Override
    public List<DiskImage> getImagesWithNoDisk(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId);

        return getCallsHandler().executeReadList("GetImagesWhichHaveNoDisk",
                DiskImageRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<DiskImage> getAllForStorageDomain(Guid storageDomainId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_domain_id", storageDomainId);

        return getCallsHandler().executeReadList("GetAllForStorageDomain",
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
            entity.setImageId(getGuidDefaultEmpty(rs, "image_guid"));
            entity.setImageTemplateId(getGuidDefaultEmpty(rs, "it_guid"));
            entity.setSize(rs.getLong("size"));
            entity.setParentId(getGuidDefaultEmpty(rs, "ParentId"));
            entity.setImageStatus(ImageStatus.forValue(rs
                    .getInt("imageStatus")));
            entity.setLastModified(DbFacadeUtils.fromDate(rs
                    .getTimestamp("lastModified")));
            entity.setAppList(rs.getString("app_list"));
            entity.setStorageIds(GuidUtils.getGuidListFromString(rs.getString("storage_id")));
            entity.setStorageTypes(getStorageTypesList(rs.getString("storage_type")));
            entity.setStoragesNames(split(rs.getString("storage_name")));
            entity.setVmSnapshotId(getGuid(rs, "vm_snapshot_id"));
            entity.setVolumeType(VolumeType.forValue(rs
                    .getInt("volume_type")));
            entity.setvolumeFormat(VolumeFormat.forValue(rs
                    .getInt("volume_format")));
            entity.setId(getGuidDefaultEmpty(rs, "image_group_id"));
            entity.setStoragePath(split(rs.getString("storage_path")));
            entity.setStoragePoolId(getGuid(rs, "storage_pool_id"));
            entity.setBoot(rs.getBoolean("boot"));
            entity.setReadRate(rs.getInt("read_rate"));
            entity.setWriteRate(rs.getInt("write_rate"));
            entity.setOvfStore(rs.getBoolean("ovf_store"));
            entity.setReadLatency(rs.getObject("read_latency_seconds") != null ? rs.getDouble("read_latency_seconds")
                    : null);
            entity.setWriteLatency(rs.getObject("write_latency_seconds") != null ? rs.getDouble("write_latency_seconds")
                    : null);
            entity.setFlushLatency(rs.getObject("flush_latency_seconds") != null ? rs.getDouble("flush_latency_seconds")
                    : null);
            entity.setActive(Boolean.TRUE.equals(rs.getObject("active")));
            entity.setQuotaIds(getGuidListFromStringPreserveAllTokens(rs.getString("quota_id")));
            entity.setQuotaNames(splitPreserveAllTokens(rs.getString("quota_name")));
            entity.setQuotaEnforcementType(QuotaEnforcementTypeEnum.forValue(rs.getInt("quota_enforcement_type")));
            entity.setDiskProfileIds(getGuidListFromStringPreserveAllTokens(rs.getString("disk_profile_id")));
            entity.setDiskProfileNames(splitPreserveAllTokens(rs.getString("disk_profile_name")));
        }

        @Override
        protected DiskImage createDiskEntity() {
            return new DiskImage();
        }

        private ArrayList<StorageType> getStorageTypesList(String storageTypesString) throws SQLException {
            List<String> splitTypes = split(storageTypesString);
            if (splitTypes == null) {
                return null;
            }

            ArrayList<StorageType> types = new ArrayList<>();
            for (String typeStr : splitTypes) {
                try {
                    types.add(StorageType.forValue(Integer.parseInt(typeStr)));
                }
                catch (NumberFormatException e) {
                    throw new SQLException("Could not parse disk image storage domain type " + typeStr, e);
                }
            }
            return types;
        }

        /**
         * since quota can be null, we need to preserve null in the list
         *
         * @param str
         * @return
         */
        private ArrayList<String> splitPreserveAllTokens(String str) {
            if (StringUtils.isEmpty(str)) {
                return null;
            }

            return new ArrayList<String>(Arrays.asList(StringUtils.splitPreserveAllTokens(str, SEPARATOR)));
        }

        /**
         * since some disk images can contain empty quota, we need to preserve null in the list.
         *
         * @param str
         * @return
         */
        private ArrayList<Guid> getGuidListFromStringPreserveAllTokens(String str) {
            ArrayList<Guid> guidList = new ArrayList<Guid>();
            if (StringUtils.isEmpty(str)) {
                return new ArrayList<Guid>();
            }
            for (String guidString : splitPreserveAllTokens(str)) {
                Guid guidToAdd = null;
                if (!StringUtils.isEmpty(guidString)) {
                    guidToAdd = Guid.createGuidFromString(guidString);
                }
                guidList.add(guidToAdd);
            }
            return guidList;
        }
    }
}
