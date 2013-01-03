package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.InterfaceComparerByMAC;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dao.BaseDAODbFacade;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class VmNetworkInterfaceDaoDbFacadeImpl extends BaseDAODbFacade implements VmNetworkInterfaceDao {

    protected final ParameterizedRowMapper<VmNetworkInterface> mapper =
            new ParameterizedRowMapper<VmNetworkInterface>() {
                @Override
                public VmNetworkInterface mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                    VmNetworkInterface entity = new VmNetworkInterface();
                    entity.getStatistics().setId(Guid.createGuidFromString(rs.getString("id")));
                    entity.getStatistics().setReceiveRate(rs.getDouble("rx_rate"));
                    entity.getStatistics().setTransmitRate(rs.getDouble("tx_rate"));
                    entity.getStatistics().setReceiveDropRate(rs.getDouble("rx_drop"));
                    entity.getStatistics().setTransmitDropRate(rs.getDouble("tx_drop"));
                    entity.getStatistics().setStatus(InterfaceStatus.forValue(rs.getInt("iface_status")));
                    entity.setType((Integer) rs.getObject("type"));
                    entity.setMacAddress(rs.getString("mac_addr"));
                    entity.setNetworkName(rs.getString("network_name"));
                    entity.setName(rs.getString("name"));
                    entity.setVmId(NGuid.createGuidFromString(rs.getString("vm_guid")));
                    entity.setVmTemplateId(NGuid.createGuidFromString(rs.getString("vmt_guid")));
                    entity.setVmName(rs.getString("vm_name"));
                    entity.setId(Guid.createGuidFromString(rs.getString("id")));
                    entity.setSpeed((Integer) rs.getObject("speed"));
                    entity.setPlugged(rs.getBoolean("is_plugged"));
                    entity.setPortMirroring(rs.getBoolean("port_mirroring"));
                    entity.setLinked(rs.getBoolean("linked"));
                    return entity;
                }
            };

    @Override
    public VmNetworkInterface get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("id", id);
        return getCallsHandler().executeRead("Getvm_interfaceById", mapper, parameterSource);
    }

    @Override
    public List<VmNetworkInterface> getAllForVm(Guid id) {
        return getAllForVm(id, null, false);
    }

    @Override
    public List<VmNetworkInterface> getAllForVm(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        List<VmNetworkInterface> results =
                getCallsHandler().executeReadList("Getvm_interfaceByvm_id", mapper, parameterSource);
        java.util.Collections.sort(results, new InterfaceComparerByMAC());
        return results;
    }

    @Override
    public List<VmNetworkInterface> getAllForTemplate(Guid id) {
        return getAllForTemplate(id, null, false);
    }

    @Override
    public List<VmNetworkInterface> getAllForTemplate(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("template_id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return getCallsHandler().executeReadList("Getvm_interfaceBytemplate_id", mapper, parameterSource);
    }

    @Override
    public void save(VmNetworkInterface stats) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", stats.getId())
                .addValue("mac_addr", stats.getMacAddress())
                .addValue("name", stats.getName())
                .addValue("network_name", stats.getNetworkName())
                .addValue("speed", stats.getSpeed())
                .addValue("vm_guid", stats.getVmId())
                .addValue("vmt_guid", stats.getVmTemplateId())
                .addValue("type", stats.getType())
                .addValue("port_mirroring", stats.isPortMirroring())
                .addValue("linked", stats.isLinked());

        getCallsHandler().executeModification("Insertvm_interface", parameterSource);
    }

    @Override
    public void update(VmNetworkInterface iface) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", iface.getId())
                .addValue("mac_addr", iface.getMacAddress())
                .addValue("name", iface.getName())
                .addValue("network_name", iface.getNetworkName())
                .addValue("speed", iface.getSpeed())
                .addValue("vm_guid", iface.getVmId())
                .addValue("vmt_guid", iface.getVmTemplateId())
                .addValue("type", iface.getType())
                .addValue("port_mirroring", iface.isPortMirroring())
                .addValue("linked", iface.isLinked());

        getCallsHandler().executeModification("Updatevm_interface", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        getCallsHandler().executeModification("Deletevm_interface", parameterSource);
    }

    @Override
    public List<VmNetworkInterface> getAll() {
        return getCallsHandler().executeReadList("GetAllFromvm_interface", mapper, getCustomMapSqlParameterSource());
    }

    @Override
    public List<VmNetworkInterface> getAllForNetwork(Guid networkId) {
        return getCallsHandler().executeReadList("GetVmInterfacesByNetworkId",
                mapper, getCustomMapSqlParameterSource().addValue("network_id", networkId));
    }

    @Override
    public List<VmNetworkInterface> getAllForTemplatesByNetwork(Guid networkId) {
        return getCallsHandler().executeReadList("GetVmTemplateInterfacesByNetworkId",
                mapper, getCustomMapSqlParameterSource().addValue("network_id", networkId));
    }
}
