package org.ovirt.engine.core.dao.network;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.network.ProviderNetwork;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDaoDbFacade;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class NetworkDaoDbFacadeImpl extends DefaultGenericDaoDbFacade<Network, Guid> implements NetworkDao {

    public NetworkDaoDbFacadeImpl() {
        super("network");
        setProcedureNameForGet("GetnetworkByid");
        setProcedureNameForGetAll("GetAllFromnetwork");
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
    public List<Network> getAllForProvider(Guid id) {
        return getCallsHandler().executeReadList("GetAllNetworksByNetworkProviderId",
                NetworkRowMapper.instance,
                createIdParameterMapper(id));
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
                        network.getProvidedBy() == null ? null : network.getProvidedBy().getExternalId());
    }

    @Override
    protected RowMapper<Network> createEntityRowMapper() {
        return NetworkRowMapper.instance;
    }

    private static final class NetworkClusterRowMapper extends NetworkRowMapper
            implements RowMapper<Network> {
        public final static NetworkClusterRowMapper INSTANCE = new NetworkClusterRowMapper();

        @Override
        public Network mapRow(ResultSet rs, int rowNum) throws SQLException {
            Network entity = super.mapRow(rs, rowNum);

            entity.setCluster(new NetworkCluster());
            entity.getCluster().setDisplay((Boolean) rs.getObject("is_display"));
            entity.getCluster().setRequired(rs.getBoolean("required"));
            entity.getCluster().setStatus(NetworkStatus.forValue(rs.getInt("status")));
            entity.getCluster().setMigration(rs.getBoolean("migration"));

            return entity;
        }
    }

    abstract static class NetworkRowMapperBase<T extends Network> implements RowMapper<T> {
        public final static NetworkRowMapper instance = new NetworkRowMapper();

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

            return entity;
        }

        abstract protected T createNetworkEntity();
    }

    static class NetworkRowMapper extends NetworkRowMapperBase<Network> {
        public final static NetworkRowMapper instance = new NetworkRowMapper();

        @Override
        protected Network createNetworkEntity() {
            return new Network();
        }
    }
}
