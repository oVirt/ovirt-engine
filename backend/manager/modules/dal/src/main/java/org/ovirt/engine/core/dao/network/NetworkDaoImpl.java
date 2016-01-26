package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
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
                NetworkRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("id", networkId)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public Network getByName(String name) {
        return getCallsHandler().executeRead("GetnetworkByName",
                NetworkRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("networkName", name));
    }

    @Override
    public Network getByNameAndDataCenter(String name, Guid storagePoolId) {
        return getCallsHandler().executeRead("GetNetworkByNameAndDataCenter",
                NetworkRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("name", name)
                        .addValue("storage_pool_id", storagePoolId));
    }

    @Override
    public Network getByNameAndCluster(String name, Guid clusterId) {
        return getCallsHandler().executeRead("GetNetworkByNameAndCluster",
                NetworkRowMapper.instance,
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
                NetworkRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public List<Network> getAllForDataCenter(Guid id) {
        return getAllForDataCenter(id, null, false);
    }

    @Override
    public List<Network> getAllForDataCenter(Guid id, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetAllNetworkByStoragePoolId",
                NetworkRowMapper.instance,
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
                NetworkClusterRowMapper.INSTANCE,
                getCustomMapSqlParameterSource()
                        .addValue("id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public List<Network> getAllForQos(Guid qosId) {
        return getCallsHandler().executeReadList("GetAllNetworksByQosId",
                NetworkRowMapper.instance,
                createIdParameterMapper(qosId));
    }

    @Override
    public List<Network> getAllForProvider(Guid id) {
        return getCallsHandler().executeReadList("GetAllNetworksByNetworkProviderId",
                NetworkRowMapper.instance,
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
        List<Network> labeledNetworks = new ArrayList<>();
        for (Network network : networksInCluster) {
            if (StringUtils.equals(network.getLabel(), label)) {
                labeledNetworks.add(network);
            }
        }

        return labeledNetworks;
    }

    @Override
    public Network getManagementNetwork(Guid clusterId) {
        return getCallsHandler().executeRead("GetManagementNetworkByCluster",
                NetworkRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("cluster_id", clusterId));
    }

    @Override
    public List<Network> getManagementNetworks(Guid dataCenterId) {
        return getCallsHandler().executeReadList("GetAllManagementNetworksByDataCenterId",
                NetworkRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("data_center_id", dataCenterId));
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
                .addValue("qos_id", network.getQosId())
                .addValue("label", network.getLabel());
    }

    @Override
    protected RowMapper<Network> createEntityRowMapper() {
        return NetworkRowMapper.instance;
    }

    private static final class NetworkClusterRowMapper extends NetworkRowMapper
            implements RowMapper<Network> {
        public static final NetworkClusterRowMapper INSTANCE = new NetworkClusterRowMapper();

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

            return entity;
        }
    }

    abstract static class NetworkRowMapperBase<T extends Network> implements RowMapper<T> {
        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            T entity = createNetworkEntity();
            entity.setId(getGuidDefaultEmpty(rs, "id"));
            entity.setName(rs.getString("name"));
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
                        rs.getString("provider_network_external_id")));
            }
            entity.setQosId(getGuid(rs, "qos_id"));

            entity.setLabel(rs.getString("label"));
            return entity;
        }

        protected abstract T createNetworkEntity();
    }

    static class NetworkRowMapper extends NetworkRowMapperBase<Network> {
        public static final NetworkRowMapper instance = new NetworkRowMapper();

        @Override
        protected Network createNetworkEntity() {
            return new Network();
        }
    }
}
