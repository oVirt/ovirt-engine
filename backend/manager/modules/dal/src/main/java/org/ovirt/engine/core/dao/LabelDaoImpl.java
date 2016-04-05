package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class LabelDaoImpl extends BaseDao implements LabelDao {
    private static class LabelRowMapper implements RowMapper<Label> {
        public static final LabelRowMapper instance = new LabelRowMapper();

        @Override
        public Label mapRow(ResultSet rs, int rowNum) throws SQLException {
            String[] rawUuids = (String[])rs.getArray("vm_ids").getArray();
            List<String> vms = Arrays.asList(rawUuids);

            rawUuids = (String[])rs.getArray("vds_ids").getArray();
            List<String> hosts = Arrays.asList(rawUuids);

            return new LabelBuilder()
                    .id(getGuidDefaultNewGuid(rs, "label_id"))
                    .name(rs.getString("label_name"))
                    .readOnly(rs.getBoolean("read_only"))
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
        }
    }
    @Override
    public Label get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("label_id", id);

        return getCallsHandler()
                .executeRead("GetLabelById", LabelRowMapper.instance, parameterSource);
    }

    @Override
    public Label getByName(String name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("label_name", name);

        return getCallsHandler()
                .executeRead("GetLabelByName", LabelRowMapper.instance, parameterSource);
    }

    @Override
    public List<Label> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler()
                .executeReadList("GetAllLabels", LabelRowMapper.instance, parameterSource);
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
                .executeReadList("GetLabelsByReferencedIds", LabelRowMapper.instance, parameterSource);
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
                .executeReadList("GetLabelByIds", LabelRowMapper.instance, parameterSource);
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
                .addValue("vms", createArrayOf("uuid", label.getVms().toArray()))
                .addValue("hosts", createArrayOf("uuid", label.getHosts().toArray()));

        getCallsHandler()
                .executeModification("UpdateLabel", parameterSource);
    }
}
