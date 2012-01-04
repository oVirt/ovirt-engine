package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.DiskType;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class DiskDaoDbFacadeImpl extends DefaultGenericDaoDbFacade<Disk, Guid> implements DiskDao {

    @Override
    protected String getProcedureNameForUpdate() {
        return "UpdateDisk";
    }

    @Override
    protected String getProcedureNameForGet() {
        return "GetDiskByDiskId";
    }

    @Override
    protected String getProcedureNameForGetAll() {
        return "GetAllFromDisks";
    }

    @Override
    protected String getProcedureNameForSave() {
        return "InsertDisk";
    }

    @Override
    protected String getProcedureNameForRemove() {
        return "DeleteDisk";
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("disk_id", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(Disk entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("internal_drive_mapping", entity.getInternalDriveMapping())
                .addValue("disk_type", EnumUtils.nameOrNull(entity.getDiskType()))
                .addValue("disk_interface", EnumUtils.nameOrNull(entity.getDiskInterface()))
                .addValue("wipe_after_delete", entity.isWipeAfterDelete())
                .addValue("propagate_errors", EnumUtils.nameOrNull(entity.getPropagateErrors()));
    }

    @Override
    protected ParameterizedRowMapper<Disk> createEntityRowMapper() {
        return new ParameterizedRowMapper<Disk>() {

            @Override
            public Disk mapRow(ResultSet rs, int rowNum) throws SQLException {
                Disk disk = new Disk();

                disk.setId(Guid.createGuidFromString(rs.getString("disk_id")));
                disk.setInternalDriveMapping(rs.getInt("internal_drive_mapping"));
                disk.setDiskType(DiskType.valueOf(rs.getString("disk_type")));
                disk.setDiskInterface(DiskInterface.valueOf(rs.getString("disk_interface")));
                disk.setWipeAfterDelete(rs.getBoolean("wipe_after_delete"));
                disk.setPropagateErrors(PropagateErrors.valueOf(rs.getString("propagate_errors")));

                return disk;
            }
        };
    }

    @Override
    public boolean exists(Guid id) {
        return get(id) != null;
    }
}
