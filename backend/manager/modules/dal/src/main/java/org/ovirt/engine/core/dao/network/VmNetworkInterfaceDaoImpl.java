package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultReadDao;
import org.ovirt.engine.core.dao.network.VmNetworkStatisticsDaoImpl.VmNetworkStatisticsRowMapper;
import org.ovirt.engine.core.dao.network.VmNicDaoImpl.VmNicRowMapperBase;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class VmNetworkInterfaceDaoImpl extends DefaultReadDao<VmNetworkInterface, Guid> implements VmNetworkInterfaceDao {

    public VmNetworkInterfaceDaoImpl() {
        super("VmNetworkInterfaceView");
    }

    @Override
    public List<VmNetworkInterface> getAllForVm(Guid id) {
        return getAllForVm(id, null, false);
    }

    @Override
    public List<VmNetworkInterface> getAllForVm(Guid id, Guid userId, boolean filtered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", id).addValue("user_id", userId).addValue("is_filtered", filtered);

        List<VmNetworkInterface> results =
                getCallsHandler().executeReadList("GetVmNetworkInterfaceViewByVmId",
                        VmNetworkInterfaceRowMapper.INSTANCE,
                        parameterSource);
        Collections.sort(results, Comparator.comparing(VmNetworkInterface::getMacAddress));
        return results;
    }

    @Override
    public List<VmNetworkInterface> getAllForMonitoredVm(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId);

        return getCallsHandler().executeReadList("GetVmNetworkInterfaceToMonitorByVmId",
                VmNetworkInterfaceMonitoringRowMapper.INSTANCE,
                parameterSource);
    }

    @Override
    public List<Guid> getAllWithVnicOutOfSync(Set<Guid> vmIds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("ids", createArrayOfUUIDs(vmIds));
        return getCallsHandler().executeReadList("GetVmIdsForVnicsOutOfSync", createGuidMapper(), parameterSource);
    }

    @Override
    public List<VmNetworkInterface> getAllForTemplate(Guid id) {
        return getAllForTemplate(id, null, false);
    }

    @Override
    public List<VmNetworkInterface> getAllForTemplate(Guid id, Guid userId, boolean filtered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("template_id", id).addValue("user_id", userId).addValue("is_filtered", filtered);

        return getCallsHandler().executeReadList("GetVmNetworkInterfaceViewByTemplateId",
                VmNetworkInterfaceRowMapper.INSTANCE,
                parameterSource);
    }

    @Override
    public List<VmNetworkInterface> getAllForNetwork(Guid networkId) {
        return getCallsHandler().executeReadList("GetVmInterfaceViewsByNetworkId",
                VmNetworkInterfaceRowMapper.INSTANCE,
                getCustomMapSqlParameterSource().addValue("network_id", networkId));
    }

    @Override
    public List<VmNetworkInterface> getAllForTemplatesByNetwork(Guid networkId) {
        return getCallsHandler().executeReadList("GetVmTemplateInterfaceViewsByNetworkId",
                VmNetworkInterfaceRowMapper.INSTANCE,
                getCustomMapSqlParameterSource().addValue("network_id", networkId));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<VmNetworkInterface> createEntityRowMapper() {
        return VmNetworkInterfaceRowMapper.INSTANCE;
    }

    private static class VmNetworkInterfaceRowMapper extends VmNicRowMapperBase<VmNetworkInterface> {

        public static VmNetworkInterfaceRowMapper INSTANCE = new VmNetworkInterfaceRowMapper();

        @Override
        public VmNetworkInterface mapRow(ResultSet rs, int rowNum) throws SQLException {
            VmNetworkInterface entity = super.mapRow(rs, rowNum);
            entity.setStatistics(VmNetworkStatisticsRowMapper.INSTANCE.mapRow(rs, rowNum));
            entity.setNetworkName(rs.getString("network_name"));
            entity.setVmName(rs.getString("vm_name"));
            entity.setVnicProfileName(rs.getString("vnic_profile_name"));
            entity.setPlugged(rs.getBoolean("is_plugged"));
            entity.setPortMirroring(rs.getBoolean("port_mirroring"));
            entity.setQosName(rs.getString("qos_name"));
            entity.setFailoverVnicProfileName(rs.getString("failover_vnic_profile_name"));
            return entity;
        }

        @Override
        protected VmNetworkInterface createVmNicEntity() {
            return new VmNetworkInterface();
        }

    }

    private static class VmNetworkInterfaceMonitoringRowMapper implements RowMapper<VmNetworkInterface> {

        public static VmNetworkInterfaceMonitoringRowMapper INSTANCE = new VmNetworkInterfaceMonitoringRowMapper();

        @Override
        public VmNetworkInterface mapRow(ResultSet rs, int rowNum) throws SQLException {
            VmNetworkInterface entity = new VmNetworkInterface();
            entity.setId(getGuidDefaultEmpty(rs, "id"));
            entity.setVmId(getGuid(rs, "vm_guid"));
            entity.setMacAddress(rs.getString("mac_addr"));
            entity.setVnicProfileId(getGuid(rs, "vnic_profile_id"));
            entity.setSpeed(rs.getInt("speed"));
            entity.setStatistics(VmNetworkStatisticsRowMapper.INSTANCE.mapRow(rs, rowNum));
            return entity;
        }

    }

}
