package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.HostDeviceId;
import org.ovirt.engine.core.common.businessentities.HostDeviceView;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class HostDeviceDaoDbFacadeImpl extends MassOperationsGenericDaoDbFacade<HostDevice, HostDeviceId> implements HostDeviceDao {

    public HostDeviceDaoDbFacadeImpl() {
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
                .addValue("product_name", entity.getProductName())
                .addValue("product_id", entity.getProductId())
                .addValue("vendor_name", entity.getVendorName())
                .addValue("vendor_id", entity.getVendorId())
                .addValue("physfn", entity.getParentPhysicalFunction())
                .addValue("total_vfs", entity.getTotalVirtualFunctions())
                .addValue("net_iface_name", entity.getNetworkInterfaceName());
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
                createBooleanMapper(),
                getCustomMapSqlParameterSource().addValue("vm_id", vmId).addValue("host_id", hostId));
    }

    @Override
    public void setVmIdOnHostDevice(HostDeviceId hostDeviceId, Guid vmId) {
        getCallsHandler().executeModification("SetVmIdOnHostDevice",
                createIdParameterMapper(hostDeviceId)
                        .addValue("vm_id", vmId));
    }

    @Override
    public void markHostDevicesUsedByVmId(Guid vmId) {
        getCallsHandler().executeModification("MarkHostDevicesUsedByVmId",
                getCustomMapSqlParameterSource().addValue("vm_id", vmId));
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
    public List<HostDeviceView> getExtendedHostDevicesByHostId(Guid hostId) {
        return getCallsHandler().executeReadList("GetExtendedHostDevicesByHostId",
                ExtendedHostDeviceRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("host_id", hostId));
    }

    @Override
    public void cleanDownVms() {
        getCallsHandler().executeModification("CleanDownVms",
                getCustomMapSqlParameterSource());
    }

    private static abstract class BaseHostDeviceRowMapper<T extends HostDevice> implements RowMapper<T> {

        protected void map(ResultSet rs, HostDevice device) throws SQLException{
            device.setHostId(getGuid(rs, "host_id"));
            device.setDeviceName(rs.getString("device_name"));
            device.setParentDeviceName(rs.getString("parent_device_name"));
            device.setCapability(rs.getString("capability"));
            device.setIommuGroup((Integer) rs.getObject("iommu_group"));
            device.setProductName(rs.getString("product_name"));
            device.setProductId(rs.getString("product_id"));
            device.setVendorName(rs.getString("vendor_name"));
            device.setVendorId(rs.getString("vendor_id"));
            device.setParentPhysicalFunction(rs.getString("physfn"));
            device.setTotalVirtualFunctions((Integer) rs.getObject("total_vfs"));
            device.setNetworkInterfaceName(rs.getString("net_iface_name"));
            device.setVmId(getGuid(rs, "vm_id"));

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

            return device;
        }
    }
}
