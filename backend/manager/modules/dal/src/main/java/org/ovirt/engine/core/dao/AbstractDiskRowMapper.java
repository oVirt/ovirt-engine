package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.ovirt.engine.core.common.businessentities.BaseDisk;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * Abstract row mapper that maps the fields of {@link BaseDisk}.
 *
 * @param <T> The type of disk to map for.
 */
abstract class AbstractDiskRowMapper<T extends BaseDisk> implements ParameterizedRowMapper<T> {

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T disk = createDiskEntity();

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

    /**
     * @return The disk entity that is being initialized.
     */
    protected abstract T createDiskEntity();
}
