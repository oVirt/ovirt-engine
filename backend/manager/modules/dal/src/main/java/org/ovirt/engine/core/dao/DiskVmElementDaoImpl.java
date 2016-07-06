package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class DiskVmElementDaoImpl extends DefaultGenericDao<DiskVmElement, VmDeviceId> implements DiskVmElementDao {

    public DiskVmElementDaoImpl() {
        super("DiskVmElement");
    }

    @Override protected MapSqlParameterSource createFullParametersMapper(DiskVmElement entity) {
        return createIdParameterMapper(entity.getId()).addValue("is_boot", entity.isBoot())
                .addValue("disk_interface", EnumUtils.nameOrNull(entity.getDiskInterface()));
    }

    @Override protected MapSqlParameterSource createIdParameterMapper(VmDeviceId id) {
        return getCustomMapSqlParameterSource()
                .addValue("disk_id", id.getDeviceId())
                .addValue("vm_id", id.getVmId());
    }

    @Override protected RowMapper<DiskVmElement> createEntityRowMapper() {
        return DiskVmElementRowMapper.INSTANCE;
    }

    private static class DiskVmElementRowMapper implements RowMapper<DiskVmElement> {
        public static DiskVmElementRowMapper INSTANCE = new DiskVmElementRowMapper();

        private DiskVmElementRowMapper() {
        }

        @Override
        public DiskVmElement mapRow(ResultSet rs, int rowNum) throws SQLException {
            DiskVmElement dve = new DiskVmElement();

            dve.setId(new VmDeviceId(getGuidDefaultEmpty(rs, "disk_id"), getGuidDefaultEmpty(rs, "vm_id")));
            dve.setBoot(rs.getBoolean("is_boot"));
            String diskInterfaceName = rs.getString("disk_interface");
            if (!StringUtils.isEmpty(diskInterfaceName)) {
                dve.setDiskInterface(DiskInterface.valueOf(diskInterfaceName));
            }
            dve.setPlugged(rs.getBoolean("is_plugged"));
            return dve;
        }
    }

    public List<DiskVmElement> getAllForVm(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_id", vmId);
        return getCallsHandler().executeReadList("GetDiskVmElementsForVm", DiskVmElementRowMapper.INSTANCE, parameterSource);
    }

    public List<DiskVmElement> getAllPluggedToVm(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_id", vmId);
        return getCallsHandler().executeReadList("GetDiskVmElementsPluggedToVm", DiskVmElementRowMapper.INSTANCE, parameterSource);
    }

}
