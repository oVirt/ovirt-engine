package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.VdsNetworkStatistics;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * <code>InterfaceDAODbFacadeImpl</code> provides an implementation of {@link InterfaceDAO}.
 *
 *
 */
public class InterfaceDAODbFacadeImpl extends BaseDAODbFacade implements InterfaceDAO {

    @Override
    public void saveStatisticsForVds(VdsNetworkStatistics stats) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", stats.getId())
                .addValue("rx_drop", stats.getReceiveDropRate())
                .addValue("rx_rate", stats.getReceiveRate())
                .addValue("tx_drop", stats.getTransmitDropRate())
                .addValue("tx_rate", stats.getTransmitRate())
                .addValue("iface_status", stats.getStatus())
                .addValue("vds_id", stats.getVdsId());
        getCallsHandler().executeModification("Insertvds_interface_statistics", parameterSource);
    }

    @Override
    public void saveInterfaceForVds(VdsNetworkInterface stats) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("addr", stats.getAddress())
                .addValue("bond_name", stats.getBondName())
                .addValue("bond_type", stats.getBondType())
                .addValue("gateway", stats.getGateway())
                .addValue("id", stats.getId())
                .addValue("is_bond", stats.getBonded())
                .addValue("bond_opts", stats.getBondOptions())
                .addValue("mac_addr", stats.getMacAddress())
                .addValue("name", stats.getName())
                .addValue("network_name", stats.getNetworkName())
                .addValue("speed", stats.getSpeed())
                .addValue("subnet", stats.getSubnet())
                .addValue("boot_protocol", stats.getBootProtocol())
                .addValue("type", stats.getType())
                .addValue("vds_id", stats.getVdsId())
                .addValue("vlan_id", stats.getVlanId())
                .addValue("mtu", stats.getMtu());

        getCallsHandler().executeModification("Insertvds_interface", parameterSource);
    }

    @Override
    public void updateStatisticsForVds(VdsNetworkStatistics stats) {
        update(stats);
    }

    @Override
    public void massUpdateStatisticsForVds(Collection<VdsNetworkStatistics> statistics) {
        for (VdsNetworkStatistics stats : statistics) {
            update(stats);
        }
    }

    /**
     * Update the {@link VdsNetworkStatistics} in the DB using the given {@link SimpleJdbcCall}.
     *
     * @param callToUpdate
     *            The call to use.
     * @param statistics
     *            The host's network statistics data.
     */
    private void update(VdsNetworkStatistics stats) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", stats.getId())
                .addValue("rx_drop", stats.getReceiveDropRate())
                .addValue("rx_rate", stats.getReceiveRate())
                .addValue("tx_drop", stats.getTransmitDropRate())
                .addValue("tx_rate", stats.getTransmitRate())
                .addValue("iface_status", stats.getStatus())
                .addValue("vds_id", stats.getVdsId());

        getCallsHandler().executeModification("Updatevds_interface_statistics", parameterSource);
    }

    @Override
    public void updateInterfaceForVds(VdsNetworkInterface stats) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("addr", stats.getAddress())
                .addValue("bond_name", stats.getBondName())
                .addValue("bond_type", stats.getBondType())
                .addValue("gateway", stats.getGateway())
                .addValue("id", stats.getId())
                .addValue("is_bond", stats.getBonded())
                .addValue("bond_opts", stats.getBondOptions())
                .addValue("mac_addr", stats.getMacAddress())
                .addValue("name", stats.getName())
                .addValue("network_name", stats.getNetworkName())
                .addValue("speed", stats.getSpeed())
                .addValue("subnet", stats.getSubnet())
                .addValue("boot_protocol", stats.getBootProtocol())
                .addValue("type", stats.getType())
                .addValue("vds_id", stats.getVdsId())
                .addValue("vlan_id", stats.getVlanId())
                .addValue("mtu", stats.getMtu());

        getCallsHandler().executeModification("Updatevds_interface", parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<VdsNetworkInterface> getAllInterfacesForVds(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id);

        ParameterizedRowMapper<VdsNetworkInterface> mapper = new ParameterizedRowMapper<VdsNetworkInterface>() {
            @Override
            public VdsNetworkInterface mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                VdsNetworkInterface entity = new VdsNetworkInterface();
                entity.getStatistics().setId(Guid.createGuidFromString(rs.getString("id")));
                entity.getStatistics().setReceiveRate(rs.getDouble("rx_rate"));
                entity.getStatistics().setTransmitRate(rs.getDouble("tx_rate"));
                entity.getStatistics().setReceiveDropRate(rs.getDouble("rx_drop"));
                entity.getStatistics().setTransmitDropRate(rs.getDouble("tx_drop"));
                entity.getStatistics().setStatus(InterfaceStatus.forValue(rs.getInt("iface_status")));
                entity.getStatistics().setVdsId(Guid.createGuidFromString(rs.getString("vds_id")));
                entity.setType((Integer) rs.getObject("type"));
                entity.setGateway(rs.getString("gateway"));
                entity.setSubnet(rs.getString("subnet"));
                entity.setAddress(rs.getString("addr"));
                entity.setSpeed((Integer) rs.getObject("speed"));
                entity.setVlanId((Integer) rs.getObject("vlan_id"));
                entity.setBondType((Integer) rs.getObject("bond_type"));
                entity.setBondName(rs.getString("bond_name"));
                entity.setBonded((Boolean) rs.getObject("is_bond"));
                entity.setBondOptions(rs.getString("bond_opts"));
                entity.setMacAddress(rs.getString("mac_addr"));
                entity.setNetworkName(rs.getString("network_name"));
                entity.setName(rs.getString("name"));
                entity.setVdsId(NGuid.createGuidFromString(rs.getString("vds_id")));
                entity.setVdsName(rs.getString("vds_name"));
                entity.setId(Guid.createGuidFromString(rs.getString("id")));
                entity.setBootProtocol(NetworkBootProtocol.forValue(rs.getInt("boot_protocol")));
                entity.setMtu(rs.getInt("mtu"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("Getinterface_viewByvds_id", mapper, parameterSource);
    }

    @Override
    public void removeStatisticsForVds(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        getCallsHandler().executeModification("Deletevds_interface_statistics", parameterSource);
    }

    @Override
    public void removeInterfaceFromVds(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        getCallsHandler().executeModification("Deletevds_interface", parameterSource);
    }
}
