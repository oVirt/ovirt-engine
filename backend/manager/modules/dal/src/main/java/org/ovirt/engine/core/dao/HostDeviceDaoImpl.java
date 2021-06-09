package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.HostDeviceId;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.common.businessentities.MDevType;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmHostDevice;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class HostDeviceDaoImpl extends MassOperationsGenericDao<HostDevice, HostDeviceId> implements HostDeviceDao {

    public HostDeviceDaoImpl() {
        super("HostDevice");
        setProcedureNameForGet("GetHostDeviceByHostIdAndDeviceName");
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(HostDeviceId hostDeviceId) {
        return getCustomMapSqlParameterSource()
                .addValue("host_id", hostDeviceId.getHostId())
                .addValue("device_name", hostDeviceId.getDeviceName());
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(HostDevice entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("parent_device_name", entity.getParentDeviceName())
                .addValue("capability", entity.getCapability())
                .addValue("iommu_group", entity.getIommuGroup())
                .addValue("mdev_types", SerializationFactory.getSerializer().serialize(entity.getMdevTypes()))
                .addValue("product_name", entity.getProductName())
                .addValue("product_id", entity.getProductId())
                .addValue("vendor_name", entity.getVendorName())
                .addValue("vendor_id", entity.getVendorId())
                .addValue("physfn", entity.getParentPhysicalFunction())
                .addValue("total_vfs", entity.getTotalVirtualFunctions())
                .addValue("net_iface_name", entity.getNetworkInterfaceName())
                .addValue("driver", entity.getDriver())
                .addValue("is_assignable", entity.isAssignable())
                .addValue("address", SerializationFactory.getSerializer().serialize(entity.getAddress()))
                .addValue("block_path", entity.getBlockPath())
                .addValue("hostdev_spec_params", SerializationFactory.getSerializer().serialize(entity.getSpecParams()));
    }

    @Override
    protected RowMapper<HostDevice> createEntityRowMapper() {
        return HostDeviceRowMapper.instance;
    }

    @Override
    public List<HostDevice> getHostDevicesByHostId(Guid hostId) {
        return getCallsHandler().executeReadList("GetHostDevicesByHostId",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("host_id", hostId));
    }

    @Override
    public List<HostDevice> getHostDevicesByHostIdAndIommuGroup(Guid hostId, int iommuGroup) {
        return getCallsHandler().executeReadList("GetHostDevicesByHostIdAndIommuGroup",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("host_id", hostId).addValue("iommu_group", iommuGroup));
    }

    @Override
    public HostDevice getHostDeviceByHostIdAndDeviceName(Guid hostId, String deviceName) {
        return get(new HostDeviceId(hostId, deviceName));
    }

    @Override
    public boolean checkVmHostDeviceAvailability(Guid vmId, Guid hostId) {
        return getCallsHandler().executeRead("CheckVmHostDeviceAvailability",
                SingleColumnRowMapper.newInstance(Boolean.class),
                getCustomMapSqlParameterSource().addValue("vm_id", vmId).addValue("host_id", hostId));
    }

    @Override
    public void setVmIdOnHostDevice(HostDeviceId hostDeviceId, Guid vmId) {
        getCallsHandler().executeModification("SetVmIdOnHostDevice",
                createIdParameterMapper(hostDeviceId)
                        .addValue("vm_id", vmId));
    }

    @Override
    public void markHostDevicesUsedByVmId(Guid vmId, Guid hostId) {
        getCallsHandler().executeModification("MarkHostDevicesUsedByVmId",
                getCustomMapSqlParameterSource()
                        .addValue("vm_id", vmId)
                        .addValue("host_id", hostId));
    }

    @Override
    public void freeHostDevicesUsedByVmId(Guid vmId) {
        getCallsHandler().executeModification("FreeHostDevicesUsedByVmId",
                getCustomMapSqlParameterSource().addValue("vm_id", vmId));
    }

    @Override
    public List<HostDeviceView> getVmExtendedHostDevicesByVmId(Guid vmId) {
        return getCallsHandler().executeReadList("GetVmExtendedHostDevicesByVmId",
                ExtendedHostDeviceRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("vm_id", vmId));
    }

    @Override
    public List<VmDevice> getVmDevicesAttachedToHost(Guid hostId) {
        return getCallsHandler().executeReadList("GetVmDevicesAttachedToHost",
                VmDeviceDaoImpl.vmDeviceRowMapper,
                getCustomMapSqlParameterSource().addValue("host_id", hostId));
    }

    @Override
    public List<HostDeviceView> getExtendedHostDevicesByHostId(Guid hostId) {
        return getCallsHandler().executeReadList("GetExtendedHostDevicesByHostId",
                ExtendedHostDeviceRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("host_id", hostId));
    }

    @Override
    public List<HostDeviceView> getUsedScsiDevicesByHostId(Guid hostId) {
        return getCallsHandler().executeReadList("GetUsedScsiDevicesByHostId",
                ExtendedHostDeviceRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("host_id", hostId));
    }

    @Override
    public void cleanDownVms() {
        getCallsHandler().executeModification("CleanDownVms",
                getCustomMapSqlParameterSource());
    }

    private abstract static class BaseHostDeviceRowMapper<T extends HostDevice> implements RowMapper<T> {

        protected void map(ResultSet rs, HostDevice device) throws SQLException{
            device.setHostId(getGuid(rs, "host_id"));
            device.setDeviceName(rs.getString("device_name"));
            device.setParentDeviceName(rs.getString("parent_device_name"));
            device.setCapability(rs.getString("capability"));
            device.setIommuGroup((Integer) rs.getObject("iommu_group"));
            String mdevs = rs.getString("mdev_types");
            if (mdevs != null && !mdevs.isEmpty()) {
                if (mdevs.contains("[")) {
                    // new JSON format
                    device.setMdevTypes(SerializationFactory.getDeserializer()
                            .deserializeOrCreateNew(rs.getString("mdev_types"), ArrayList.class));
                } else {
                    // old comma separated format
                    String[] mdevNames = mdevs.split(",");
                    List<MDevType> mdevTypes = new ArrayList<>();
                    for (String mdevName : mdevNames) {
                        mdevTypes.add(new MDevType(mdevName, null, null, null));
                    }
                    device.setMdevTypes(mdevTypes);
                }
            }

            device.setProductName(rs.getString("product_name"));
            device.setProductId(rs.getString("product_id"));
            device.setVendorName(rs.getString("vendor_name"));
            device.setVendorId(rs.getString("vendor_id"));
            device.setParentPhysicalFunction(rs.getString("physfn"));
            device.setTotalVirtualFunctions((Integer) rs.getObject("total_vfs"));
            device.setNetworkInterfaceName(rs.getString("net_iface_name"));
            device.setAssignable(rs.getBoolean("is_assignable"));
            device.setAddress(SerializationFactory.getDeserializer()
                    .deserializeOrCreateNew(rs.getString("address"), HashMap.class));
            device.setVmId(getGuid(rs, "vm_id"));
            device.setDriver(rs.getString("driver"));
            device.setBlockPath(rs.getString("block_path"));
            device.setSpecParams(SerializationFactory.getDeserializer()
                    .deserializeOrCreateNew(rs.getString("hostdev_spec_params"), HashMap.class));
        }
    }

    private static class HostDeviceRowMapper extends BaseHostDeviceRowMapper<HostDevice> {

        public static final HostDeviceRowMapper instance = new HostDeviceRowMapper();

        @Override
        public HostDevice mapRow(ResultSet rs, int rowNum) throws SQLException {
            HostDevice device = new HostDevice();
            map(rs, device);

            return device;
        }
    }

    private static class ExtendedHostDeviceRowMapper extends BaseHostDeviceRowMapper<HostDeviceView> {

        public static final ExtendedHostDeviceRowMapper instance = new ExtendedHostDeviceRowMapper();

        @Override
        public HostDeviceView mapRow(ResultSet rs, int rowNum) throws SQLException {
            HostDeviceView device = new HostDeviceView();
            map(rs, device);

            device.setConfiguredVmId(getGuid(rs, "configured_vm_id"));
            device.setAttachedVmNames(split(rs.getString("attached_vm_names")));
            device.setRunningVmName(rs.getString("running_vm_name"));

            Map<String, Object> specParams = SerializationFactory.getDeserializer()
                    .deserializeOrCreateNew(rs.getString("spec_params"), HashMap.class);

            device.setIommuPlaceholder(specParams != null && VmHostDevice.isIommuPlaceHolder(specParams));

            return device;
        }
    }
}
