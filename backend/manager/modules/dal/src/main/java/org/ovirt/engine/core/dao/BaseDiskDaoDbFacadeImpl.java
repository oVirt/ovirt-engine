package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ovirt.engine.core.common.businessentities.BaseDisk;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class BaseDiskDaoDbFacadeImpl extends DefaultGenericDaoDbFacade<BaseDisk, Guid> implements BaseDiskDao {

    public BaseDiskDaoDbFacadeImpl() {
        super("BaseDisk");
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("disk_id", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(BaseDisk entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("internal_drive_mapping", entity.getInternalDriveMapping())
                .addValue("disk_alias", entity.getDiskAlias())
                .addValue("disk_description", entity.getDiskDescription())
                .addValue("disk_interface", EnumUtils.nameOrNull(entity.getDiskInterface()))
                .addValue("wipe_after_delete", entity.isWipeAfterDelete())
                .addValue("propagate_errors", EnumUtils.nameOrNull(entity.getPropagateErrors()))
                .addValue("shareable", entity.isShareable());
    }

    @Override
    protected ParameterizedRowMapper<BaseDisk> createEntityRowMapper() {
        return new ParameterizedRowMapper<BaseDisk>() {

            @Override
            public BaseDisk mapRow(ResultSet rs, int rowNum) throws SQLException {
                BaseDisk disk = new BaseDisk();

                disk.setId(Guid.createGuidFromString(rs.getString("disk_id")));
                disk.setInternalDriveMapping(rs.getInt("internal_drive_mapping"));
                disk.setDiskAlias(rs.getString("disk_alias"));
                disk.setDiskDescription(rs.getString("disk_description"));
                disk.setDiskInterface(DiskInterface.valueOf(rs.getString("disk_interface")));
                disk.setWipeAfterDelete(rs.getBoolean("wipe_after_delete"));
                disk.setPropagateErrors(PropagateErrors.valueOf(rs.getString("propagate_errors")));
                disk.setShareable(rs.getBoolean("shareable"));

                return disk;
            }
        };
    }

    @Override
    public boolean exists(Guid id) {
        return get(id) != null;
    }
}
