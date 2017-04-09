package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.Collections;
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

    @Override
    protected MapSqlParameterSource createFullParametersMapper(DiskVmElement entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("is_boot", entity.isBoot())
                .addValue("pass_discard", entity.isPassDiscard())
                .addValue("disk_interface", EnumUtils.nameOrNull(entity.getDiskInterface()))
                .addValue("is_using_scsi_reservation", entity.isUsingScsiReservation());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(VmDeviceId id) {
        return getCustomMapSqlParameterSource()
                .addValue("disk_id", id.getDeviceId())
                .addValue("vm_id", id.getVmId());
    }

    @Override
    protected RowMapper<DiskVmElement> createEntityRowMapper() {
        return diskVmElementRowMapper;
    }

    private static final RowMapper<DiskVmElement> diskVmElementRowMapper = (rs, rowNum) -> {
        DiskVmElement dve = new DiskVmElement();

        dve.setId(new VmDeviceId(getGuidDefaultEmpty(rs, "disk_id"), getGuidDefaultEmpty(rs, "vm_id")));
        dve.setBoot(rs.getBoolean("is_boot"));
        dve.setPassDiscard(rs.getBoolean("pass_discard"));
        String diskInterfaceName = rs.getString("disk_interface");
        if (!StringUtils.isEmpty(diskInterfaceName)) {
            dve.setDiskInterface(DiskInterface.valueOf(diskInterfaceName));
        }
        dve.setPlugged(rs.getBoolean("is_plugged"));
        dve.setLogicalName(rs.getString("logical_name"));
        dve.setReadOnly(rs.getBoolean("is_readonly"));
        dve.setUsingScsiReservation(rs.getBoolean("is_using_scsi_reservation"));
        return dve;
    };

    @Override
    public DiskVmElement get(VmDeviceId id) {
        return get(id, null, false);
    }

    @Override
    public DiskVmElement get(VmDeviceId id, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("GetDiskVmElementByDiskVmElementId",
                diskVmElementRowMapper,
                createIdParameterMapper(id)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public List<DiskVmElement> getAllDiskVmElementsByDiskId(Guid diskId) {
        return getAllDiskVmElementsByDisksIds(Collections.singleton(diskId));
    }

    @Override
    public List<DiskVmElement> getAllDiskVmElementsByDisksIds(Collection<Guid> disksIds) {
        return getCallsHandler().executeReadList("GetDiskVmElementsByDiskVmElementsIds", diskVmElementRowMapper,
                getCustomMapSqlParameterSource().addValue("disks_ids", createArrayOfUUIDs(disksIds)));
    }

    public List<DiskVmElement> getAllForVm(Guid vmId) {
        return getAllForVm(vmId, null, false);
    }

    public List<DiskVmElement> getAllForVm(Guid vmId, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("user_id", userID)
                .addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetDiskVmElementsForVm",
                diskVmElementRowMapper,
                parameterSource);
    }

    public List<DiskVmElement> getAllPluggedToVm(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_id", vmId);
        return getCallsHandler().executeReadList("GetDiskVmElementsPluggedToVm",
                diskVmElementRowMapper,
                parameterSource);
    }
}
