package org.ovirt.engine.core.dao.network;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.MassOperationsGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class HostNicVfsConfigDaoImpl extends MassOperationsGenericDao<HostNicVfsConfig, Guid>
        implements HostNicVfsConfigDao {

    private final RowMapper<HostNicVfsConfig> nicVfsConfigRowMapper = (rs, rowNum) -> {
        HostNicVfsConfig entity = new HostNicVfsConfig();
        entity.setId(getGuid(rs, "id"));
        entity.setNicId(getGuid(rs, "nic_id"));
        entity.setAllNetworksAllowed(rs.getBoolean("is_all_networks_allowed"));

        fillNetworksAndLabelsDataOnConfig(entity);

        return entity;
    };

    public HostNicVfsConfigDaoImpl() {
        super("HostNicVfsConfig");
        setProcedureNameForGet("GetHostNicVfsConfigById");
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(HostNicVfsConfig hostNicVfsConfig) {
        return createIdParameterMapper(hostNicVfsConfig.getId())
                .addValue("nic_id", hostNicVfsConfig.getNicId())
                .addValue("is_all_networks_allowed", hostNicVfsConfig.isAllNetworksAllowed());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<HostNicVfsConfig> createEntityRowMapper() {
        return nicVfsConfigRowMapper;
    }

    private void fillNetworksAndLabelsDataOnConfig(HostNicVfsConfig vfsConfig) {
        Guid id = vfsConfig.getId();
        vfsConfig.setNetworks(getNetworksByVfsConfigId(id));
        vfsConfig.setNetworkLabels(getLabelsByVfsConfigId(id));
    }

    @Override
    public void save(HostNicVfsConfig entity) {
        super.save(entity);
        saveNetworksAndLabels(entity);
    }

    @Override
    public void update(HostNicVfsConfig entity) {
        super.update(entity);
        removeAllNetworksByVfsConfigId(entity.getId());
        removeAllLabelsByVfsConfigId(entity.getId());
        saveNetworksAndLabels(entity);
    }

    private void saveNetworksAndLabels(HostNicVfsConfig entity) {
        massNetworksUpdate(entity.getId(), entity.getNetworks());
        massLabelsUpdate(entity.getId(), entity.getNetworkLabels());
    }

    private MapSqlParameterSource createVfsConfigIdParameter(Guid vfsConfigId) {
        return getCustomMapSqlParameterSource().addValue("vfs_config_id", vfsConfigId);
    }

    @Override
    public List<HostNicVfsConfig> getAllVfsConfigByHostId(Guid hostId) {
        return getCallsHandler().executeReadList("GetAllVfsConfigByHostId",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource()
                .addValue("host_id", hostId));
    }

    // VfsConfigNetworks

    Set<Guid> getNetworksByVfsConfigId(Guid vfsConfigId) {
        return new HashSet<>(getCallsHandler().executeReadList("GetNetworksByVfsConfigId", createGuidMapper(),
                createVfsConfigIdParameter(vfsConfigId)));
    }

    @Override
    public HostNicVfsConfig getByNicId(Guid nicId) {
        return getCallsHandler().executeRead("GetVfsConfigByNicId",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("nic_id", nicId));
    }

    @Override
    public void addNetwork(Guid vfsConfigId, Guid networkId) {
        getCallsHandler().executeModification("InsertVfsConfigNetwork",
                createNetworkParametersMapper(vfsConfigId, networkId));
    }

    private void massNetworksUpdate(Guid vfsConfigId, Set<Guid> networks) {
        List<MapSqlParameterSource> executions = networks.stream()
                .map(networkId -> createNetworkParametersMapper(vfsConfigId, networkId)).collect(Collectors.toList());

        getCallsHandler().executeStoredProcAsBatch("InsertVfsConfigNetwork", executions);
    }

    @Override
    public void removeNetwork(Guid vfsConfigId, Guid networkId) {
        getCallsHandler().executeModification("DeleteVfsConfigNetwork",
                createNetworkParametersMapper(vfsConfigId, networkId));
    }

    private void removeAllNetworksByVfsConfigId(Guid vfsConfigId) {
        MapSqlParameterSource parameterSource = createVfsConfigIdParameter(vfsConfigId);
        getCallsHandler().executeModification("DeleteAllVfsConfigNetworks", parameterSource);
    }

    private MapSqlParameterSource createNetworkParametersMapper(Guid vfsConfigId, Guid networkId) {
        return getCustomMapSqlParameterSource()
                .addValue("vfs_config_id", vfsConfigId)
                .addValue("network_id", networkId);
    }

    // VfsConfigLabels

    Set<String> getLabelsByVfsConfigId(Guid vfsConfigId) {
        return new HashSet<>(getCallsHandler().executeReadList("GetLabelsByVfsConfigId",
                new SingleColumnRowMapper<>(),
                createVfsConfigIdParameter(vfsConfigId)));
    }

    @Override
    public void addLabel(Guid vfsConfigId, String label) {
        getCallsHandler().executeModification("InsertVfsConfigLabel", createLabelParametersMapper(vfsConfigId, label));
    }

    private void massLabelsUpdate(Guid vfsConfigId, Set<String> labels) {
        List<MapSqlParameterSource> executions =
                labels.stream().map(l -> createLabelParametersMapper(vfsConfigId, l)).collect(Collectors.toList());

        getCallsHandler().executeStoredProcAsBatch("InsertVfsConfigLabel", executions);
    }

    @Override
    public void removeLabel(Guid vfsConfigId, String label) {
        getCallsHandler().executeModification("DeleteVfsConfigLabel", createLabelParametersMapper(vfsConfigId, label));
    }

    private void removeAllLabelsByVfsConfigId(Guid vfsConfigId) {
        MapSqlParameterSource parameterSource = createVfsConfigIdParameter(vfsConfigId);
        getCallsHandler().executeModification("DeleteAllVfsConfigLabels", parameterSource);
    }

    private MapSqlParameterSource createLabelParametersMapper(Guid vfsConfigId, String label) {
        return getCustomMapSqlParameterSource()
                .addValue("vfs_config_id", vfsConfigId)
                .addValue("label", label);
    }
}
