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
 * <code>NetworkDAODbFacadeImpl</code> provides a concrete implementation of {@link NetworkDAO} based on code refactored
 * from {@link DbFacade}.
 *
 *
 */
public class NetworkDAODbFacadeImpl extends BaseDAODbFacade implements NetworkDAO {

    @Override
    public network getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("networkName", name);

        ParameterizedRowMapper<network> mapper = new ParameterizedRowMapper<network>() {
            @Override
            public network mapRow(ResultSet rs, int rowNum) throws SQLException {
                network entity = new network();
                ;
                entity.setaddr(rs.getString("addr"));
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs.getString("id")));
                entity.setname(rs.getString("name"));
                entity.setsubnet(rs.getString("subnet"));
                entity.setgateway(rs.getString("gateway"));
                entity.settype((Integer) rs.getObject("type"));
                entity.setvlan_id((Integer) rs.getObject("vlan_id"));
                entity.setstp(rs.getBoolean("stp"));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setis_display((Boolean) rs.getObject("is_display"));
                return entity;
            }
        };

        Map<String, Object> dbResults = dialect.createJdbcCallForQuery(jdbcTemplate)
                .withProcedureName("GetnetworkByName")
                .returningResultSet("RETURN_VALUE", mapper)
                .execute(parameterSource);

        return (network) DbFacadeUtils.asSingleResult((List<?>) (dbResults
                .get("RETURN_VALUE")));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<network> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<network> mapper = new ParameterizedRowMapper<network>() {
            @Override
            public network mapRow(ResultSet rs, int rowNum) throws SQLException {
                network entity = new network();
                ;
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
                return entity;
            }
        };

        Map<String, Object> dbResults = dialect.createJdbcCallForQuery(jdbcTemplate)
                .withProcedureName("GetAllFromnetwork")
                .returningResultSet("RETURN_VALUE", mapper)
                .execute(parameterSource);

        return (List<network>) dbResults.get("RETURN_VALUE");
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<network> getAllForDataCenter(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        ParameterizedRowMapper<network> mapper = new ParameterizedRowMapper<network>() {
            @Override
            public network mapRow(ResultSet rs, int rowNum) throws SQLException {
                network entity = new network();
                ;
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
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAllNetworkByStoragePoolId", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<network> getAllForCluster(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        ParameterizedRowMapper<network> mapper = new ParameterizedRowMapper<network>() {
            @Override
            public network mapRow(ResultSet rs, int rowNum) throws SQLException {
                network entity = new network();
                ;
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
                entity.setStatus(NetworkStatus.forValue(rs.getInt("status")));
                entity.setis_display((Boolean) rs.getObject("is_display"));
                return entity;
            }

        };

        return getCallsHandler().executeReadList("GetAllNetworkByClusterId", mapper, parameterSource);
    }

    @Override
    public void save(network net) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("addr", net.getaddr())
                .addValue("description", net.getdescription())
                .addValue("id", net.getId()).addValue("name", net.getname())
                .addValue("subnet", net.getsubnet())
                .addValue("gateway", net.getgateway())
                .addValue("type", net.gettype())
                .addValue("vlan_id", net.getvlan_id())
                .addValue("stp", net.getstp())
                .addValue("storage_pool_id", net.getstorage_pool_id());

        getCallsHandler().executeModification("Insertnetwork", parameterSource);
    }

    @Override
    public void update(network net) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("addr", net.getaddr())
                .addValue("description", net.getdescription())
                .addValue("id", net.getId()).addValue("name", net.getname())
                .addValue("subnet", net.getsubnet())
                .addValue("gateway", net.getgateway())
                .addValue("type", net.gettype())
                .addValue("vlan_id", net.getvlan_id())
                .addValue("stp", net.getstp())
                .addValue("storage_pool_id", net.getstorage_pool_id());

        getCallsHandler().executeModification("Updatenetwork", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        getCallsHandler().executeModification("Deletenetwork", parameterSource);
    }

    @Override
    public network get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        ParameterizedRowMapper<network> mapper = new ParameterizedRowMapper<network>() {
            @Override
            public network mapRow(ResultSet rs, int rowNum) throws SQLException {
                network entity = new network();
                ;
                entity.setaddr(rs.getString("addr"));
                entity.setdescription(rs.getString("description"));
                entity.setId(Guid.createGuidFromString(rs.getString("id")));
                entity.setname(rs.getString("name"));
                entity.setsubnet(rs.getString("subnet"));
                entity.setgateway(rs.getString("gateway"));
                entity.settype((Integer) rs.getObject("type"));
                entity.setvlan_id((Integer) rs.getObject("vlan_id"));
                entity.setstp(rs.getBoolean("stp"));
                entity.setstorage_pool_id(NGuid.createGuidFromString(rs
                        .getString("storage_pool_id")));
                entity.setis_display((Boolean) rs.getObject("is_display"));
                return entity;
            }
        };

        return getCallsHandler().executeRead("GetnetworkByid", mapper, parameterSource);
    }
}
