package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.InterfaceStatus;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.Vlan;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;
import org.ovirt.engine.core.dao.BaseDAODbFacade;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class InterfaceDaoDbFacadeImpl extends BaseDAODbFacade implements InterfaceDao {

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
                .addValue("mtu", stats.getMtu())
                .addValue("bridged", stats.isBridged());

        getCallsHandler().executeModification("Insertvds_interface", parameterSource);
    }

    @Override
    public void updateStatisticsForVds(VdsNetworkStatistics stats) {
        update(stats);
    }

    @Override
    public void massUpdateStatisticsForVds(Collection<VdsNetworkStatistics> statistics) {
        List<MapSqlParameterSource> executions = new ArrayList<>(statistics.size());
        for (VdsNetworkStatistics stats : statistics) {
            executions.add(getCustomMapSqlParameterSource()
                    .addValue("id", stats.getId())
                    .addValue("rx_drop", stats.getReceiveDropRate())
                    .addValue("rx_rate", stats.getReceiveRate())
                    .addValue("tx_drop", stats.getTransmitDropRate())
                    .addValue("tx_rate", stats.getTransmitRate())
                    .addValue("iface_status", stats.getStatus())
                    .addValue("vds_id", stats.getVdsId()));
        }
        getCallsHandler().executeStoredProcAsBatch("Updatevds_interface_statistics",
                executions);
    }

    @Override
    public void massUpdateInterfacesForVds(List<VdsNetworkInterface> dbIfacesToBatch) {
        updateAllInBatch("Updatevds_interface", dbIfacesToBatch, new MapSqlParameterMapper<VdsNetworkInterface>() {
            @Override
            public MapSqlParameterSource map(VdsNetworkInterface entity) {
                MapSqlParameterSource paramValue = new MapSqlParameterSource().addValue("addr", entity.getAddress())
                        .addValue("bond_name", entity.getBondName())
                        .addValue("bond_type", entity.getBondType())
                        .addValue("gateway", entity.getGateway())
                        .addValue("id", entity.getId())
                        .addValue("is_bond", entity.getBonded())
                        .addValue("bond_opts", entity.getBondOptions())
                        .addValue("mac_addr", entity.getMacAddress())
                        .addValue("name", entity.getName())
                        .addValue("network_name", entity.getNetworkName())
                        .addValue("speed", entity.getSpeed())
                        .addValue("subnet", entity.getSubnet())
                        .addValue("boot_protocol", entity.getBootProtocol())
                        .addValue("type", entity.getType())
                        .addValue("vds_id", entity.getVdsId())
                        .addValue("vlan_id", entity.getVlanId())
                        .addValue("mtu", entity.getMtu())
                        .addValue("bridged", entity.isBridged());
                return paramValue;
            }
        });
    }

    public void updateAllInBatch(String procedureName,
            Collection<VdsNetworkInterface> paramValues,
            MapSqlParameterMapper<VdsNetworkInterface> mapper) {
        getCallsHandler().executeStoredProcAsBatch(procedureName,
                paramValues, mapper);
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
                .addValue("mtu", stats.getMtu())
                .addValue("bridged", stats.isBridged());

        getCallsHandler().executeModification("Updatevds_interface", parameterSource);
    }

    @Override
    public List<VdsNetworkInterface> getAllInterfacesForVds(Guid id) {
        return getAllInterfacesForVds(id, null, false);
    }

    @Override
    public List<VdsNetworkInterface> getAllInterfacesWithIpAddress(Guid clusterId, String ipAddress) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("addr", ipAddress).addValue("cluster_id", clusterId);

        return getCallsHandler().executeReadList("Getinterface_viewByAddr",
                vdsNetworkInterfaceRowMapper, parameterSource);
    }

    @Override
    public List<VdsNetworkInterface> getAllInterfacesForVds(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return getCallsHandler().executeReadList("Getinterface_viewByvds_id",
                vdsNetworkInterfaceRowMapper,
                parameterSource);
    }

    @Override
    public VdsNetworkInterface getManagedInterfaceForVds(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return getCallsHandler().executeRead("GetVdsManagedInterfaceByVdsId",
                vdsNetworkInterfaceRowMapper,
                parameterSource);
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

    @Override
    public List<VdsNetworkInterface> getVdsInterfacesByNetworkId(Guid networkId) {
        return getCallsHandler().executeReadList("GetVdsInterfacesByNetworkId",
                vdsNetworkInterfaceRowMapper,
                getCustomMapSqlParameterSource().addValue("network_id", networkId));
    }

    @Override
    public VdsNetworkInterface get(Guid id) {
        return getCallsHandler().executeRead("GetVdsInterfaceById",
                vdsNetworkInterfaceRowMapper,
                getCustomMapSqlParameterSource().addValue("vds_interface_id", id));
    }

    private static final RowMapper<VdsNetworkInterface> vdsNetworkInterfaceRowMapper =
            new RowMapper<VdsNetworkInterface>() {
                @Override
                public VdsNetworkInterface mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                    VdsNetworkInterface entity = createInterface(rs);
                    entity.getStatistics().setId(getGuidDefaultEmpty(rs, "id"));
                    entity.getStatistics().setReceiveRate(rs.getDouble("rx_rate"));
                    entity.getStatistics().setTransmitRate(rs.getDouble("tx_rate"));
                    entity.getStatistics().setReceiveDropRate(rs.getDouble("rx_drop"));
                    entity.getStatistics().setTransmitDropRate(rs.getDouble("tx_drop"));
                    entity.getStatistics().setStatus(InterfaceStatus.forValue(rs.getInt("iface_status")));
                    entity.getStatistics().setVdsId(getGuidDefaultEmpty(rs, "vds_id"));
                    entity.setType((Integer) rs.getObject("type"));
                    entity.setGateway(rs.getString("gateway"));
                    entity.setSubnet(rs.getString("subnet"));
                    entity.setAddress(rs.getString("addr"));
                    entity.setNetworkName(rs.getString("network_name"));
                    entity.setName(rs.getString("name"));
                    entity.setVdsId(getGuid(rs, "vds_id"));
                    entity.setVdsName(rs.getString("vds_name"));
                    entity.setId(getGuidDefaultEmpty(rs, "id"));
                    entity.setBootProtocol(NetworkBootProtocol.forValue(rs.getInt("boot_protocol")));
                    entity.setMtu(rs.getInt("mtu"));
                    entity.setBridged(rs.getBoolean("bridged"));
                    return entity;
                }

                /**
                 * Create the correct type according to the row. If the type can't be determined for whatever reason,
                 * {@link VdsNetworkInterface} instance is created & initialized.
                 *
                 * @param rs
                 *            The row representing the entity.
                 * @return The instance of the correct type as represented by the row.
                 * @throws SQLException
                 */
                private VdsNetworkInterface createInterface(ResultSet rs) throws SQLException {
                    VdsNetworkInterface iface;

                    String macAddress = rs.getString("mac_addr");
                    Integer vlanId = (Integer) rs.getObject("vlan_id");
                    Integer bondType = (Integer) rs.getObject("bond_type");
                    String bondName = rs.getString("bond_name");
                    Boolean isBond = (Boolean) rs.getObject("is_bond");
                    String bondOptions = rs.getString("bond_opts");
                    Integer speed = (Integer) rs.getObject("speed");

                    if (isBond != null && vlanId != null) {
                        iface = new VdsNetworkInterface();
                        iface.setMacAddress(macAddress);
                        iface.setVlanId(vlanId);
                        iface.setBondType(bondType);
                        iface.setBondName(bondName);
                        iface.setBonded(isBond);
                        iface.setBondOptions(bondOptions);
                        iface.setSpeed(speed);
                    } else if (Boolean.TRUE.equals(isBond)) {
                        iface = new Bond(macAddress, bondOptions, bondType);
                    } else if (vlanId != null) {
                        iface = new Vlan(vlanId);
                    } else {
                        iface = new Nic(macAddress, speed, bondName);
                    }

                    return iface;
                }
            };

}
