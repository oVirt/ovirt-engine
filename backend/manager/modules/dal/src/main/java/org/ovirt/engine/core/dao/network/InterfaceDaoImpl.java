package org.ovirt.engine.core.dao.network;

import static org.ovirt.engine.core.dao.network.HostNetworkQosDaoImpl.HostNetworkQosDaoDbFacadaeImplMapper.MAPPER;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.Vlan;
import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;
import org.ovirt.engine.core.dao.BaseDao;
import org.ovirt.engine.core.dao.network.NetworkStatisticsDaoImpl.NetworkStatisticsParametersMapper;
import org.ovirt.engine.core.dao.network.NetworkStatisticsDaoImpl.NetworkStatisticsRowMapper;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class InterfaceDaoImpl extends BaseDao implements InterfaceDao {

    @Inject
    private NetworkQoSDao networkQosDao;

    @Inject
    private HostNetworkQosDao hostNetworkQosDao;

    @Override
    public void saveStatisticsForVds(VdsNetworkStatistics stats) {
        MapSqlParameterSource parameterSource = createStatisticsParametersMapper(stats);
        getCallsHandler().executeModification("Insertvds_interface_statistics", parameterSource);
    }

    @Override
    public void massUpdateInterfacesForVds(List<VdsNetworkInterface> dbIfacesToBatch) {
        updateAllInBatch("Updatevds_interface", dbIfacesToBatch, this::createInterfaceParametersMapper);
    }

    @Override
    public void massClearNetworkFromNics(List<Guid> nicIds) {
        getCallsHandler().executeStoredProcAsBatch("Clear_network_from_nics",
                nicIds,
                id -> {
                    CustomMapSqlParameterSource paramSource = getCustomMapSqlParameterSource();
                    paramSource.addValue("id", id);
                    return paramSource;

                });
    }

    public void updateAllInBatch(String procedureName,
            Collection<VdsNetworkInterface> paramValues,
            MapSqlParameterMapper<VdsNetworkInterface> mapper) {
        for (VdsNetworkInterface entity : paramValues) {
            hostNetworkQosDao.persistQosChanges(entity.getId(), entity.getQos());
        }
        getCallsHandler().executeStoredProcAsBatch(procedureName, paramValues, mapper);
    }

    @Override
    public void saveInterfaceForVds(VdsNetworkInterface nic) {
        hostNetworkQosDao.persistQosChanges(nic.getId(), nic.getQos());
        MapSqlParameterSource parameterSource = createInterfaceParametersMapper(nic);
        getCallsHandler().executeModification("Insertvds_interface", parameterSource);
    }

    @Override
    public void updateStatisticsForVds(VdsNetworkStatistics stats) {
        update(stats);
    }

    @Override
    public void massUpdateStatisticsForVds(Collection<VdsNetworkStatistics> statistics) {
        List<MapSqlParameterSource> executions =
                statistics.stream().map(this::createStatisticsParametersMapper).collect(Collectors.toList());

        getCallsHandler().executeStoredProcAsBatch("Updatevds_interface_statistics", executions);
    }

    /**
     * Update the {@link VdsNetworkStatistics} in the DB
     *
     * @param stats
     *            The host's network statistics data.
     */
    private void update(VdsNetworkStatistics stats) {
        getCallsHandler().executeModification("Updatevds_interface_statistics", createStatisticsParametersMapper(stats));
    }

    private MapSqlParameterSource createStatisticsParametersMapper(VdsNetworkStatistics stats) {
        NetworkStatisticsParametersMapper<VdsNetworkStatistics> mapper = new NetworkStatisticsParametersMapper<>();
        return getCustomMapSqlParameterSource()
                .addValues(mapper.createParametersMap(stats))
                .addValue("vds_id", stats.getVdsId());
    }

    @Override
    public void updateInterfaceForVds(VdsNetworkInterface nic) {
        hostNetworkQosDao.persistQosChanges(nic.getId(), nic.getQos());
        getCallsHandler().executeModification("Updatevds_interface", createInterfaceParametersMapper(nic));
    }

    private MapSqlParameterSource createInterfaceParametersMapper(VdsNetworkInterface nic) {
        return getCustomMapSqlParameterSource()
                .addValue("addr", nic.getIpv4Address())
                .addValue("ipv6_address", nic.getIpv6Address())
                .addValue("bond_name", nic.getBondName())
                .addValue("bond_type", nic.getBondType())
                .addValue("gateway", nic.getIpv4Gateway())
                .addValue("ipv4_default_route", nic.isIpv4DefaultRoute())
                .addValue("ipv6_gateway", nic.getIpv6Gateway())
                .addValue("id", nic.getId())
                .addValue("is_bond", nic.getBonded())
                .addValue("bond_opts", nic.getBondOptions())
                .addValue("mac_addr", nic.getMacAddress())
                .addValue("name", nic.getName())
                .addValue("network_name", nic.getNetworkName())
                .addValue("speed", nic.getSpeed())
                .addValue("subnet", nic.getIpv4Subnet())
                .addValue("ipv6_prefix", nic.getIpv6Prefix())
                .addValue("boot_protocol", nic.getIpv4BootProtocol())
                .addValue("ipv6_boot_protocol", nic.getIpv6BootProtocol())
                .addValue("type", nic.getType())
                .addValue("vds_id", nic.getVdsId())
                .addValue("vlan_id", nic.getVlanId())
                .addValue("base_interface", nic.getBaseInterface())
                .addValue("mtu", nic.getMtu())
                .addValue("bridged", nic.isBridged())
                .addValue("labels", SerializationFactory.getSerializer().serialize(nic.getLabels()))
                .addValue("ad_partner_mac", nic.getAdPartnerMac())
                .addValue("ad_aggregator_id", nic.getAdAggregatorId())
                .addValue("bond_active_slave", nic.isBond() ? ((Bond)nic).getActiveSlave() : null)
                .addValue("reported_switch_type", nic.getReportedSwitchType() == null ? null : nic.getReportedSwitchType().getOptionValue());
    }

    @Override
    public List<VdsNetworkInterface> getAllInterfacesForVds(Guid id) {
        return getAllInterfacesForVds(id, null, false);
    }

    @Override
    public Map<Guid, List<String>> getHostNetworksByCluster(Guid clusterId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterId);

        List<Pair<Guid, String>> hostNetworks =
                getCallsHandler().executeReadList("GetHostNetworksByCluster", hostNetworkNameMapper, parameterSource);

        Map<Guid, List<String>> neworksByHost = new HashMap<>();
        for (Pair<Guid, String> pair : hostNetworks) {
            if (!neworksByHost.containsKey(pair.getFirst())) {
                neworksByHost.put(pair.getFirst(), new ArrayList<>());
            }

            neworksByHost.get(pair.getFirst()).add(pair.getSecond());
        }

        return neworksByHost;
    }

    @Override
    public List<VdsNetworkInterface> getAllInterfacesWithIpAddress(Guid clusterId, String ipAddress) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("addr", ipAddress).addValue("cluster_id", clusterId);

        return getCallsHandler().executeReadList("Getinterface_viewByAddr",
                qosDaoInterfaceMapper, parameterSource);
    }

    @Override
    public List<VdsNetworkInterface> getAllInterfacesForVds(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return getCallsHandler().executeReadList("GetInterfaceViewWithQosByVdsId",
                interfaceWithQosMapper,
                parameterSource);
    }

    @Override
    public VdsNetworkInterface getManagedInterfaceForVds(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vds_id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return getCallsHandler().executeRead("GetVdsManagedInterfaceByVdsId",
                qosDaoInterfaceMapper,
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

        networkQosDao.remove(id);
        getCallsHandler().executeModification("Deletevds_interface", parameterSource);
    }

    @Override
    public List<VdsNetworkInterface> getVdsInterfacesByNetworkId(Guid networkId) {
        return getCallsHandler().executeReadList("GetVdsInterfacesByNetworkId",
                qosDaoInterfaceMapper,
                getCustomMapSqlParameterSource().addValue("network_id", networkId));
    }

    @Override
    public VdsNetworkInterface get(Guid id) {
        return getCallsHandler().executeRead("GetVdsInterfaceById",
                qosDaoInterfaceMapper,
                getCustomMapSqlParameterSource().addValue("vds_interface_id", id));
    }

    @Override
    public VdsNetworkInterface get(Guid hostId, String name) {
        return getCallsHandler().executeRead("GetVdsInterfaceByName",
                qosDaoInterfaceMapper,
                getCustomMapSqlParameterSource()
                        .addValue("host_id", hostId)
                        .addValue("name", name));
    }

    @Override
    public List<VdsNetworkInterface> getAllInterfacesByClusterId(Guid clusterId) {
        return getCallsHandler().executeReadList("GetInterfacesWithQosByClusterId",
                interfaceWithQosMapper,
                getCustomMapSqlParameterSource().addValue("cluster_id", clusterId));
    }

    @Override
    public List<VdsNetworkInterface> getAllInterfacesByLabelForDataCenter(Guid dataCenterId, String label) {
        return nicsContainingLabel(getAllInterfacesByDataCenterId(dataCenterId), label);
    }

    @Override
    public List<VdsNetworkInterface> getAllInterfacesByLabelForCluster(Guid clusterId, String label) {
        return nicsContainingLabel(getAllInterfacesByClusterId(clusterId), label);
    }

    protected List<VdsNetworkInterface> nicsContainingLabel(List<VdsNetworkInterface> interfaces, String label) {
        return interfaces.stream()
                .filter(i -> i.getLabels() != null && i.getLabels().contains(label))
                .collect(Collectors.toList());
    }

    @Override
    public List<VdsNetworkInterface> getAllInterfacesByDataCenterId(Guid dataCenterId) {
        return getCallsHandler().executeReadList("GetInterfacesByDataCenterId",
                qosDaoInterfaceMapper,
                getCustomMapSqlParameterSource().addValue("data_center_id", dataCenterId));
    }

    @Override
    public Set<String> getAllNetworkLabelsForDataCenter(Guid dataCenterId) {
        return getAllInterfacesByDataCenterId(dataCenterId).stream()
                .filter(nic -> nic.getLabels() != null)
                .flatMap(nic -> nic.getLabels().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public List<VdsNetworkInterface> getIscsiIfacesByHostIdAndStorageTargetId(Guid hostId, String storageTargetId) {
        return getCallsHandler().executeReadList("GetIscsiIfacesByHostIdAndStorageTargetId",
                qosDaoInterfaceMapper,
                getCustomMapSqlParameterSource().addValue("host_id", hostId).addValue("target_id", storageTargetId));
    }

    @Override
    public Optional<VdsNetworkInterface> getActiveMigrationNetworkInterfaceForHost(Guid hostId) {
        return Optional.ofNullable(getCallsHandler().executeRead("getActiveMigrationNetworkInterfaceForHost",
                qosDaoInterfaceMapper,
                getCustomMapSqlParameterSource().addValue("host_id", hostId)));
    }

    private final RowMapper<HostNetworkQos> qosDaoRetriever = (rs, rowNum) ->
            hostNetworkQosDao.get(getGuidDefaultEmpty(rs, "id"));

    private final VdsNetworkInterfaceRowMapper qosDaoInterfaceMapper = new VdsNetworkInterfaceRowMapper(qosDaoRetriever);

    private final RowMapper<HostNetworkQos> qosResultSetMapper = (rs, rowNum) -> {
        HostNetworkQos qos = null;
        if (getGuid(rs, "qos_id") != null) {
            qos = MAPPER.createQosEntity(rs);
            qos.setId(getGuid(rs, "id"));
        }
        return qos;
    };

    private final VdsNetworkInterfaceRowMapper interfaceWithQosMapper = new VdsNetworkInterfaceRowMapper(qosResultSetMapper);

    private static final class VdsNetworkInterfaceRowMapper  implements RowMapper<VdsNetworkInterface> {

        private final RowMapper<HostNetworkQos> hostNetworkQosMapper;

        VdsNetworkInterfaceRowMapper(RowMapper<HostNetworkQos> mapper) {
            this.hostNetworkQosMapper = mapper;
        }

        @SuppressWarnings("unchecked")
        @Override
        public VdsNetworkInterface mapRow(ResultSet rs, int rowNum) throws SQLException {
            VdsNetworkInterface entity = createInterface(rs);

                    entity.setSpeed((Integer) rs.getObject("speed"));
                    entity.setMacAddress(rs.getString("mac_addr"));
                    entity.setStatistics(HostNetworkStatisticsRowMapper.INSTANCE.mapRow(rs, rowNum));
                    entity.setType((Integer) rs.getObject("type"));
                    entity.setIpv4Gateway(rs.getString("gateway"));
                    entity.setIpv4DefaultRoute(rs.getBoolean("ipv4_default_route"));
                    entity.setIpv6Gateway(rs.getString("ipv6_gateway"));
                    entity.setIpv4Subnet(rs.getString("subnet"));
                    entity.setIpv6Prefix(getInteger(rs, "ipv6_prefix"));
                    entity.setIpv4Address(rs.getString("addr"));
                    entity.setIpv6Address(rs.getString("ipv6_address"));
                    entity.setNetworkName(rs.getString("network_name"));
                    entity.setName(rs.getString("name"));
                    entity.setVdsId(getGuid(rs, "vds_id"));
                    entity.setVdsName(rs.getString("vds_name"));
                    entity.setId(getGuidDefaultEmpty(rs, "id"));
                    entity.setIpv4BootProtocol(Ipv4BootProtocol.forValue(rs.getInt("boot_protocol")));
                    entity.setIpv6BootProtocol(Ipv6BootProtocol.forValue(rs.getInt("ipv6_boot_protocol")));
                    entity.setReportedSwitchType(SwitchType.parse(rs.getString("reported_switch_type")));
                    entity.setMtu(rs.getInt("mtu"));
                    entity.setBridged(rs.getBoolean("bridged"));
                    entity.setQos(hostNetworkQosMapper.mapRow(rs, rowNum));
                    entity.setLabels(SerializationFactory.getDeserializer().deserialize(rs.getString("labels"),
                        HashSet.class));
                    entity.setAdPartnerMac(rs.getString("ad_partner_mac"));
                    entity.setAdAggregatorId(getInteger(rs, "ad_aggregator_id"));

            return entity;
        }

        /**
         * Create the correct type according to the row.
         *
         * @param rs The row representing the entity.
         * @return The instance of the correct type as represented by the row.
         */
        private VdsNetworkInterface createInterface(ResultSet rs) throws SQLException {
            Integer vlanId = (Integer) rs.getObject("vlan_id");
            Boolean isBond = (Boolean) rs.getObject("is_bond");

            if (Boolean.TRUE.equals(isBond)) {
                Bond bond = new Bond();

                bond.setBonded(isBond);
                bond.setBondOptions(rs.getString("bond_opts"));
                bond.setBondType((Integer) rs.getObject("bond_type"));
                bond.setActiveSlave(rs.getString("bond_active_slave"));

                return bond;
            }

            if (vlanId != null) {
                Vlan vlan = new Vlan();
                vlan.setVlanId(vlanId);
                vlan.setBaseInterface(rs.getString("base_interface"));
                vlan.setBonded(isBond);
                return vlan;
            }

            Nic nic = new Nic();
            nic.setBondName(rs.getString("bond_name"));
            nic.setBonded(isBond);
            return nic;

        }

    }

    private static final RowMapper<Pair<Guid, String>> hostNetworkNameMapper =
            (rs, rowNum) -> new Pair<>(getGuid(rs, "vds_id"), rs.getString("network_name"));

    private static class HostNetworkStatisticsRowMapper extends NetworkStatisticsRowMapper<VdsNetworkStatistics> {

        private static final HostNetworkStatisticsRowMapper INSTANCE = new HostNetworkStatisticsRowMapper();

        @Override
        public VdsNetworkStatistics mapRow(ResultSet rs, int rowNum) throws SQLException {
            VdsNetworkStatistics entity = super.mapRow(rs, rowNum);
            entity.setVdsId(getGuidDefaultEmpty(rs, "vds_id"));
            return entity;
        }

        @Override
        protected VdsNetworkStatistics createEntity() {
            return new VdsNetworkStatistics();
        }
    }
}
