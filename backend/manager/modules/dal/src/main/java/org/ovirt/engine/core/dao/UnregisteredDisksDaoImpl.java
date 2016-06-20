package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.utils.GuidUtils;
import org.springframework.jdbc.core.RowMapper;

@Named
@Singleton
public class UnregisteredDisksDaoImpl extends BaseDao implements UnregisteredDisksDao {

    @Override
    public List<UnregisteredDisk> getByDiskIdAndStorageDomainId(Guid diskId, Guid storageDomainId) {
        List<UnregisteredDisk> unregisteredDisks =
                getCallsHandler().executeReadList("GetDiskByDiskIdAndStorageDomainId",
                        UnregisteredDiskRowMapper.instance,
                        getCustomMapSqlParameterSource()
                                .addValue("disk_id", diskId)
                                .addValue("storage_domain_id", storageDomainId));
        for (UnregisteredDisk unregDisk : unregisteredDisks) {
            List<VmBase> vms = getCallsHandler().executeReadList("GetEntitiesByDiskId",
                    VmsForUnregisteredDiskRowMapper.instance,
                    getCustomMapSqlParameterSource().addValue("disk_id", unregDisk.getId()));
            unregDisk.getVms().addAll(vms);
        }
        return unregisteredDisks;
    }

    @Override
    public void removeUnregisteredDisk(Guid diskId, Guid storageDomainId) {
        getCallsHandler().executeModification("RemoveDiskFromUnregistered", getCustomMapSqlParameterSource()
                .addValue("disk_id", diskId)
                .addValue("storage_domain_id", storageDomainId));
    }

    @Override
    public void removeUnregisteredDiskRelatedToVM(Guid vmId, Guid storageDomainId) {
        getCallsHandler().executeModification("RemoveDiskFromUnregisteredRelatedToVM", getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("storage_domain_id", storageDomainId));
    }

    @Override
    public void saveUnregisteredDisk(UnregisteredDisk disk) {
        // OVF data is not included since it is being updated in the stored procedure.
        getCallsHandler().executeModification("InsertUnregisteredDisk",
                getCustomMapSqlParameterSource()
                        .addValue("disk_id", disk.getDiskImage().getId())
                        .addValue("image_id", disk.getDiskImage().getImageId())
                        .addValue("disk_alias", disk.getDiskImage().getDiskAlias())
                        .addValue("disk_description", disk.getDiskImage().getDiskDescription())
                        .addValue("creation_date", disk.getDiskImage().getCreationDate())
                        .addValue("last_modified", disk.getDiskImage().getLastModified())
                        .addValue("volume_type", disk.getDiskImage().getVolumeType())
                        .addValue("volume_format", disk.getDiskImage().getVolumeFormat())
                        .addValue("actual_size", disk.getDiskImage().getActualSize())
                        .addValue("size", disk.getDiskImage().getSize())
                        .addValue("storage_domain_id", disk.getDiskImage().getStorageIds().get(0)));

        for (VmBase vmBase : disk.getVms()) {
            getCallsHandler().executeModification("InsertUnregisteredDisksToVms",
                    getCustomMapSqlParameterSource()
                            .addValue("disk_id", disk.getDiskImage().getId())
                            .addValue("entity_id", vmBase.getId())
                            .addValue("entity_name", vmBase.getName())
                            .addValue("storage_domain_id", disk.getDiskImage().getStorageIds().get(0)));
        }
    }

    private static class UnregisteredDiskRowMapper implements RowMapper<UnregisteredDisk> {
        public static final UnregisteredDiskRowMapper instance = new UnregisteredDiskRowMapper();

        @Override
        public UnregisteredDisk mapRow(ResultSet rs, int rowNum) throws SQLException {
            UnregisteredDisk entity = new UnregisteredDisk();
            DiskImage diskImage = new DiskImage();
            diskImage.setId(getGuid(rs, "disk_id"));
            diskImage.setImageId(getGuid(rs, "image_id"));
            diskImage.setDiskAlias(rs.getString("disk_alias"));
            diskImage.setDiskDescription(rs.getString("disk_description"));
            diskImage.setActualSizeInBytes(rs.getLong("actual_size"));
            diskImage.setSize(rs.getLong("size"));
            diskImage.setCreationDate(DbFacadeUtils.fromDate(rs.getTimestamp("creation_date")));
            diskImage.setLastModified(DbFacadeUtils.fromDate(rs.getTimestamp("last_modified")));
            diskImage.setStorageIds(GuidUtils.getGuidListFromString(rs.getString("storage_domain_id")));
            diskImage.setVolumeType(VolumeType.forValue(rs.getInt("volume_type")));
            diskImage.setVolumeFormat(VolumeFormat.forValue(rs.getInt("volume_format")));
            entity.setDiskImage(diskImage);
            return entity;
        }
    }

    private static class VmsForUnregisteredDiskRowMapper implements RowMapper<VmBase> {
        public static final VmsForUnregisteredDiskRowMapper instance = new VmsForUnregisteredDiskRowMapper();

        @Override
        public VmBase mapRow(ResultSet rs, int rowNum) throws SQLException {
            VmBase vmBase = new VmBase();
            vmBase.setId(getGuidDefaultEmpty(rs, "entity_id"));
            vmBase.setName(rs.getString("entity_name"));
            return vmBase;
        }
    }
}
