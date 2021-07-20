package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.VmCheckpointState;
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
public class VmCheckpointDaoImpl extends DefaultGenericDao<VmCheckpoint, Guid> implements VmCheckpointDao {

    VmCheckpointDaoImpl() {
        super("VmCheckpoint");
        setProcedureNameForGet("GetVmCheckpointByVmCheckpointId");
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmCheckpoint entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("vm_id", entity.getVmId())
                .addValue("parent_id", entity.getParentId())
                .addValue("_create_date", entity.getCreationDate())
                .addValue("state", entity.getState().getName())
                .addValue("description", entity.getDescription());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid uuid) {
        return getCustomMapSqlParameterSource().addValue("checkpoint_id", uuid);
    }
    @Override
    protected RowMapper<VmCheckpoint> createEntityRowMapper() {
        return vmCheckpointRowMapper;
    }

    private static final RowMapper<VmCheckpoint> vmCheckpointRowMapper = (rs, rowNum) -> {
        VmCheckpoint entity = new VmCheckpoint();
        entity.setId(getGuid(rs, "checkpoint_id"));
        entity.setParentId(getGuid(rs, "parent_id"));
        entity.setVmId(getGuid(rs, "vm_id"));
        entity.setCreationDate(DbFacadeUtils.fromDate(rs.getTimestamp("_create_date")));
        entity.setState(VmCheckpointState.forName(rs.getString("state")));
        entity.setDescription(rs.getString("description"));
        return entity;
    };

    @Override
    public void update(VmCheckpoint entity) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("checkpoint_id", entity.getId())
                .addValue("vm_id", entity.getVmId())
                .addValue("parent_id", entity.getParentId())
                .addValue("state", entity.getState().getName())
                .addValue("description", entity.getDescription());
        getCallsHandler().executeModification("UpdateVmCheckpoint", parameterSource);
    }

    @Override
    public List<VmCheckpoint> getAllForVm(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_id", id);
        return getCallsHandler().executeReadList("GetVmCheckpointsByVmId", vmCheckpointRowMapper, parameterSource);
    }

    @Override
    public VmCheckpoint getChildCheckpoint(Guid checkpointId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("checkpoint_id", checkpointId);
        return getCallsHandler()
                .executeRead("GetVmCheckpointByVmCheckpointParentId", vmCheckpointRowMapper, parameterSource);
    }

    @Override
    public void addDiskToCheckpoint(Guid backupId, Guid diskId) {
        getCallsHandler().executeModification("InsertVmCheckpointDiskMap",
                getCustomMapSqlParameterSource()
                        .addValue("checkpoint_id", backupId)
                        .addValue("disk_id", diskId));
    }

    @Override
    public List<DiskImage> getDisksByCheckpointId(Guid checkpointId) {
        return getCallsHandler().executeReadList("GetDisksByVmCheckpointId",
                DiskImageDaoImpl.DiskImageRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("checkpoint_id", checkpointId));
    }

    @Override
    public void removeAllCheckpointsByVmId(Guid vmId) {
        getCallsHandler().executeModification("DeleteAllCheckpointsByVmId",
                getCustomMapSqlParameterSource().addValue("vm_id", vmId));
    }

    @Override
    public void invalidateAllCheckpointsByVmId(Guid vmId) {
        getCallsHandler().executeModification("InvalidateAllCheckpointsByVmId",
                getCustomMapSqlParameterSource()
                        .addValue("vm_id", vmId)
                        .addValue("state", VmCheckpointState.INVALID.getName()));
    }

    @Override
    public boolean isDiskIncludedInCheckpoint(Guid diskId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("disk_id", diskId);
        return getCallsHandler().executeRead("IsDiskIncludedInCheckpoint",
                SingleColumnRowMapper.newInstance(Boolean.class),
                parameterSource);
    }
}
