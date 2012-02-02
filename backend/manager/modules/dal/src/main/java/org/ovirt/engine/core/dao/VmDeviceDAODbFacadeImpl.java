package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class VmDeviceDAODbFacadeImpl extends
        DefaultGenericDaoDbFacade<VmDevice, VmDeviceId> implements VmDeviceDAO {

    @Override
    protected String getProcedureNameForUpdate() {
        return "UpdateVmDevice";
    }

    @Override
    protected String getProcedureNameForGet() {
        return "GetVmDeviceByDeviceId";
    }

    @Override
    protected String getProcedureNameForGetAll() {
        return "GetAllFromVmDevice";
    }

    @Override
    protected String getProcedureNameForSave() {
        return "InsertVmDevice";
    }

    @Override
    protected String getProcedureNameForRemove() {
        return "DeleteVmDevice";
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(VmDeviceId id) {
        return getCustomMapSqlParameterSource()
                .addValue("device_id", id.getDeviceId())
                .addValue("vm_id", id.getVmId());
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(VmDevice entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("device", entity.getDevice())
                .addValue("type", entity.getType())
                .addValue("address", entity.getAddress())
                .addValue("boot_order", entity.getBootOrder())
                .addValue("spec_params", entity.getSpecParams())
                .addValue("is_managed", entity.getIsManaged())
                .addValue("is_plugged", entity.getIsPlugged())
                .addValue("is_readonly", entity.getIsReadOnly());
    }

    @Override
    protected ParameterizedRowMapper<VmDevice> createEntityRowMapper() {
        return new ParameterizedRowMapper<VmDevice>() {

            @Override
            public VmDevice mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                VmDevice vmDevice = new VmDevice();

                vmDevice.setId(new VmDeviceId(Guid.createGuidFromString(rs.getString("device_id")),
                        Guid.createGuidFromString(rs
                                .getString("vm_id"))));
                vmDevice.setDevice(rs.getString("device"));
                vmDevice.setType(rs.getString("type"));
                vmDevice.setAddress(rs.getString("address"));
                vmDevice.setBootOrder(rs.getInt("boot_order"));
                vmDevice.setSpecParams(rs.getString("spec_params"));
                vmDevice.setIsManaged(rs.getBoolean("is_managed"));
                vmDevice.setIsPlugged(rs.getBoolean("is_plugged"));
                vmDevice.setIsReadOnly(rs.getBoolean("is_readonly"));
                return vmDevice;
            }
        };
    }

    @Override
    public boolean exists(VmDeviceId id) {
        return get(id) != null;
    }

    @Override
    public List<VmDevice> getVmDeviceByVmId(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId);

        return getCallsHandler().executeReadList("GetVmDeviceByVmId",
                createEntityRowMapper(), parameterSource);
    }

    @Override
    public List<VmDevice> getVmDeviceByVmIdAndType(Guid vmId, String type) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("type", type);
        return getCallsHandler().executeReadList("GetVmDeviceByVmIdAndType",
                createEntityRowMapper(), parameterSource);
    }

    @Override
    public List<VmDevice> getVmDeviceByVmIdTypeAndDevice(Guid vmId, String type, String device) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("type", type)
                .addValue("device", device);

        return getCallsHandler().executeReadList("GetVmDeviceByVmIdTypeAndDevice",
                createEntityRowMapper(), parameterSource);
    }
}
