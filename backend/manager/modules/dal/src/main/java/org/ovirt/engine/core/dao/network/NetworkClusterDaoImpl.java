package org.ovirt.engine.core.dao.network;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class NetworkClusterDaoImpl extends BaseDao implements NetworkClusterDao {

    private static final RowMapper<NetworkCluster> mapper = (rs, rowNum) -> {
        NetworkCluster entity = new NetworkCluster();
        entity.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));
        entity.setNetworkId(getGuidDefaultEmpty(rs, "network_id"));
        entity.setStatus(NetworkStatus.forValue(rs.getInt("status")));
        entity.setDisplay(rs.getBoolean("is_display"));
        entity.setRequired(rs.getBoolean("required"));
        entity.setMigration(rs.getBoolean("migration"));
        entity.setManagement(rs.getBoolean("management"));
        entity.setGluster(rs.getBoolean("is_gluster"));
        entity.setDefaultRoute(rs.getBoolean("default_route"));
        return entity;
    };

    @Override
    public NetworkCluster get(NetworkClusterId id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", id.getClusterId())
                .addValue("network_id", id.getNetworkId());

        return getCallsHandler().executeRead("Getnetwork_clusterBycluster_idAndBynetwork_id", mapper, parameterSource);
    }

    @Override
    public List<NetworkCluster> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetAllFromnetwork_cluster", mapper, parameterSource);
    }

    @Override
    public List<NetworkCluster> getAllForCluster(Guid clusterid) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterid);

        return getCallsHandler().executeReadList("GetAllFromnetwork_clusterByClusterId", mapper,
                parameterSource);
    }

    @Override
    public List<NetworkCluster> getAllForNetwork(Guid network) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("network_id", network);

        return getCallsHandler().executeReadList("GetAllFromnetwork_clusterByNetworkId", mapper,
                parameterSource);
    }

    @Override
    public void save(NetworkCluster cluster) {
        MapSqlParameterSource parameterSource = createAllFieldsParameterSource(cluster);

        getCallsHandler().executeModification("Insertnetwork_cluster", parameterSource);
    }

    @Override
    public void update(NetworkCluster cluster) {
        MapSqlParameterSource parameterSource = createAllFieldsParameterSource(cluster);

        getCallsHandler().executeModification("Updatenetwork_cluster", parameterSource);
    }

    private MapSqlParameterSource createAllFieldsParameterSource(NetworkCluster networkCluster) {
        return getCustomMapSqlParameterSource()
                .addValue("cluster_id", networkCluster.getClusterId())
                .addValue("network_id", networkCluster.getNetworkId())
                .addValue("status", networkCluster.getStatus())
                .addValue("is_display", networkCluster.isDisplay())
                .addValue("required", networkCluster.isRequired())
                .addValue("migration", networkCluster.isMigration())
                .addValue("management", networkCluster.isManagement())
                .addValue("is_gluster", networkCluster.isGluster())
                .addValue("default_route", networkCluster.isDefaultRoute());

    }

    @Override
    public void updateStatus(NetworkCluster cluster) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", cluster.getClusterId())
                .addValue("network_id", cluster.getNetworkId())
                .addValue("status", cluster.getStatus());

        getCallsHandler().executeModification("Updatenetwork_cluster_status", parameterSource);
    }

    @Override
    public void remove(NetworkClusterId networkClusterId) {
        remove(networkClusterId.clusterId, networkClusterId.networkId);
    }

    @Override
    public void remove(Guid clusterid, Guid networkid) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterid)
                .addValue("network_id", networkid);

        getCallsHandler().executeModification("Deletenetwork_cluster", parameterSource);
    }

    @Override
    public void setNetworkExclusivelyAsDisplay(Guid clusterId, Guid networkId) {
        setExclusiveFlagOnNetworkCluster(clusterId, networkId, "set_network_exclusively_as_display");
    }

    @Override
    public void setNetworkExclusivelyAsMigration(Guid clusterId, Guid networkId) {
        setExclusiveFlagOnNetworkCluster(clusterId, networkId, "set_network_exclusively_as_migration");
    }

    @Override
    public void setNetworkExclusivelyAsDefaultRoute(Guid clusterId, Guid networkId) {
        setExclusiveFlagOnNetworkCluster(clusterId, networkId, "set_network_exclusively_as_default_role_network");
    }

    @Override
    public void setNetworkExclusivelyAsManagement(Guid clusterId, Guid networkId) {
        setExclusiveFlagOnNetworkCluster(clusterId, networkId, "set_network_exclusively_as_management");
    }

    @Override
    public void setNetworkExclusivelyAsGluster(Guid clusterId, Guid networkId) {
        setExclusiveFlagOnNetworkCluster(clusterId, networkId, "set_network_exclusively_as_gluster");
    }

    private void setExclusiveFlagOnNetworkCluster(Guid clusterId, Guid networkId, String procedureName) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterId).addValue("network_id", networkId);

        getCallsHandler().executeModification(procedureName, parameterSource);
    }
}
