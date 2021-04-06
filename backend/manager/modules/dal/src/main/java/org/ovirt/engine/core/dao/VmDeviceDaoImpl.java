package org.ovirt.engine.core.dao;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

@Named
@Singleton
public class VmDeviceDaoImpl extends
        MassOperationsGenericDao<VmDevice, VmDeviceId> implements VmDeviceDao {

    public VmDeviceDaoImpl() {
        super("VmDevice");
        setProcedureNameForGet("GetVmDeviceByDeviceId");
        setProcedureNameForGetAll("GetAllFromVmDevice");
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
                .addValue("type", entity.getType().getValue())
                .addValue("address", entity.getAddress())
                .addValue("spec_params", SerializationFactory.getSerializer().serialize(entity.getSpecParams()))
                .addValue("is_managed", entity.isManaged())
                .addValue("is_plugged", entity.isPlugged())
                .addValue("is_readonly", entity.getReadOnly())
                .addValue("alias", entity.getAlias())
                .addValue("custom_properties",
                        SerializationFactory.getSerializer().serialize(entity.getCustomProperties()))
                .addValue("snapshot_id", entity.getSnapshotId())
                .addValue("logical_name", entity.getLogicalName())
                .addValue("host_device", entity.getHostDevice());
    }

    @Override
    protected RowMapper<VmDevice> createEntityRowMapper() {
        return vmDeviceRowMapper;
    }

    @Override
    public boolean exists(VmDeviceId id) {
        return get(id) != null;
    }

    @Override
    public List<VmDevice> getVmDeviceByVmId(Guid vmId) {
        return getVmDeviceByVmId(vmId, null, false);
    }

    @Override
    public List<VmDevice> getVmDeviceByVmId(Guid vmId, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("user_id", userID)
                .addValue("is_filtered", isFiltered);

        return getCallsHandler().executeReadList("GetVmDeviceByVmId",
                createEntityRowMapper(), parameterSource);
    }

    @Override
    public List<VmDevice> getVmDevicesByDeviceId(Guid deviceId, Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("device_id", deviceId)
                .addValue("vm_id", vmId);

        return getCallsHandler().executeReadList("GetVmDeviceByDeviceId",
                createEntityRowMapper(), parameterSource);
    }

    @Override
    public List<VmDevice> getVmDeviceByVmIdAndType(Guid vmId, VmDeviceGeneralType type) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("type", type.getValue());
        return getCallsHandler().executeReadList("GetVmDeviceByVmIdAndType",
                createEntityRowMapper(), parameterSource);
    }

    @Override
    public List<VmDevice> getVmDeviceByVmIdTypeAndDevice(Guid vmBaseId, VmDeviceGeneralType type, String device) {
        return getVmDeviceByVmIdTypeAndDevice(vmBaseId, type, device, null, false);
    }

    @Override
    public List<VmDevice> getVmDeviceByVmIdTypeAndDevice(Guid vmBaseId, VmDeviceGeneralType type, VmDeviceType device) {
        return getVmDeviceByVmIdTypeAndDevice(vmBaseId, type, device.getName());
    }

    @Override
    public List<VmDevice> getVmDeviceByVmIdTypeAndDevice
            (Guid vmId, VmDeviceGeneralType type, String device, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("type", type.getValue())
                .addValue("device", device)
                .addValue("user_id", userID)
                .addValue("is_filtered", isFiltered);

        return getCallsHandler().executeReadList("GetVmDeviceByVmIdTypeAndDevice",
                createEntityRowMapper(), parameterSource);
    }

    @Override
    public List<VmDevice> getVmDeviceByTypeAndDevice
            (List<Guid> vmsIds, VmDeviceGeneralType type, String device, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_ids", createArrayOfUUIDs(vmsIds))
                .addValue("type", type.getValue())
                .addValue("device", device)
                .addValue("user_id", userID)
                .addValue("is_filtered", isFiltered);

        return getCallsHandler().executeReadList("GetVmDeviceByTypeAndDevice",
                createEntityRowMapper(), parameterSource);
    }
    @Override
    public List<VmDevice> getVmDeviceByType(VmDeviceGeneralType type) {
        return getCallsHandler().executeReadList("GetVmDeviceByType",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("type", type.getValue()));
    }

    @Override
    public List<VmDevice> getUnmanagedDevicesByVmId(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId);
        return getCallsHandler().executeReadList("GetVmUnmanagedDevicesByVmId",
                createEntityRowMapper(), parameterSource);
    }

    @Override
    public boolean existsVmDeviceByVmIdAndType(Guid vmId, VmDeviceGeneralType type) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId).addValue("type", type.getValue());
        return getCallsHandler().executeRead("ExistsVmDeviceByVmIdAndType",
                SingleColumnRowMapper.newInstance(Boolean.class), parameterSource);
    }

    @Override
    public boolean isMemBalloonEnabled(Guid vmId) {

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId);

        Map<String, Object> dbResults =
                new SimpleJdbcCall(getJdbcTemplate()).withFunctionName("isMemBalloonEnabled").execute(
                        parameterSource);

        String resultKey = getDialect().getFunctionReturnKey();
        return (Boolean) dbResults.getOrDefault(resultKey, false);

    }

    static final RowMapper<VmDevice> vmDeviceRowMapper = (rs, rowNum) -> {
        VmDevice vmDevice = new VmDevice();

        vmDevice.setId(new VmDeviceId(getGuidDefaultEmpty(rs, "device_id"), getGuidDefaultEmpty(rs, "vm_id")));
        vmDevice.setDevice(rs.getString("device"));
        vmDevice.setType(VmDeviceGeneralType.forValue(rs.getString("type")));
        vmDevice.setAddress(rs.getString("address"));
        vmDevice.setSpecParams(SerializationFactory.getDeserializer()
                .deserializeOrCreateNew(rs.getString("spec_params"), HashMap.class));
        vmDevice.setManaged(rs.getBoolean("is_managed"));

        // note - those columns are being used also in DiskVmRowMapper, therefore any related
        // change should be done there as well.
        vmDevice.setPlugged(rs.getBoolean("is_plugged"));
        vmDevice.setReadOnly(rs.getBoolean("is_readonly"));

        vmDevice.setAlias(rs.getString("alias"));
        vmDevice.setCustomProperties(SerializationFactory.getDeserializer()
                .deserializeOrCreateNew(rs.getString("custom_properties"), LinkedHashMap.class));
        vmDevice.setSnapshotId(getGuid(rs, "snapshot_id"));
        vmDevice.setLogicalName(rs.getString("logical_name"));
        vmDevice.setHostDevice(rs.getString("host_device"));
        return vmDevice;
    };

    @Override
    public void removeAll(List<VmDeviceId> removedDeviceIds) {
        removedDeviceIds.forEach(this::remove);
    }

    @Override
    public void removeVmDevicesByVmIdAndType(Guid vmId, VmDeviceGeneralType type) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId).addValue("type", type.getValue());
        getCallsHandler().executeModification("DeleteVmDevicesByVmIdAndType", parameterSource);
    }

    @Override
    public void saveAll(List<VmDevice> newVmDevices) {
        newVmDevices.forEach(this::save);
    }

    @Override
    public void clearDeviceAddress(Guid deviceId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("device_id", deviceId);

        getCallsHandler().executeModification("clearVmDeviceAddress", parameterSource);
    }

    @Override
    public void clearAllDeviceAddressesByVmId(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId);

        getCallsHandler().executeModification("clearAllDeviceAddressesByVmId", parameterSource);
    }

    @Override
    public void removeAllUnmanagedDevicesByVmId(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId);

        getCallsHandler().executeModification("removeAllUnmanagedDevicesByVmId", parameterSource);
    }

    private MapSqlParameterSource createParameterSourceForUpdate(VmDevice vmDevice) {
        return getCustomMapSqlParameterSource()
                .addValue("vm_id", vmDevice.getVmId())
                .addValue("device_id", vmDevice.getDeviceId());
    }

}
