package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskAlignment;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;

public abstract class AbstractBaseDiskRowMapper<T extends BaseDisk> implements RowMapper<T> {

    @Override
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T disk = createDiskEntity();

        disk.setId(BaseDAODbFacade.getGuidDefaultEmpty(rs, "disk_id"));
        disk.setDiskAlias(rs.getString("disk_alias"));
        disk.setDiskDescription(rs.getString("disk_description"));
        String diskInterface = rs.getString("disk_interface");
        if (!StringUtils.isEmpty(diskInterface)) {
            disk.setDiskInterface(DiskInterface.valueOf(diskInterface));
        }

        disk.setWipeAfterDelete(rs.getBoolean("wipe_after_delete"));
        String propagateErrors = rs.getString("propagate_errors");
        if (!StringUtils.isEmpty(propagateErrors)) {
            disk.setPropagateErrors(PropagateErrors.valueOf(propagateErrors));
        }

        disk.setShareable(rs.getBoolean("shareable"));
        disk.setBoot(rs.getBoolean("boot"));
        disk.setSgio(ScsiGenericIO.forValue(rs.getInt("sgio")));
        disk.setAlignment(DiskAlignment.forValue(rs.getInt("alignment")));
        disk.setLastAlignmentScan(DbFacadeUtils.fromDate(rs.getTimestamp("last_alignment_scan")));

        return disk;
    }

    /**
     * @return The disk entity that is being initialized.
     */
    protected abstract T createDiskEntity();
}
