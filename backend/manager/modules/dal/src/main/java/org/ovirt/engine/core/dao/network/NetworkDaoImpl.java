package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class NetworkDaoImpl extends DefaultGenericDao<Network, Guid> implements NetworkDao {

    @Inject
    private DnsResolverConfigurationDao dnsResolverConfigurationDao;

    private NetworkClusterRowMapper networkClusterRowMapper;

    private NetworkRowMapper networkRowMapper;

    @PostConstruct
    public void init() {
        networkClusterRowMapper = new NetworkClusterRowMapper(dnsResolverConfigurationDao);
        networkRowMapper = new NetworkRowMapper(dnsResolverConfigurationDao);
    }

    public NetworkDaoImpl() {
        super("network");
        setProcedureNameForGet("GetnetworkByid");
        setProcedureNameForGetAll("GetAllFromnetwork");
    }

    @Override
    public Network get(Guid networkId) {
        return get(networkId, null, false);
    }

    @Override
    public Network get(Guid networkId, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead(getProcedureNameForGet(),
                networkRowMapper,
                getCustomMapSqlParameterSource().addValue("id", networkId)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public Network getByNameAndDataCenter(String name, Guid storagePoolId) {
        return getCallsHandler().executeRead("GetNetworkByNameAndDataCenter",
                networkRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("name", name)
                        .addValue("storage_pool_id", storagePoolId));
    }

    @Override
    public Network getByNameAndCluster(String name, Guid clusterId) {
        return getCallsHandler().executeRead("GetNetworkByNameAndCluster",
                networkRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("name", name)
                        .addValue("cluster_id", clusterId));
    }

    @Override
    public List<Network> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<Network> getAll(Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetAllFromnetwork",
                networkRowMapper,
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public List<Network> getAllForDataCenter(Guid id) {
        return getAllForDataCenter(id, null, false);
    }

    @Override
    public List<Network> getAllForDataCenter(Guid id, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetAllNetworkByStoragePoolId",
                networkRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public List<Network> getAllForCluster(Guid id) {
        if (id == null) {
            return Collections.emptyList();
        }
        return getAllForCluster(id, null, false);
    }

    @Override
    public List<Network> getAllForCluster(Guid id, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetAllNetworkByClusterId",
                networkClusterRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("id", id)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public List<Network> getAllForQos(Guid qosId) {
        return getCallsHandler().executeReadList("GetAllNetworksByQosId",
                networkRowMapper,
                createIdParameterMapper(qosId));
    }

    @Override
    public List<Network> getAllForProvider(Guid id) {
        return getCallsHandler().executeReadList("GetAllNetworksByNetworkProviderId",
                networkRowMapper,
                createIdParameterMapper(id));
    }

    @Override
    public Set<String> getAllNetworkLabelsForDataCenter(Guid id) {
        return new HashSet<>(getCallsHandler().executeReadList("GetAllNetworkLabelsByDataCenterId",
                new SingleColumnRowMapper<>(),
                createIdParameterMapper(id)));
    }

    @Override
    public List<Network> getAllByLabelForCluster(String label, Guid clusterId) {
        List<Network> networksInCluster = getAllForCluster(clusterId);
        return networksInCluster.stream()
                .filter(network -> StringUtils.equals(network.getLabel(), label)).collect(Collectors.toList());
    }

    @Override
    public Network getManagementNetwork(Guid clusterId) {
        return getCallsHandler().executeRead("GetManagementNetworkByCluster",
                networkRowMapper,
                getCustomMapSqlParameterSource().addValue("cluster_id", clusterId));
    }

    @Override
    public List<Network> getManagementNetworks(Guid dataCenterId) {
        return getCallsHandler().executeReadList("GetAllManagementNetworksByDataCenterId",
                networkRowMapper,
                getCustomMapSqlParameterSource().addValue("data_center_id", dataCenterId));
    }

    @Override
    public List<Network> getAllExternalNetworksLinkedToPhysicalNetwork(Guid physicalNetworkId) {
        return getCallsHandler().executeReadList("GetAllNetworksByProviderPhysicalNetworkId",
                networkRowMapper,
                getCustomMapSqlParameterSource().addValue("network_id", physicalNetworkId));
    }

    @Override
    public List<Network> getRequiredNetworksByDataCenterId(Guid dataCenterId) {
        return getCallsHandler().executeReadList("GetRequiredNetworksByDataCenterId",
                networkRowMapper,
                getCustomMapSqlParameterSource().addValue("data_center_id", dataCenterId));
    }

    @Override
    public Network getNetworkByVdsmNameAndDataCenterId(String vdsmName, Guid dataCenterId) {
        return getCallsHandler().executeRead("GetNetworkByVdsmNameAndDataCenterId",
                networkRowMapper,
                getCustomMapSqlParameterSource().addValue("vdsm_name", vdsmName)
                        .addValue("data_center_id", dataCenterId));
    }

    @Override
    public Map<String, Network> getNetworksForCluster(Guid clusterId) {
        return getAllForCluster(clusterId)
            .stream()
            .collect(Collectors.toMap(Network::getName, Function.identity()));
    }

    @Override
    public void save(Network entity) {
        DnsResolverConfiguration dnsResolverConfiguration = entity.getDnsResolverConfiguration();
        if (dnsResolverConfiguration != null) {
            Validate.isTrue(dnsResolverConfiguration.getId() == null);
            dnsResolverConfigurationDao.save(dnsResolverConfiguration);
        }
        super.save(entity);
    }

    @Override
    public void update(Network entity) {
        DnsResolverConfiguration dnsResolverConfiguration = entity.getDnsResolverConfiguration();
        if (dnsResolverConfiguration == null) {
            dnsResolverConfigurationDao.removeByNetworkId(entity.getId());
        } else {
            if (dnsResolverConfiguration.getId() == null) {
                dnsResolverConfigurationDao.save(dnsResolverConfiguration);
            } else {
                dnsResolverConfigurationDao.update(dnsResolverConfiguration);
            }
        }
        super.update(entity);
    }

    @Override
    public void remove(Guid guid) {
        dnsResolverConfigurationDao.removeByNetworkId(guid);
        super.remove(guid);
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(Network network) {
        return getCustomMapSqlParameterSource()
                .addValue("addr", network.getAddr())
                .addValue("description", network.getDescription())
                .addValue("free_text_comment", network.getComment())
                .addValue("id", network.getId())
                .addValue("name", network.getName())
                .addValue("vdsm_name", network.getVdsmName())
                .addValue("subnet", network.getSubnet())
                .addValue("gateway", network.getGateway())
                .addValue("type", network.getType())
                .addValue("vlan_id", network.getVlanId())
                .addValue("stp", network.getStp())
                .addValue("storage_pool_id", network.getDataCenterId())
                .addValue("mtu", network.getMtu())
                .addValue("vm_network", network.isVmNetwork())
                .addValue("provider_network_provider_id",
                        network.getProvidedBy() == null ? null : network.getProvidedBy().getProviderId())
                .addValue("provider_network_external_id",
                        network.getProvidedBy() == null ? null : network.getProvidedBy().getExternalId())
                .addValue("provider_physical_network_id",
                        network.getProvidedBy() == null ? null : network.getProvidedBy().getPhysicalNetworkId())
                .addValue("qos_id", network.getQosId())
                .addValue("label", network.getLabel())
                .addValue("dns_resolver_configuration_id", getDnsResolverConfigurationId(network))
                .addValue("port_isolation", network.isPortIsolation());
    }

    private Guid getDnsResolverConfigurationId(Network network) {
        DnsResolverConfiguration dnsResolverConfiguration = network.getDnsResolverConfiguration();
        if (dnsResolverConfiguration == null) {
            return null;
        }

        return dnsResolverConfiguration.getId();
    }

    @Override
    protected RowMapper<Network> createEntityRowMapper() {
        return networkRowMapper;
    }

    private static final class NetworkClusterRowMapper extends NetworkRowMapper implements RowMapper<Network> {

        private NetworkClusterRowMapper(DnsResolverConfigurationDao dnsResolverConfigurationDao) {
            super(dnsResolverConfigurationDao);
        }

        @Override
        public Network mapRow(ResultSet rs, int rowNum) throws SQLException {
            Network entity = super.mapRow(rs, rowNum);

            entity.setCluster(new NetworkCluster());
            entity.getCluster().setDisplay((Boolean) rs.getObject("is_display"));
            entity.getCluster().setRequired(rs.getBoolean("required"));
            entity.getCluster().setStatus(NetworkStatus.forValue(rs.getInt("status")));
            entity.getCluster().setMigration(rs.getBoolean("migration"));
            entity.getCluster().setManagement(rs.getBoolean("management"));
            entity.getCluster().setGluster(rs.getBoolean("is_gluster"));
            entity.getCluster().setDefaultRoute(rs.getBoolean("default_route"));

            return entity;
        }
    }

    abstract static class NetworkRowMapperBase<T extends Network> implements RowMapper<T> {

        private final DnsResolverConfigurationDao dnsResolverConfigurationDao;

        protected NetworkRowMapperBase(DnsResolverConfigurationDao dnsResolverConfigurationDao) {
            this.dnsResolverConfigurationDao = dnsResolverConfigurationDao;
        }

        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            T entity = createNetworkEntity();
            entity.setId(getGuidDefaultEmpty(rs, "id"));
            entity.setName(rs.getString("name"));
            entity.setVdsmName(rs.getString("vdsm_name"));
            entity.setDescription(rs.getString("description"));
            entity.setComment(rs.getString("free_text_comment"));
            entity.setType((Integer) rs.getObject("type"));
            entity.setAddr(rs.getString("addr"));
            entity.setSubnet(rs.getString("subnet"));
            entity.setGateway(rs.getString("gateway"));
            entity.setVlanId((Integer) rs.getObject("vlan_id"));
            entity.setStp(rs.getBoolean("stp"));
            entity.setDataCenterId(getGuidDefaultEmpty(rs, "storage_pool_id"));
            entity.setMtu(rs.getInt("mtu"));
            entity.setVmNetwork(rs.getBoolean("vm_network"));
            Guid providerId = getGuid(rs, "provider_network_provider_id");
            if (providerId != null) {
                entity.setProvidedBy(new ProviderNetwork(
                        providerId,
                        rs.getString("provider_network_external_id"),
                        getGuid(rs, "provider_physical_network_id")));
            }
            entity.setQosId(getGuid(rs, "qos_id"));

            entity.setLabel(rs.getString("label"));

            Guid dnsResolverConfigurationId = getGuid(rs, "dns_resolver_configuration_id");
            entity.setDnsResolverConfiguration(dnsResolverConfigurationDao.get(dnsResolverConfigurationId));
            entity.setPortIsolation(rs.getBoolean("port_isolation"));

            return entity;
        }

        protected abstract T createNetworkEntity();
    }

    private static class NetworkRowMapper extends NetworkRowMapperBase<Network> {
        public NetworkRowMapper(DnsResolverConfigurationDao dnsResolverConfigurationDao) {
            super(dnsResolverConfigurationDao);
        }

        @Override
        protected Network createNetworkEntity() {
            return new Network();
        }
    }
}
