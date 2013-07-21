package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.comparators.InterfaceComparerByMAC;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAODbFacade;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class VmNetworkInterfaceDaoDbFacadeImpl extends BaseDAODbFacade implements VmNetworkInterfaceDao {

    private static final int MAC_COLUMN_POSITION = 1;

    @SuppressWarnings("unchecked")
    protected final RowMapper<VmNetworkInterface> mapper =
            new RowMapper<VmNetworkInterface>() {
                @Override
                public VmNetworkInterface mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                    VmNetworkInterface entity = new VmNetworkInterface();
                    entity.getStatistics().setId(getGuidDefaultEmpty(rs, "id"));
                    entity.getStatistics().setReceiveRate(rs.getDouble("rx_rate"));
                    entity.getStatistics().setTransmitRate(rs.getDouble("tx_rate"));
                    entity.getStatistics().setReceiveDropRate(rs.getDouble("rx_drop"));
                    entity.getStatistics().setTransmitDropRate(rs.getDouble("tx_drop"));
                    entity.getStatistics().setStatus(InterfaceStatus.forValue(rs.getInt("iface_status")));
                    entity.setType((Integer) rs.getObject("type"));
                    entity.setMacAddress(rs.getString("mac_addr"));
                    entity.setNetworkName(rs.getString("network_name"));
                    entity.setName(rs.getString("name"));
                    entity.setVmId(getGuid(rs, "vm_guid"));
                    entity.setVnicProfileId(getGuid(rs, "vnic_profile_id"));
                    entity.setVmTemplateId(getGuid(rs, "vmt_guid"));
                    entity.setVmName(rs.getString("vm_name"));
                    entity.setId(getGuidDefaultEmpty(rs, "id"));
                    entity.setSpeed((Integer) rs.getObject("speed"));
                    entity.setPlugged(rs.getBoolean("is_plugged"));
                    entity.setCustomProperties(SerializationFactory.getDeserializer()
                            .deserializeOrCreateNew(rs.getString("custom_properties"), LinkedHashMap.class));
                    entity.setPortMirroring(rs.getBoolean("port_mirroring"));
                    entity.setLinked(rs.getBoolean("linked"));
                    return entity;
                }
            };

    protected final RowMapper<String> macMapper = new RowMapper<String>() {

        @Override
        public String mapRow(ResultSet rs, int rowNum) throws SQLException {
            return rs.getString(MAC_COLUMN_POSITION);
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
    public List<VmNetworkInterface> getAll() {
        return getCallsHandler().executeReadList("GetAllFromvm_interface", mapper, getCustomMapSqlParameterSource());
    }

    @Override
    public List<VmNetworkInterface> getAllForNetwork(Guid networkId) {
        return getCallsHandler().executeReadList("GetVmInterfaceViewsByNetworkId",
                mapper, getCustomMapSqlParameterSource().addValue("network_id", networkId));
    }

    @Override
    public List<VmNetworkInterface> getAllForTemplatesByNetwork(Guid networkId) {
        return getCallsHandler().executeReadList("GetVmTemplateInterfaceViewsByNetworkId",
                mapper, getCustomMapSqlParameterSource().addValue("network_id", networkId));
    }

    @Override
    public List<String> getAllMacsByDataCenter(Guid dataCenterId) {
        return getCallsHandler().executeReadList("GetMacsByDataCenterId",
                macMapper, getCustomMapSqlParameterSource().addValue("data_center_id", dataCenterId));
    }

    @Override
    public List<VmNetworkInterface> getPluggedForMac(String macAddress) {
        return getCallsHandler().executeReadList("GetPluggedVmInterfacesByMac",
                mapper, getCustomMapSqlParameterSource().addValue("mac_address", macAddress));
    }
}
