package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>NetworkClusterDAODbFacadeImpl</code> provides a concrete implementation of {@link NetworkClusterDAO} based on
 * code refactored from {@link DbFacade}.
 *
 *
 */
public class NetworkClusterDAODbFacadeImpl extends BaseDAODbFacade implements NetworkClusterDAO {

    @SuppressWarnings("unchecked")
    @Override
    public List<network_cluster> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<network_cluster> mapper = new ParameterizedRowMapper<network_cluster>() {
            @Override
            public network_cluster mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                network_cluster entity = new network_cluster();
                entity.setcluster_id(Guid.createGuidFromString(rs
                        .getString("cluster_id")));
                entity.setnetwork_id(Guid.createGuidFromString(rs
                        .getString("network_id")));
                entity.setstatus(rs.getInt("status"));
                entity.setis_display(rs.getBoolean("is_display"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAllFromnetwork_cluster", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<network_cluster> getAllForCluster(Guid clusterid) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterid);

        ParameterizedRowMapper<network_cluster> mapper = new ParameterizedRowMapper<network_cluster>() {
            @Override
            public network_cluster mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                network_cluster entity = new network_cluster();
                entity.setcluster_id(Guid.createGuidFromString(rs
                        .getString("cluster_id")));
                entity.setnetwork_id(Guid.createGuidFromString(rs
                        .getString("network_id")));
                entity.setstatus(rs.getInt("status"));
                entity.setis_display(rs.getBoolean("is_display"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAllFromnetwork_clusterByClusterId", mapper,
                parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<network_cluster> getAllForNetwork(Guid network) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("network_id", network);

        ParameterizedRowMapper<network_cluster> mapper = new ParameterizedRowMapper<network_cluster>() {
            @Override
            public network_cluster mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                network_cluster entity = new network_cluster();
                entity.setnetwork_id(Guid.createGuidFromString(rs
                        .getString("network_id")));
                entity.setcluster_id(Guid.createGuidFromString(rs
                        .getString("cluster_id")));
                entity.setstatus(rs.getInt("status"));
                entity.setis_display(rs.getBoolean("is_display"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAllFromnetwork_clusterByNetworkId",mapper,
                parameterSource);
    }

    @Override
    public void save(network_cluster cluster) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", cluster.getcluster_id())
                .addValue("network_id", cluster.getnetwork_id())
                .addValue("status", cluster.getstatus())
                .addValue("is_display", cluster.getis_display());

        getCallsHandler().executeModification("Insertnetwork_cluster", parameterSource);
    }

    @Override
    public void update(network_cluster cluster) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", cluster.getcluster_id())
                .addValue("network_id", cluster.getnetwork_id())
                .addValue("status", cluster.getstatus())
                .addValue("is_display", cluster.getis_display());

        getCallsHandler().executeModification("Updatenetwork_cluster", parameterSource);
    }

    @Override
    public void updateStatus(network_cluster cluster) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", cluster.getcluster_id())
                .addValue("network_id", cluster.getnetwork_id())
                .addValue("status", cluster.getstatus());

        getCallsHandler().executeModification("Updatenetwork_cluster_status", parameterSource);
    }

    @Override
    public void remove(Guid clusterid, Guid networkid) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterid).addValue("network_id",
                        networkid);

        getCallsHandler().executeModification("Deletenetwork_cluster", parameterSource);
    }
}
