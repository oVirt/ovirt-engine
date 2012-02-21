package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * <code>NetworkDAODbFacadeImpl</code> provides a concrete implementation of {@link #NetworkDAO} based on code
 * refactored from {@link #DbFacade}.
 */
public class NetworkDAODbFacadeImpl extends DefaultGenericDaoDbFacade<network, Guid> implements NetworkDAO {

    private static ParameterizedRowMapper<network> parameterizedRowMapper;
    static {
        parameterizedRowMapper = new ParameterizedRowMapper<network>() {
            @Override
            public network mapRow(ResultSet rs, int rowNum) throws SQLException {
                network entity = new network();
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
                entity.setis_display((Boolean) rs.getObject("is_display"));
                entity.setStatus(NetworkStatus.forValue(rs.getInt("status")));
                entity.setMtu(rs.getInt("mtu"));
                return entity;
            };
        };
    }

    @Override
    public network getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("networkName", name);

        Map<String, Object> dbResults = dialect.createJdbcCallForQuery(jdbcTemplate)
                .withProcedureName("GetnetworkByName")
                .returningResultSet("RETURN_VALUE", parameterizedRowMapper)
                .execute(parameterSource);

        return (network) DbFacadeUtils.asSingleResult((List<?>) (dbResults
                .get("RETURN_VALUE")));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<network> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        Map<String, Object> dbResults = dialect.createJdbcCallForQuery(jdbcTemplate)
                .withProcedureName("GetAllFromnetwork")
                .returningResultSet("RETURN_VALUE", parameterizedRowMapper)
                .execute(parameterSource);

        return (List<network>) dbResults.get("RETURN_VALUE");
    }

    @Override
    public List<network> getAllForDataCenter(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        return getCallsHandler().executeReadList("GetAllNetworkByStoragePoolId",
                parameterizedRowMapper,
                parameterSource);
    }

    @Override
    public List<network> getAllForCluster(Guid id) {
        return getAllForCluster(id, null, false);
    }

    @Override
    public List<network> getAllForCluster(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);

        return getCallsHandler().executeReadList("GetAllNetworkByClusterId", parameterizedRowMapper, parameterSource);
    }

    @Override
    protected String getProcedureNameForUpdate() {
        return "Updatenetwork";
    }

    @Override
    protected String getProcedureNameForGet() {
        return "GetnetworkByid";
    }

    @Override
    protected String getProcedureNameForGetAll() {
        return "GetAllFromnetwork";
    }

    @Override
    protected String getProcedureNameForSave() {
        return "Insertnetwork";
    }

    @Override
    protected String getProcedureNameForRemove() {
        return "Deletenetwork";
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(network network) {
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
                .addValue("mtu", network.getMtu());

    }

    @Override
    protected ParameterizedRowMapper<network> createEntityRowMapper() {
        return parameterizedRowMapper;
    }
}
