package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Nic;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkStatistics;
import org.ovirt.engine.core.common.businessentities.network.Vlan;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;
import org.ovirt.engine.core.dao.BaseDAODbFacade;
import org.ovirt.engine.core.dao.network.NetworkStatisticsDaoDbFacadeImpl.NetworkStatisticsParametersMapper;
import org.ovirt.engine.core.dao.network.NetworkStatisticsDaoDbFacadeImpl.NetworkStatisticsRowMapper;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class InterfaceDaoDbFacadeImpl extends BaseDAODbFacade implements InterfaceDao {

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
        updateAllInBatch("Updatevds_interface", dbIfacesToBatch, new MapSqlParameterMapper<VdsNetworkInterface>() {
            @Override
            public MapSqlParameterSource map(VdsNetworkInterface nic) {
                return createInterfaceParametersMapper(nic);
            }
        });
    }

    public void updateAllInBatch(String procedureName,
            Collection<VdsNetworkInterface> paramValues,
            MapSqlParameterMapper<VdsNetworkInterface> mapper) {
        for (VdsNetworkInterface entity : paramValues) {
            persistQosChanges(entity);
        }
        getCallsHandler().executeStoredProcAsBatch(procedureName, paramValues, mapper);
    }

    @Override
    public void saveInterfaceForVds(VdsNetworkInterface nic) {
        persistQosChanges(nic);
        MapSqlParameterSource parameterSource = createInterfaceParametersMapper(nic);
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
            executions.add(createStatisticsParametersMapper(stats));
        }

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
        persistQosChanges(nic);
        getCallsHandler().executeModification("Updatevds_interface", createInterfaceParametersMapper(nic));
    }

    private MapSqlParameterSource createInterfaceParametersMapper(VdsNetworkInterface nic) {
        return getCustomMapSqlParameterSource()
                .addValue("addr", nic.getAddress())
                .addValue("bond_name", nic.getBondName())
                .addValue("bond_type", nic.getBondType())
                .addValue("gateway", nic.getGateway())
                .addValue("id", nic.getId())
                .addValue("is_bond", nic.getBonded())
                .addValue("bond_opts", nic.getBondOptions())
                .addValue("mac_addr", nic.getMacAddress())
                .addValue("name", nic.getName())
                .addValue("network_name", nic.getNetworkName())
                .addValue("speed", nic.getSpeed())
                .addValue("subnet", nic.getSubnet())
                .addValue("boot_protocol", nic.getBootProtocol())
                .addValue("type", nic.getType())
                .addValue("vds_id", nic.getVdsId())
                .addValue("vlan_id", nic.getVlanId())
                .addValue("base_interface", nic.getBaseInterface())
                .addValue("mtu", nic.getMtu())
                .addValue("bridged", nic.isBridged())
                .addValue("qos_overridden", nic.isQosOverridden())
                .addValue("labels", SerializationFactory.getSerializer().serialize(nic.getLabels()))
                .addValue("custom_properties", nic.hasCustomProperties() ?
                        SerializationFactory.getSerializer().serialize(nic.getCustomProperties()) : null);
    }

    private void persistQosChanges(VdsNetworkInterface entity) {
        Guid id = entity.getId();
        HostNetworkQos oldQos = hostNetworkQosDao.get(id);
        HostNetworkQos qos = entity.getQos();
        if (qos == null) {
            if (oldQos != null) {
                hostNetworkQosDao.remove(id);
            }
        } else {
            qos.setId(id);
            if (oldQos == null) {
                hostNetworkQosDao.save(qos);
            } else if (!qos.equals(oldQos)) {
                hostNetworkQosDao.update(qos);
            }
        }
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
                neworksByHost.put(pair.getFirst(), new ArrayList<String>());
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

        networkQosDao.remove(id);
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

    @Override
    public List<VdsNetworkInterface> getAllInterfacesByClusterId(Guid clusterId) {
        return getCallsHandler().executeReadList("GetInterfacesByClusterId",
                vdsNetworkInterfaceRowMapper,
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
        List<VdsNetworkInterface> result = new ArrayList<>();

        for (VdsNetworkInterface vdsNetworkInterface : interfaces) {
            if (vdsNetworkInterface.getLabels() != null && vdsNetworkInterface.getLabels().contains(label)) {
                result.add(vdsNetworkInterface);
            }
        }

        return result;
    }

    private List<VdsNetworkInterface> getAllInterfacesByDataCenterId(Guid dataCenterId) {
        return getCallsHandler().executeReadList("GetInterfacesByDataCenterId",
                vdsNetworkInterfaceRowMapper,
                getCustomMapSqlParameterSource().addValue("data_center_id", dataCenterId));
    }

    @Override
    public Set<String> getAllNetworkLabelsForDataCenter(Guid dataCenterId) {
        Set<String> labels = new HashSet<>();
        for (VdsNetworkInterface nic : getAllInterfacesByDataCenterId(dataCenterId)) {
            if (nic.getLabels() != null) {
                labels.addAll(nic.getLabels());
            }
        }

        return labels;

    }

    @Override
    public List<VdsNetworkInterface> getIscsiIfacesByHostIdAndStorageTargetId(Guid hostId, String storageTargetId) {
        return getCallsHandler().executeReadList("GetIscsiIfacesByHostIdAndStorageTargetId",
                vdsNetworkInterfaceRowMapper,
                getCustomMapSqlParameterSource().addValue("host_id", hostId).addValue("target_id", storageTargetId));
    }

    private final RowMapper<VdsNetworkInterface> vdsNetworkInterfaceRowMapper =
            new RowMapper<VdsNetworkInterface>() {
                @SuppressWarnings("unchecked")
                @Override
                public VdsNetworkInterface mapRow(ResultSet rs, int rowNum)
                        throws SQLException {
                    VdsNetworkInterface entity = createInterface(rs);
                    entity.setStatistics(HostNetworkStatisticsRowMapper.INSTANCE.mapRow(rs, rowNum));
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
                    entity.setQos(hostNetworkQosDao.get(entity.getId()));
                    entity.setQosOverridden(rs.getBoolean("qos_overridden"));
                    entity.setLabels(SerializationFactory.getDeserializer().deserialize(rs.getString("labels"),
                            HashSet.class));
                    entity.setCustomProperties(SerializationFactory.getDeserializer()
                            .deserialize(rs.getString("custom_properties"), LinkedHashMap.class));
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
                    String baseInterface = rs.getString("base_interface");
                    Integer bondType = (Integer) rs.getObject("bond_type");
                    String bondName = rs.getString("bond_name");
                    Boolean isBond = (Boolean) rs.getObject("is_bond");
                    String bondOptions = rs.getString("bond_opts");
                    Integer speed = (Integer) rs.getObject("speed");

                    if (Boolean.TRUE.equals(isBond) && vlanId != null) {
                        iface = new VdsNetworkInterface();
                        iface.setMacAddress(macAddress);
                        iface.setVlanId(vlanId);
                        iface.setBaseInterface(baseInterface);
                        iface.setBondType(bondType);
                        iface.setBondName(bondName);
                        iface.setBonded(isBond);
                        iface.setBondOptions(bondOptions);
                        iface.setSpeed(speed);
                    } else if (Boolean.TRUE.equals(isBond)) {
                        iface = new Bond(macAddress, bondOptions, bondType);
                    } else if (vlanId != null) {
                        iface = new Vlan(vlanId, baseInterface);
                    } else {
                        iface = new Nic(macAddress, speed, bondName);
                    }

                    return iface;
                }
            };

    private static final RowMapper<Pair<Guid, String>> hostNetworkNameMapper =
            new RowMapper<Pair<Guid, String>>() {

        @Override
        public Pair<Guid, String> mapRow(ResultSet rs, int arg1) throws SQLException {
            return new Pair<>(getGuid(rs, "vds_id"), rs.getString("network_name"));
        }
    };

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
