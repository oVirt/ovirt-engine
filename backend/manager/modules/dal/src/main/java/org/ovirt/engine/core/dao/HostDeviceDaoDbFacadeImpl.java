package org.ovirt.engine.core.dao;

import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.HostDeviceId;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
                .addValue("net_iface_name", entity.getNetworkInterfaceName())
                .addValue("vm_id", entity.getVmId());
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
    public HostDevice getHostDeviceByHostIdAndDeviceName(Guid hostId, String deviceName) {
        return get(new HostDeviceId(hostId, deviceName));
    }

    private static class HostDeviceRowMapper implements RowMapper<HostDevice> {

        public static final HostDeviceRowMapper instance = new HostDeviceRowMapper();

        @Override
        public HostDevice mapRow(ResultSet rs, int rowNum) throws SQLException {
            HostDevice device = new HostDevice();

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

            return device;
        }
    }
}
