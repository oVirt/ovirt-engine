package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.NetworkCluster;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * <code>NetworkDAODbFacadeImpl</code> provides a concrete implementation of {@link #NetworkDAO} based on code
 * refactored from {@link #DbFacade}.
 */
public class NetworkDAODbFacadeImpl extends DefaultGenericDaoDbFacade<Network, Guid> implements NetworkDAO {

    public NetworkDAODbFacadeImpl() {
        super("network");
        setProcedureNameForGet("GetnetworkByid");
        setProcedureNameForGetAll("GetAllFromnetwork");
    }

    @Override
    public Network getByName(String name) {
        Map<String, Object> dbResults = dialect.createJdbcCallForQuery(jdbcTemplate)
                .withProcedureName("GetnetworkByName")
                .returningResultSet("RETURN_VALUE", NetworkRowMapper.instance)
                .execute(getCustomMapSqlParameterSource()
                        .addValue("networkName", name));

        return (Network) DbFacadeUtils.asSingleResult((List<?>) (dbResults
                .get("RETURN_VALUE")));
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
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(Network network) {
        return getCustomMapSqlParameterSource()
                .addValue("addr", network.getaddr())
                .addValue("description", network.getdescription())
                .addValue("id", network.getId())
                .addValue("name", network.getname())
                .addValue("subnet", network.getsubnet())
                .addValue("gateway", network.getgateway())
                .addValue("type", network.gettype())
                .addValue("vlan_id", network.getvlan_id())
                .addValue("stp", network.getstp())
                .addValue("storage_pool_id", network.getstorage_pool_id())
                .addValue("mtu", network.getMtu())
                .addValue("vm_network", network.isVmNetwork());
    }

    @Override
    protected ParameterizedRowMapper<Network> createEntityRowMapper() {
        return NetworkRowMapper.instance;
    }

    private static final class NetworkClusterRowMapper extends NetworkRowMapper
            implements ParameterizedRowMapper<Network> {
        public final static NetworkClusterRowMapper INSTANCE = new NetworkClusterRowMapper();

        @Override
        public Network mapRow(ResultSet rs, int rowNum) throws SQLException {
            Network entity = super.mapRow(rs, rowNum);

            entity.setCluster(new NetworkCluster());
            entity.getCluster().setis_display((Boolean) rs.getObject("is_display"));
            entity.getCluster().setRequired(rs.getBoolean("required"));
            entity.getCluster().setstatus(NetworkStatus.forValue(rs.getInt("status")));

            return entity;
        }
    }

    abstract static class NetworkRowMapperBase<T extends Network> implements ParameterizedRowMapper<T> {
        public final static NetworkRowMapper instance = new NetworkRowMapper();

        @Override
        public T mapRow(ResultSet rs, int rowNum) throws SQLException {
            T entity = createNetworkEntity();
            entity.setId(Guid.createGuidFromString(rs.getString("id")));
            entity.setname(rs.getString("name"));
            entity.setdescription(rs.getString("description"));
            entity.settype((Integer) rs.getObject("type"));
            entity.setaddr(rs.getString("addr"));
            entity.setsubnet(rs.getString("subnet"));
            entity.setgateway(rs.getString("gateway"));
            entity.setvlan_id((Integer) rs.getObject("vlan_id"));
            entity.setstp(rs.getBoolean("stp"));
            entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                 .getString("storage_pool_id")));
            entity.setMtu(rs.getInt("mtu"));
            entity.setVmNetwork(rs.getBoolean("vm_network"));

            return entity;
        }

        abstract protected T createNetworkEntity();
    }

    static class NetworkRowMapper extends NetworkRowMapperBase<Network> {
        public final static NetworkRowMapper instance = new NetworkRowMapper();

        protected Network createNetworkEntity() {
            return new Network();
        }
    }
}
