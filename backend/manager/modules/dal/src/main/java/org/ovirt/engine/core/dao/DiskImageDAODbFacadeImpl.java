package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.image_vm_map_id;
import org.ovirt.engine.core.common.businessentities.image_vm_pool_map;
import org.ovirt.engine.core.common.businessentities.stateless_vm_image_map;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * <code>DiskImageDAODbFacadeImpl</code> provides an implementation of {@link DiskImageDAO} that uses previously
 * developed code from {@link DbFacade}.
 *
 *
 */
public class DiskImageDAODbFacadeImpl extends BaseDAODbFacade implements DiskImageDAO {

    @Override
    public DiskImage get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_guid", id);

        ParameterizedRowMapper<DiskImage> mapper = new ParameterizedRowMapper<DiskImage>() {
            @Override
            public DiskImage mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                DiskImage entity = new DiskImage();
                entity.setactive((Boolean) rs.getObject("active"));
                entity.setvm_guid(Guid.createGuidFromString(rs
                        .getString("vm_guid")));
                entity.setcreation_date(DbFacadeUtils.fromDate(rs
                        .getTimestamp("creation_date")));
                entity.setactual_size(rs.getLong("actual_size"));
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs
                        .getString("image_guid")));
                entity.setinternal_drive_mapping(rs
                        .getString("internal_drive_mapping"));
                entity.setit_guid(Guid.createGuidFromString(rs
                        .getString("it_guid")));
                entity.setsize(rs.getLong("size"));
                entity.setParentId(Guid.createGuidFromString(rs
                        .getString("ParentId")));
                entity.setimageStatus(ImageStatus.forValue(rs
                        .getInt("imageStatus")));
                entity.setlastModified(DbFacadeUtils.fromDate(rs
                        .getTimestamp("lastModified")));
                entity.setappList(rs.getString("app_list"));
                entity.setstorage_id(NGuid.createGuidFromString(rs
                        .getString("storage_id")));
                entity.setvm_snapshot_id(NGuid.createGuidFromString(rs
                        .getString("vm_snapshot_id")));
                entity.setvolume_type(VolumeType.forValue(rs
                        .getInt("volume_type")));
                entity.setvolume_format(VolumeFormat.forValue(rs
                        .getInt("volume_format")));
                entity.setdisk_type(DiskType.forValue(rs.getInt("disk_type")));
                entity.setimage_group_id(Guid.createGuidFromString(rs
                        .getString("image_group_id")));
                entity.setstorage_path(rs.getString("storage_path"));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setdisk_interface(DiskInterface.forValue(rs
                        .getInt("disk_interface")));
                entity.setboot(rs.getBoolean("boot"));
                entity.setwipe_after_delete(rs.getBoolean("wipe_after_delete"));
                entity.setpropagate_errors(PropagateErrors.forValue(rs
                        .getInt("propagate_errors")));
                entity.setread_rate(rs.getInt("read_rate"));
                entity.setwrite_rate(rs.getInt("write_rate"));
                return entity;
            }
        };

        return getCallsHandler().executeRead("GetImageByImageGuid", mapper, parameterSource);
    }

    @Override
    public DiskImage getSnapshotById(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_guid", id);

        ParameterizedRowMapper<DiskImage> mapper = new ParameterizedRowMapper<DiskImage>() {
            @Override
            public DiskImage mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                DiskImage entity = new DiskImage();
                entity.setcreation_date(DbFacadeUtils.fromDate(rs
                        .getTimestamp("creation_date")));
                entity.setactual_size(rs.getLong("actual_size"));
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs
                        .getString("image_guid")));
                entity.setinternal_drive_mapping(rs
                        .getString("internal_drive_mapping"));
                entity.setit_guid(Guid.createGuidFromString(rs
                        .getString("it_guid")));
                entity.setsize(rs.getLong("size"));
                entity.setParentId(Guid.createGuidFromString(rs
                        .getString("ParentId")));
                entity.setimageStatus(ImageStatus.forValue(rs
                        .getInt("imageStatus")));
                entity.setlastModified(DbFacadeUtils.fromDate(rs
                        .getTimestamp("lastModified")));
                entity.setappList(rs.getString("app_list"));
                entity.setstorage_id(NGuid.createGuidFromString(rs
                        .getString("storage_id")));
                entity.setvm_snapshot_id(NGuid.createGuidFromString(rs
                        .getString("vm_snapshot_id")));
                entity.setvolume_type(VolumeType.forValue(rs
                        .getInt("volume_type")));
                entity.setvolume_format(VolumeFormat.forValue(rs
                        .getInt("volume_format")));
                entity.setdisk_type(DiskType.forValue(rs.getInt("disk_type")));
                entity.setimage_group_id(Guid.createGuidFromString(rs
                        .getString("image_group_id")));
                entity.setstorage_path(rs.getString("storage_path"));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setdisk_interface(DiskInterface.forValue(rs
                        .getInt("disk_interface")));
                entity.setboot(rs.getBoolean("boot"));
                entity.setwipe_after_delete(rs.getBoolean("wipe_after_delete"));
                entity.setpropagate_errors(PropagateErrors.forValue(rs
                        .getInt("propagate_errors")));
                entity.setread_rate(rs.getInt("read_rate"));
                entity.setwrite_rate(rs.getInt("write_rate"));
                return entity;
            }
        };

        return getCallsHandler().executeRead("GetSnapshotByGuid", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DiskImage> getAllForVm(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", id);

        ParameterizedRowMapper<DiskImage> mapper = new ParameterizedRowMapper<DiskImage>() {
            @Override
            public DiskImage mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                DiskImage entity = new DiskImage();
                entity.setactive((Boolean) rs.getObject("active"));
                entity.setcreation_date(DbFacadeUtils.fromDate(rs
                        .getTimestamp("creation_date")));
                entity.setactual_size(rs.getLong("actual_size"));
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs
                        .getString("image_guid")));
                entity.setinternal_drive_mapping(rs
                        .getString("internal_drive_mapping"));
                entity.setit_guid(Guid.createGuidFromString(rs
                        .getString("it_guid")));
                entity.setsize(rs.getLong("size"));
                entity.setvm_guid(Guid.createGuidFromString(rs
                        .getString("vm_guid")));
                entity.setParentId(Guid.createGuidFromString(rs
                        .getString("ParentId")));
                entity.setimageStatus(ImageStatus.forValue(rs
                        .getInt("imageStatus")));
                entity.setlastModified(DbFacadeUtils.fromDate(rs
                        .getTimestamp("lastModified")));
                entity.setappList(rs.getString("app_list"));
                entity.setstorage_id(NGuid.createGuidFromString(rs
                        .getString("storage_id")));
                entity.setvm_snapshot_id(NGuid.createGuidFromString(rs
                        .getString("vm_snapshot_id")));
                entity.setimage_group_id(Guid.createGuidFromString(rs
                        .getString("image_group_id")));
                entity.setvolume_type(VolumeType.forValue(rs
                        .getInt("volume_type")));
                entity.setvolume_format(VolumeFormat.forValue(rs
                        .getInt("volume_format")));
                entity.setdisk_type(DiskType.forValue(rs.getInt("disk_type")));
                entity.setstorage_path(rs.getString("storage_path"));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setdisk_interface(DiskInterface.forValue(rs
                        .getInt("disk_interface")));
                entity.setboot(rs.getBoolean("boot"));
                entity.setwipe_after_delete(rs.getBoolean("wipe_after_delete"));
                entity.setpropagate_errors(PropagateErrors.forValue(rs
                        .getInt("propagate_errors")));
                entity.setread_rate(rs.getInt("read_rate"));
                entity.setwrite_rate(rs.getInt("write_rate"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetImagesByVmGuid", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DiskImage> getAllSnapshotsForParent(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("parent_guid", id);

        ParameterizedRowMapper<DiskImage> mapper = new ParameterizedRowMapper<DiskImage>() {
            @Override
            public DiskImage mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                DiskImage entity = new DiskImage();
                entity.setcreation_date(DbFacadeUtils.fromDate(rs
                        .getTimestamp("creation_date")));
                entity.setactual_size(rs.getLong("actual_size"));
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs
                        .getString("image_guid")));
                entity.setinternal_drive_mapping(rs
                        .getString("internal_drive_mapping"));
                entity.setit_guid(Guid.createGuidFromString(rs
                        .getString("it_guid")));
                entity.setsize(rs.getLong("size"));
                entity.setParentId(Guid.createGuidFromString(rs
                        .getString("ParentId")));
                entity.setimageStatus(ImageStatus.forValue(rs
                        .getInt("imageStatus")));
                entity.setlastModified(DbFacadeUtils.fromDate(rs
                        .getTimestamp("lastModified")));
                entity.setappList(rs.getString("app_list"));
                entity.setstorage_id(NGuid.createGuidFromString(rs
                        .getString("storage_id")));
                entity.setvm_snapshot_id(NGuid.createGuidFromString(rs
                        .getString("vm_snapshot_id")));
                entity.setvolume_type(VolumeType.forValue(rs
                        .getInt("volume_type")));
                entity.setvolume_format(VolumeFormat.forValue(rs
                        .getInt("volume_format")));
                entity.setdisk_type(DiskType.forValue(rs.getInt("disk_type")));
                entity.setimage_group_id(Guid.createGuidFromString(rs
                        .getString("image_group_id")));
                entity.setstorage_path(rs.getString("storage_path"));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setdisk_interface(DiskInterface.forValue(rs
                        .getInt("disk_interface")));
                entity.setboot(rs.getBoolean("boot"));
                entity.setwipe_after_delete(rs.getBoolean("wipe_after_delete"));
                entity.setpropagate_errors(PropagateErrors.forValue(rs
                        .getInt("propagate_errors")));
                entity.setread_rate(rs.getInt("read_rate"));
                entity.setwrite_rate(rs.getInt("write_rate"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetSnapshotByParentGuid", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DiskImage> getAllSnapshotsForStorageDomain(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_domain_id", id);

        ParameterizedRowMapper<DiskImage> mapper = new ParameterizedRowMapper<DiskImage>() {
            @Override
            public DiskImage mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                DiskImage entity = new DiskImage();
                entity.setcreation_date(DbFacadeUtils.fromDate(rs
                        .getTimestamp("creation_date")));
                entity.setactual_size(rs.getLong("actual_size"));
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs
                        .getString("image_guid")));
                entity.setinternal_drive_mapping(rs
                        .getString("internal_drive_mapping"));
                entity.setit_guid(Guid.createGuidFromString(rs
                        .getString("it_guid")));
                entity.setsize(rs.getLong("size"));
                entity.setParentId(Guid.createGuidFromString(rs
                        .getString("ParentId")));
                entity.setimageStatus(ImageStatus.forValue(rs
                        .getInt("imageStatus")));
                entity.setlastModified(DbFacadeUtils.fromDate(rs
                        .getTimestamp("lastModified")));
                entity.setappList(rs.getString("app_list"));
                entity.setstorage_id(NGuid.createGuidFromString(rs
                        .getString("storage_id")));
                entity.setvm_snapshot_id(NGuid.createGuidFromString(rs
                        .getString("vm_snapshot_id")));
                entity.setvolume_type(VolumeType.forValue(rs
                        .getInt("volume_type")));
                entity.setvolume_format(VolumeFormat.forValue(rs
                        .getInt("volume_format")));
                entity.setdisk_type(DiskType.forValue(rs.getInt("disk_type")));
                entity.setimage_group_id(Guid.createGuidFromString(rs
                        .getString("image_group_id")));
                entity.setstorage_path(rs.getString("storage_path"));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setdisk_interface(DiskInterface.forValue(rs
                        .getInt("disk_interface")));
                entity.setboot(rs.getBoolean("boot"));
                entity.setwipe_after_delete(rs.getBoolean("wipe_after_delete"));
                entity.setpropagate_errors(PropagateErrors.forValue(rs
                        .getInt("propagate_errors")));
                entity.setread_rate(rs.getInt("read_rate"));
                entity.setwrite_rate(rs.getInt("write_rate"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetSnapshotsByStorageDomainId", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DiskImage> getAllSnapshotsForVmSnapshot(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_snapshot_id", id);

        ParameterizedRowMapper<DiskImage> mapper = new ParameterizedRowMapper<DiskImage>() {
            @Override
            public DiskImage mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                DiskImage entity = new DiskImage();
                entity.setcreation_date(DbFacadeUtils.fromDate(rs
                        .getTimestamp("creation_date")));
                entity.setactual_size(rs.getLong("actual_size"));
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs
                        .getString("image_guid")));
                entity.setinternal_drive_mapping(rs
                        .getString("internal_drive_mapping"));
                entity.setit_guid(Guid.createGuidFromString(rs
                        .getString("it_guid")));
                entity.setsize(rs.getLong("size"));
                entity.setParentId(Guid.createGuidFromString(rs
                        .getString("ParentId")));
                entity.setimageStatus(ImageStatus.forValue(rs
                        .getInt("imageStatus")));
                entity.setlastModified(DbFacadeUtils.fromDate(rs
                        .getTimestamp("lastModified")));
                entity.setappList(rs.getString("app_list"));
                entity.setstorage_id(NGuid.createGuidFromString(rs
                        .getString("storage_id")));
                entity.setvm_snapshot_id(NGuid.createGuidFromString(rs
                        .getString("vm_snapshot_id")));
                entity.setvolume_type(VolumeType.forValue(rs
                        .getInt("volume_type")));
                entity.setvolume_format(VolumeFormat.forValue(rs
                        .getInt("volume_format")));
                entity.setdisk_type(DiskType.forValue(rs.getInt("disk_type")));
                entity.setimage_group_id(Guid.createGuidFromString(rs
                        .getString("image_group_id")));
                entity.setstorage_path(rs.getString("storage_path"));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setdisk_interface(DiskInterface.forValue(rs
                        .getInt("disk_interface")));
                entity.setboot(rs.getBoolean("boot"));
                entity.setwipe_after_delete(rs.getBoolean("wipe_after_delete"));
                entity.setpropagate_errors(PropagateErrors.forValue(rs
                        .getInt("propagate_errors")));
                entity.setread_rate(rs.getInt("read_rate"));
                entity.setwrite_rate(rs.getInt("write_rate"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetSnapshotsByVmSnapshotId", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DiskImage> getAllSnapshotsForImageGroup(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_group_id", id);

        ParameterizedRowMapper<DiskImage> mapper = new ParameterizedRowMapper<DiskImage>() {
            @Override
            public DiskImage mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                DiskImage entity = new DiskImage();
                entity.setcreation_date(DbFacadeUtils.fromDate(rs
                        .getTimestamp("creation_date")));
                entity.setactual_size(rs.getLong("actual_size"));
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs
                        .getString("image_guid")));
                entity.setinternal_drive_mapping(rs
                        .getString("internal_drive_mapping"));
                entity.setit_guid(Guid.createGuidFromString(rs
                        .getString("it_guid")));
                entity.setsize(rs.getLong("size"));
                entity.setParentId(Guid.createGuidFromString(rs
                        .getString("ParentId")));
                entity.setimageStatus(ImageStatus.forValue(rs
                        .getInt("imageStatus")));
                entity.setlastModified(DbFacadeUtils.fromDate(rs
                        .getTimestamp("lastModified")));
                entity.setappList(rs.getString("app_list"));
                entity.setstorage_id(NGuid.createGuidFromString(rs
                        .getString("storage_id")));
                entity.setvm_snapshot_id(NGuid.createGuidFromString(rs
                        .getString("vm_snapshot_id")));
                entity.setvolume_type(VolumeType.forValue(rs
                        .getInt("volume_type")));
                entity.setvolume_format(VolumeFormat.forValue(rs
                        .getInt("volume_format")));
                entity.setdisk_type(DiskType.forValue(rs.getInt("disk_type")));
                entity.setimage_group_id(Guid.createGuidFromString(rs
                        .getString("image_group_id")));
                entity.setstorage_path(rs.getString("storage_path"));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setdisk_interface(DiskInterface.forValue(rs
                        .getInt("disk_interface")));
                entity.setboot(rs.getBoolean("boot"));
                entity.setwipe_after_delete(rs.getBoolean("wipe_after_delete"));
                entity.setpropagate_errors(PropagateErrors.forValue(rs
                        .getInt("propagate_errors")));
                entity.setread_rate(rs.getInt("read_rate"));
                entity.setwrite_rate(rs.getInt("write_rate"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetSnapshotsByImageGroupId", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<DiskImage> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<DiskImage> mapper = new ParameterizedRowMapper<DiskImage>() {
            @Override
            public DiskImage mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                DiskImage entity = new DiskImage();
                entity.setcreation_date(DbFacadeUtils.fromDate(rs
                        .getTimestamp("creation_date")));
                entity.setactual_size(rs.getLong("actual_size"));
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs
                        .getString("image_guid")));
                entity.setinternal_drive_mapping(rs
                        .getString("internal_drive_mapping"));
                entity.setit_guid(Guid.createGuidFromString(rs
                        .getString("it_guid")));
                entity.setsize(rs.getLong("size"));
                entity.setParentId(Guid.createGuidFromString(rs
                        .getString("ParentId")));
                entity.setimageStatus(ImageStatus.forValue(rs
                        .getInt("imageStatus")));
                entity.setlastModified(DbFacadeUtils.fromDate(rs
                        .getTimestamp("lastModified")));
                entity.setappList(rs.getString("app_list"));
                entity.setstorage_id(NGuid.createGuidFromString(rs
                        .getString("storage_id")));
                entity.setvm_snapshot_id(NGuid.createGuidFromString(rs
                        .getString("vm_snapshot_id")));
                entity.setvolume_type(VolumeType.forValue(rs
                        .getInt("volume_type")));
                entity.setvolume_format(VolumeFormat.forValue(rs
                        .getInt("volume_format")));
                entity.setdisk_type(DiskType.forValue(rs.getInt("disk_type")));
                entity.setimage_group_id(Guid.createGuidFromString(rs
                        .getString("image_group_id")));
                entity.setstorage_path(rs.getString("storage_path"));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setdisk_interface(DiskInterface.forValue(rs
                        .getInt("disk_interface")));
                entity.setboot(rs.getBoolean("boot"));
                entity.setwipe_after_delete(rs.getBoolean("wipe_after_delete"));
                entity.setpropagate_errors(PropagateErrors.forValue(rs
                        .getInt("propagate_errors")));
                entity.setread_rate(rs.getInt("read_rate"));
                entity.setwrite_rate(rs.getInt("write_rate"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAllFromImages", mapper, parameterSource);
    }

    @Override
    public void save(DiskImage image) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("creation_date", image.getcreation_date())
                .addValue("description", image.getdescription())
                .addValue("image_guid", image.getId())
                .addValue("internal_drive_mapping",
                        image.getinternal_drive_mapping())
                .addValue("it_guid", image.getit_guid())
                .addValue("size", image.getsize())
                .addValue("ParentId", image.getParentId())
                .addValue("imageStatus", image.getimageStatus())
                .addValue("lastModified", image.getlastModified())
                .addValue("app_list", image.getappList())
                .addValue("storage_id", image.getstorage_id())
                .addValue("vm_snapshot_id", image.getvm_snapshot_id())
                .addValue("volume_type", image.getvolume_type())
                .addValue("volume_format", image.getvolume_format())
                .addValue("disk_type", image.getdisk_type())
                .addValue("image_group_id", image.getimage_group_id())
                .addValue("disk_interface", image.getdisk_interface())
                .addValue("boot", image.getboot())
                .addValue("wipe_after_delete", image.getwipe_after_delete())
                .addValue("propagate_errors", image.getpropagate_errors());

        getCallsHandler().executeModification("InsertImage", parameterSource);
    }

    @Override
    public void update(DiskImage image) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("creation_date", image.getcreation_date())
                .addValue("description", image.getdescription())
                .addValue("image_guid", image.getId())
                .addValue("internal_drive_mapping",
                        image.getinternal_drive_mapping())
                .addValue("it_guid", image.getit_guid())
                .addValue("size", image.getsize())
                .addValue("ParentId", image.getParentId())
                .addValue("imageStatus", image.getimageStatus())
                .addValue("lastModified", image.getlastModified())
                .addValue("app_list", image.getappList())
                .addValue("storage_id", image.getstorage_id())
                .addValue("vm_snapshot_id", image.getvm_snapshot_id())
                .addValue("volume_type", image.getvolume_type())
                .addValue("volume_format", image.getvolume_format())
                .addValue("disk_type", image.getdisk_type())
                .addValue("image_group_id", image.getimage_group_id())
                .addValue("disk_interface", image.getdisk_interface())
                .addValue("boot", image.getboot())
                .addValue("wipe_after_delete", image.getwipe_after_delete())
                .addValue("propagate_errors", image.getpropagate_errors());

        getCallsHandler().executeModification("UpdateImage", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_guid", id);

        getCallsHandler().executeModification("DeleteImage", parameterSource);
    }

    @Override
    public void removeAllForVmId(Guid id) {
        List<Guid> imagesList = new ArrayList<Guid>();
        for (DiskImage image : getAllForVm(id)) {
            imagesList.add(image.getId());

            List<DiskImage> imagesForDisk =
                DbFacade.getInstance().getDiskImageDAO().getAllSnapshotsForImageGroup(image.getimage_group_id());
            if (imagesForDisk == null || imagesForDisk.isEmpty()) {
                DbFacade.getInstance().getDiskDao().remove(image.getimage_group_id());
            }
        }
        // TODO this will be fixed when we have ORM and object relationships
        for (Guid guid : imagesList) {
            DbFacade.getInstance().getImageVmMapDAO().remove(new image_vm_map_id(guid, id));
        }
    }

    @Override
    public image_vm_pool_map getImageVmPoolMapByImageId(Guid imageId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("image_guid", imageId);

        ParameterizedRowMapper<image_vm_pool_map> mapper = new ParameterizedRowMapper<image_vm_pool_map>() {
            @Override
            public image_vm_pool_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                image_vm_pool_map entity = new image_vm_pool_map();
                entity.setimage_guid(Guid.createGuidFromString(rs.getString("image_guid")));
                entity.setinternal_drive_mapping(rs.getString("internal_drive_mapping"));
                entity.setvm_guid(Guid.createGuidFromString(rs.getString("vm_guid")));
                return entity;
            }
        };

        return getCallsHandler().executeRead("Getimage_vm_pool_mapByimage_guid", mapper, parameterSource);
    }

    @Override
    public void addImageVmPoolMap(image_vm_pool_map map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("image_guid",
                map.getimage_guid()).addValue("internal_drive_mapping", map.getinternal_drive_mapping()).addValue(
                "vm_guid", map.getvm_guid());

        getCallsHandler().executeModification("Insertimage_vm_pool_map", parameterSource);
    }

    @Override
    public void removeImageVmPoolMap(Guid imageId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("image_guid", imageId);

        getCallsHandler().executeModification("Deleteimage_vm_pool_map", parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<image_vm_pool_map> getImageVmPoolMapByVmId(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_guid", vmId);

        ParameterizedRowMapper<image_vm_pool_map> mapper = new ParameterizedRowMapper<image_vm_pool_map>() {
            @Override
            public image_vm_pool_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                image_vm_pool_map entity = new image_vm_pool_map();
                entity.setimage_guid(Guid.createGuidFromString(rs.getString("image_guid")));
                entity.setinternal_drive_mapping(rs.getString("internal_drive_mapping"));
                entity.setvm_guid(Guid.createGuidFromString(rs.getString("vm_guid")));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("Getimage_vm_pool_mapByvm_guid", mapper, parameterSource);
    }

    @Override
    public stateless_vm_image_map getStatelessVmImageMapForImageId(Guid imageId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("image_guid", imageId);

        ParameterizedRowMapper<stateless_vm_image_map> mapper = new ParameterizedRowMapper<stateless_vm_image_map>() {
            @Override
            public stateless_vm_image_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                stateless_vm_image_map entity = new stateless_vm_image_map();
                entity.setimage_guid(Guid.createGuidFromString(rs.getString("image_guid")));
                entity.setinternal_drive_mapping(rs.getString("internal_drive_mapping"));
                entity.setvm_guid(Guid.createGuidFromString(rs.getString("vm_guid")));
                return entity;
            }
        };

        return getCallsHandler().executeRead("Getstateless_vm_image_mapByimage_guid", mapper, parameterSource);
    }

    @Override
    public void addStatelessVmImageMap(stateless_vm_image_map map) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("image_guid",
                map.getimage_guid()).addValue("internal_drive_mapping", map.getinternal_drive_mapping()).addValue(
                "vm_guid", map.getvm_guid());

        getCallsHandler().executeModification("Insertstateless_vm_image_map", parameterSource);
    }

    @Override
    public void removeStatelessVmImageMap(Guid imageId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("image_guid", imageId);

        getCallsHandler().executeModification("Deletestateless_vm_image_map", parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<stateless_vm_image_map> getAllStatelessVmImageMapsForVm(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_guid", vmId);

        ParameterizedRowMapper<stateless_vm_image_map> mapper = new ParameterizedRowMapper<stateless_vm_image_map>() {
            @Override
            public stateless_vm_image_map mapRow(ResultSet rs, int rowNum) throws SQLException {
                stateless_vm_image_map entity = new stateless_vm_image_map();
                entity.setimage_guid(Guid.createGuidFromString(rs.getString("image_guid")));
                entity.setinternal_drive_mapping(rs.getString("internal_drive_mapping"));
                entity.setvm_guid(Guid.createGuidFromString(rs.getString("vm_guid")));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("Getstateless_vm_image_mapByvm_guid", mapper, parameterSource);
    }

    @Override
    public DiskImage getAncestor(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("image_guid", id);

        ParameterizedRowMapper<DiskImage> mapper = new ParameterizedRowMapper<DiskImage>() {
            @Override
            public DiskImage mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                DiskImage entity = new DiskImage();
                entity.setcreation_date(DbFacadeUtils.fromDate(rs
                        .getTimestamp("creation_date")));
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs
                        .getString("image_guid")));
                entity.setinternal_drive_mapping(rs
                        .getString("internal_drive_mapping"));
                entity.setit_guid(Guid.createGuidFromString(rs
                        .getString("it_guid")));
                entity.setsize(rs.getLong("size"));
                entity.setParentId(Guid.createGuidFromString(rs
                        .getString("ParentId")));
                entity.setimageStatus(ImageStatus.forValue(rs
                        .getInt("imageStatus")));
                entity.setlastModified(DbFacadeUtils.fromDate(rs
                        .getTimestamp("lastModified")));
                entity.setappList(rs.getString("app_list"));
                entity.setstorage_id(NGuid.createGuidFromString(rs
                        .getString("storage_id")));
                entity.setvm_snapshot_id(NGuid.createGuidFromString(rs
                        .getString("vm_snapshot_id")));
                entity.setvolume_type(VolumeType.forValue(rs
                        .getInt("volume_type")));
                entity.setvolume_format(VolumeFormat.forValue(rs
                        .getInt("volume_format")));
                entity.setdisk_type(DiskType.forValue(rs.getInt("disk_type")));
                entity.setimage_group_id(Guid.createGuidFromString(rs
                        .getString("image_group_id")));
                entity.setdisk_interface(DiskInterface.forValue(rs
                        .getInt("disk_interface")));
                entity.setboot(rs.getBoolean("boot"));
                entity.setwipe_after_delete(rs.getBoolean("wipe_after_delete"));
                entity.setpropagate_errors(PropagateErrors.forValue(rs
                        .getInt("propagate_errors")));
                return entity;
            }
        };

        return getCallsHandler().executeRead("GetAncestralImageByImageGuid", mapper, parameterSource);
    }
}
