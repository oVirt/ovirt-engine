package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.springframework.jdbc.core.RowMapper;

public abstract class AbstractBaseDiskRowMapper<T extends BaseDisk> implements RowMapper<T> {

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T disk = createDiskEntity();

        disk.setId(BaseDao.getGuidDefaultEmpty(rs, "disk_id"));
        disk.setDiskAlias(rs.getString("disk_alias"));
        disk.setDiskDescription(rs.getString("disk_description"));

        disk.setWipeAfterDelete(rs.getBoolean("wipe_after_delete"));
        String propagateErrors = rs.getString("propagate_errors");
        if (!StringUtils.isEmpty(propagateErrors)) {
            disk.setPropagateErrors(PropagateErrors.valueOf(propagateErrors));
        }

        disk.setShareable(rs.getBoolean("shareable"));
        disk.setSgio(ScsiGenericIO.forValue(rs.getInt("sgio")));
        disk.setContentType(DiskContentType.forValue(rs.getInt("disk_content_type")));

        return disk;
    }

    /**
     * @return The disk entity that is being initialized.
     */
    protected abstract T createDiskEntity();
}
