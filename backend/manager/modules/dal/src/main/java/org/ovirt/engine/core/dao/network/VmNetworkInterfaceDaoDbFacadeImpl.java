package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.comparators.InterfaceComparerByMAC;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultReadDaoDbFacade;
import org.ovirt.engine.core.dao.network.VmNicDaoDbFacadeImpl.VmNicRowMapperBase;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class VmNetworkInterfaceDaoDbFacadeImpl extends DefaultReadDaoDbFacade<VmNetworkInterface, Guid> implements VmNetworkInterfaceDao {

    public VmNetworkInterfaceDaoDbFacadeImpl() {
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
        Collections.sort(results, new InterfaceComparerByMAC());
        return results;
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

        @SuppressWarnings("unchecked")
        @Override
        public VmNetworkInterface mapRow(ResultSet rs, int rowNum) throws SQLException {
            VmNetworkInterface entity = super.mapRow(rs, rowNum);
            entity.getStatistics().setId(getGuidDefaultEmpty(rs, "id"));
            entity.getStatistics().setReceiveRate(rs.getDouble("rx_rate"));
            entity.getStatistics().setTransmitRate(rs.getDouble("tx_rate"));
            entity.getStatistics().setReceiveDropRate(rs.getDouble("rx_drop"));
            entity.getStatistics().setTransmitDropRate(rs.getDouble("tx_drop"));
            entity.getStatistics().setStatus(InterfaceStatus.forValue(rs.getInt("iface_status")));
            entity.setNetworkName(rs.getString("network_name"));
            entity.setVmName(rs.getString("vm_name"));
            entity.setVnicProfileName(rs.getString("vnic_profile_name"));
            entity.setPlugged(rs.getBoolean("is_plugged"));
            entity.setPortMirroring(rs.getBoolean("port_mirroring"));
            entity.setQosName(rs.getString("qos_name"));
            return entity;

        }
        @Override
        protected VmNetworkInterface createVmNicEntity() {
            return new VmNetworkInterface();
        }

    }
}
