package org.ovirt.engine.core.dao;

import java.util.Date;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.VmBackupPhase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code VmBackupDaoImpl} provides an implementation of {@link VmBackupDao}.
 */
@Named
@Singleton
public class VmBackupDaoImpl extends DefaultGenericDao<VmBackup, Guid> implements VmBackupDao {

    VmBackupDaoImpl() {
        super("VmBackup");
        setProcedureNameForGet("GetVmBackupByVmBackupId");
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmBackup entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("vm_id", entity.getVmId())
                .addValue("host_id", entity.getHostId())
                .addValue("from_checkpoint_id", entity.getFromCheckpointId())
                .addValue("to_checkpoint_id", entity.getToCheckpointId())
                .addValue("phase", entity.getPhase().getName())
                .addValue("_create_date", entity.getCreationDate())
                .addValue("_update_date", entity.getModificationDate())
                .addValue("description", entity.getDescription())
                .addValue("is_live_backup", entity.isLiveBackup());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid uuid) {
        return getCustomMapSqlParameterSource().addValue("backup_id", uuid);
    }

    @Override
    protected RowMapper<VmBackup> createEntityRowMapper() {
        return vmBackupRowMapper;
    }

    private static final RowMapper<VmBackup> vmBackupRowMapper = (rs, rowNum) -> {
        VmBackup entity = new VmBackup();
        entity.setId(getGuid(rs, "backup_id"));
        entity.setVmId(getGuid(rs, "vm_id"));
        entity.setHostId(getGuid(rs, "host_id"));
        entity.setFromCheckpointId(getGuid(rs, "from_checkpoint_id"));
        entity.setToCheckpointId(getGuid(rs, "to_checkpoint_id"));
        entity.setPhase(VmBackupPhase.forName(rs.getString("phase")));
        entity.setCreationDate(DbFacadeUtils.fromDate(rs.getTimestamp("_create_date")));
        entity.setModificationDate(DbFacadeUtils.fromDate(rs.getTimestamp("_update_date")));
        entity.setDescription(rs.getString("description"));
        entity.setLiveBackup(rs.getBoolean("is_live_backup"));
        return entity;
    };

    @Override
    public List<VmBackup> getAllForVm(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_id", id);
        return getCallsHandler().executeReadList("GetVmBackupsByVmId", vmBackupRowMapper, parameterSource);
    }

    @Override
    public void update(VmBackup entity) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("backup_id", entity.getId())
                .addValue("vm_id", entity.getVmId())
                .addValue("host_id", entity.getHostId())
                .addValue("from_checkpoint_id", entity.getFromCheckpointId())
                .addValue("to_checkpoint_id", entity.getToCheckpointId())
                .addValue("phase", entity.getPhase().getName())
                .addValue("_update_date", new Date())
                .addValue("description", entity.getDescription())
                .addValue("is_live_backup", entity.isLiveBackup());
        getCallsHandler()
                .executeModification("UpdateVmBackup", parameterSource);
    }

    @Override
    public void addDiskToVmBackup(Guid backupId, Guid diskId) {
        getCallsHandler().executeModification("InsertVmBackupDiskMap",
                getCustomMapSqlParameterSource()
                        .addValue("backup_id", backupId)
                        .addValue("disk_id", diskId));
    }

    @Override
    public void addBackupUrlToVmBackup(Guid backupId, Guid diskId, String backupUrl) {
        getCallsHandler().executeModification("UpdateVmBackupDiskMap",
                getCustomMapSqlParameterSource()
                        .addValue("backup_id", backupId)
                        .addValue("disk_id", diskId)
                        .addValue("backup_url", backupUrl));
    }

    @Override
    public String getBackupUrlForDisk(Guid backupId, Guid diskId) {
        return getCallsHandler().executeRead("GetBackupUrlForDiskId",
                SingleColumnRowMapper.newInstance(String.class),
                getCustomMapSqlParameterSource()
                        .addValue("backup_id", backupId)
                        .addValue("disk_id", diskId));
    }

    @Override
    public List<DiskImage> getDisksByBackupId(Guid backupId) {
        return getCallsHandler().executeReadList("GetDisksByVmBackupId",
                DiskImageDaoImpl.DiskImageRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("backup_id", backupId));
    }

    @Override
    public void deleteCompletedBackups(Date succeededBackups, Date failedBackups) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("succeeded_end_time", succeededBackups)
                .addValue("failed_end_time", failedBackups);
        getCallsHandler().executeModification("DeleteCompletedBackupsOlderThanDate", parameterSource);
    }
}
