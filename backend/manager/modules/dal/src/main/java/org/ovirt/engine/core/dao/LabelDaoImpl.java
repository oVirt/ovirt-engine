package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class LabelDaoImpl extends BaseDao implements LabelDao {
    private static final RowMapper<Label> labelRowMapper = (rs, rowNum) -> {
        String[] rawUuids = (String[])rs.getArray("vm_ids").getArray();
        List<String> vms = Arrays.asList(rawUuids);

        rawUuids = (String[])rs.getArray("vds_ids").getArray();
        List<String> hosts = Arrays.asList(rawUuids);

        return new LabelBuilder()
                .id(getGuidDefaultNewGuid(rs, "label_id"))
                .name(rs.getString("label_name"))
                .readOnly(rs.getBoolean("read_only"))
                .implicitAffinityGroup(rs.getBoolean("has_implicit_affinity_group"))
                .vmIds(vms.stream()
                        // Labels with no assignments will have null in the column
                        .filter(v -> v != null)
                        // Convert to Guid
                        .map(Guid::new)
                        .collect(Collectors.toSet()))
                .hostIds(hosts.stream()
                        // Labels with no assignments will have null in the column
                        .filter(v -> v != null)
                        // Convert to Guid
                        .map(Guid::new)
                        .collect(Collectors.toSet()))
                .build();
    };

    private static final RowMapper<Pair<Guid, String>> entityIdNameRowMapper = (rs, rowNum) -> {
        Pair<Guid, String> idNamePair = new Pair<>();
        Guid entityId = getGuidDefaultNewGuid(rs, "entity_id");
        String entityName = rs.getString("entity_name");
        idNamePair.setFirst(entityId);
        idNamePair.setSecond(entityName);

        return idNamePair;
    };

    @Override
    public Label get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("label_id", id);

        return getCallsHandler()
                .executeRead("GetLabelById", labelRowMapper, parameterSource);
    }

    @Override
    public Label getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("label_name", name);

        return getCallsHandler()
                .executeRead("GetLabelByName", labelRowMapper, parameterSource);
    }

    @Override
    public List<Label> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler()
                .executeReadList("GetAllLabels", labelRowMapper, parameterSource);
    }

    @Override
    public List<Label> getAllByEntityIds(Iterable<Guid> entities) {
        List<UUID> uuids = new ArrayList<>();
        for (Guid guid: entities) {
            uuids.add(guid.getUuid());
        }

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("entity_ids", createArrayOf("uuid", uuids.toArray()));

        return getCallsHandler()
                .executeReadList("GetLabelsByReferencedIds", labelRowMapper, parameterSource);
    }

    @Override
    public List<Label> getAllByClusterId(Guid clusterId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", clusterId);

        return getCallsHandler()
                .executeReadList("GetAllLabelsForCluster", labelRowMapper, parameterSource);
    }

    @Override
    public List<Label> getAllByIds(Iterable<Guid> ids) {
        List<UUID> uuids = new ArrayList<>();
        for (Guid guid: ids) {
            uuids.add(guid.getUuid());
        }

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("label_ids", createArrayOf("uuid", uuids.toArray()));

        return getCallsHandler()
                .executeReadList("GetLabelByIds", labelRowMapper, parameterSource);
    }

    @Override
    public void save(Label label) {
        Guid id = label.getId();
        if (Guid.isNullOrEmpty(id)) {
            id = Guid.newGuid();
            label.setId(id);
        }

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("label_id", label.getId())
                .addValue("label_name", label.getName())
                .addValue("readonly", label.isReadOnly())
                .addValue("has_implicit_affinity_group", label.isImplicitAffinityGroup())
                .addValue("vms", createArrayOf("uuid", label.getVms().toArray()))
                .addValue("hosts", createArrayOf("uuid", label.getHosts().toArray()));

        getCallsHandler().executeModification("CreateLabel", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("label_id", id);

        getCallsHandler()
                .executeModification("DeleteLabel", parameterSource);
    }

    @Override
    public void update(Label label) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("label_id", label.getId())
                .addValue("label_name", label.getName())
                .addValue("readonly", label.isReadOnly())
                .addValue("has_implicit_affinity_group", label.isImplicitAffinityGroup())
                .addValue("vms", createArrayOf("uuid", label.getVms().toArray()))
                .addValue("hosts", createArrayOf("uuid", label.getHosts().toArray()));

        getCallsHandler()
                .executeModification("UpdateLabel", parameterSource);
    }

    @Override
    public void addVmToLabels(Guid vmId, List<Guid> labelIds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("labels", createArrayOf("uuid", labelIds.toArray()));

        getCallsHandler()
                .executeModification("AddVmToLabels", parameterSource);
    }

    @Override
    public void addHostToLabels(Guid hostId, List<Guid> labelIds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("host_id", hostId)
                .addValue("labels", createArrayOf("uuid", labelIds.toArray()));

        getCallsHandler()
                .executeModification("AddHostToLabels", parameterSource);
    }

    @Override
    public void updateLabelsForVm(Guid vmId, List<Guid> labelIds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("labels", createArrayOf("uuid", labelIds.toArray()));

        getCallsHandler()
                .executeModification("UpdateLabelsForVm", parameterSource);
    }

    @Override
    public void updateLabelsForHost(Guid hostId, List<Guid> labelIds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("host_id", hostId)
                .addValue("labels", createArrayOf("uuid", labelIds.toArray()));

        getCallsHandler()
                .executeModification("UpdateLabelsForHost", parameterSource);
    }

    @Override
    public Map<Guid, String> getEntitiesNameMap() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        List<Pair<Guid, String>> returnValues = getCallsHandler()
                .executeReadList("GetEntitiesNameMap", entityIdNameRowMapper, parameterSource);

        Map<Guid, String> returnMap = new HashMap<>();
        returnValues.forEach(pair -> returnMap.put(pair.getFirst(), pair.getSecond()));

        return returnMap;
    }
}
