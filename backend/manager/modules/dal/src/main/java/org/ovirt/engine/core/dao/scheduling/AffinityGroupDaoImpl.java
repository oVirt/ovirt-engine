package org.ovirt.engine.core.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.EntityAffinityRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.dao.DefaultGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class AffinityGroupDaoImpl extends DefaultGenericDao<AffinityGroup, Guid> implements AffinityGroupDao {

    public AffinityGroupDaoImpl() {
        super("AffinityGroup");
    }

    @Override
    public List<AffinityGroup> getAllAffinityGroupsByClusterId(Guid clusterId) {
        return getCallsHandler().executeReadList("getAllAffinityGroupsByClusterId",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("cluster_id", clusterId));
    }

    @Override
    public List<AffinityGroup> getAllAffinityGroupsWithFlatLabelsByClusterId(Guid clusterId) {
        return getCallsHandler().executeReadList("getAllAffinityGroupsWithFlatLabelsByClusterId",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("cluster_id", clusterId));
    }

    @Override
    public List<AffinityGroup> getAllAffinityGroupsByVmId(Guid vmId) {
        return getCallsHandler().executeReadList("getAllAffinityGroupsByVmId",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("vm_id", vmId));
    }

    @Override
    public List<AffinityGroup> getAllAffinityGroupsWithFlatLabelsByVmId(Guid vmId) {
        return getCallsHandler().executeReadList("getAllAffinityGroupsWithFlatLabelsByVmId",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("vm_id", vmId));
    }

    @Override
    public AffinityGroup getByName(String str) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("name", str);

        return (AffinityGroup) DbFacadeUtils.asSingleResult(
                getCallsHandler().executeReadList("GetAffinityGroupByName",
                        createEntityRowMapper(),
                        parameterSource));
    }

    @Override
    public void save(AffinityGroup entity) {
        getCallsHandler().executeModification("InsertAffinityGroupWithMembers", createFullParametersMapper(entity));
    }

    @Override
    public void update(AffinityGroup entity) {
        getCallsHandler().executeModification("UpdateAffinityGroupWithMembers", createFullParametersMapper(entity));
    }

    @Override
    public List<AffinityGroup> getPositiveEnforcingAffinityGroupsByRunningVmsOnVdsId(Guid vdsId) {
        return getCallsHandler().executeReadList("getPositiveEnforcingAffinityGroupsByRunningVmsOnVdsId",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("vds_id", vdsId));
    }

    @Override
    public void setAffinityGroupsForVm(Guid vmId, List<Guid> groupIds) {
        getCallsHandler().executeModification("SetAffinityGroupsForVm",
                getCustomMapSqlParameterSource()
                        .addValue("vm_id", vmId)
                        .addValue("groups", createArrayOf("uuid", groupIds.toArray())));
    }

    @Override
    public void setAffinityGroupsForHost(Guid hostId, List<Guid> groupIds) {
        getCallsHandler().executeModification("SetAffinityGroupsForHost",
                getCustomMapSqlParameterSource()
                        .addValue("host_id", hostId)
                        .addValue("groups", createArrayOf("uuid", groupIds.toArray())));
    }

    @Override
    public void insertAffinityVm(Guid affinityId, Guid vmId){
        getCallsHandler().executeModification("InsertAffinityVm",
                getCustomMapSqlParameterSource()
                        .addValue("affinity_group_id", affinityId)
                        .addValue("vm_id", vmId));
    }

    @Override
    public void deleteAffinityVm(Guid affinityId, Guid vmId){
        getCallsHandler().executeModification("DeleteAffinityVm",
                getCustomMapSqlParameterSource()
                        .addValue("affinity_group_id", affinityId)
                        .addValue("vm_id", vmId));
    }

    @Override
    public void insertAffinityHost(Guid affinityId, Guid vdsId){
        getCallsHandler().executeModification("InsertAffinityHost",
                getCustomMapSqlParameterSource()
                        .addValue("affinity_group_id", affinityId)
                        .addValue("vds_id", vdsId));
    }

    @Override
    public void deleteAffinityHost(Guid affinityId, Guid vdsId) {
        getCallsHandler().executeModification("DeleteAffinityHost",
                getCustomMapSqlParameterSource()
                        .addValue("affinity_group_id", affinityId)
                        .addValue("vds_id", vdsId));
    }

    @Override
    public void insertAffinityVmLabel(Guid affinityId, Guid labelId) {
        getCallsHandler().executeModification("InsertAffinityVmLabel",
                getCustomMapSqlParameterSource()
                        .addValue("affinity_group_id", affinityId)
                        .addValue("vm_label_id", labelId));
    }

    @Override
    public void deleteAffinityVmLabel(Guid affinityId, Guid labelId) {
        getCallsHandler().executeModification("DeleteAffinityVmLabel",
                getCustomMapSqlParameterSource()
                        .addValue("affinity_group_id", affinityId)
                        .addValue("vm_label_id", labelId));
    }

    @Override
    public void insertAffinityHostLabel(Guid affinityId, Guid labelId) {
        getCallsHandler().executeModification("InsertAffinityHostLabel",
                getCustomMapSqlParameterSource()
                        .addValue("affinity_group_id", affinityId)
                        .addValue("host_label_id", labelId));
    }

    @Override
    public void deleteAffinityHostLabel(Guid affinityId, Guid labelId) {
        getCallsHandler().executeModification("DeleteAffinityHostLabel",
                getCustomMapSqlParameterSource()
                        .addValue("affinity_group_id", affinityId)
                        .addValue("host_label_id", labelId));
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(AffinityGroup entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("name", entity.getName())
                .addValue("description", entity.getDescription())
                .addValue("cluster_id", entity.getClusterId())
                .addValue("vm_positive", entity.isVmPositive())
                .addValue("vm_enforcing", entity.isVmEnforcing())
                .addValue("vds_positive", entity.isVdsPositive())
                .addValue("vds_enforcing", entity.isVdsEnforcing())
                .addValue("vms_affinity_enabled", entity.isVmAffinityEnabled())
                .addValue("vds_affinity_enabled", entity.isVdsAffinityEnabled())
                .addValue("priority", entity.getPriority())
                .addValue("vm_ids", createArrayOf("uuid", entity.getVmIds().toArray()))
                .addValue("vds_ids", createArrayOf("uuid", entity.getVdsIds().toArray()))
                .addValue("vm_label_ids", createArrayOf("uuid", entity.getVmLabels().toArray()))
                .addValue("host_label_ids", createArrayOf("uuid", entity.getHostLabels().toArray()));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<AffinityGroup> createEntityRowMapper() {
        return (rs, rowNum) -> {

            AffinityGroup affinityGroup = new AffinityGroup();
            affinityGroup.setId(getGuid(rs, "id"));
            affinityGroup.setName(rs.getString("name"));
            affinityGroup.setDescription(rs.getString("description"));
            affinityGroup.setClusterId(getGuid(rs, "cluster_id"));
            affinityGroup.setVmEnforcing(rs.getBoolean("vm_enforcing"));
            affinityGroup.setVdsEnforcing(rs.getBoolean("vds_enforcing"));
            affinityGroup.setPriority(rs.getLong("priority"));

            if (rs.getBoolean("vds_affinity_enabled")) {
                if (rs.getBoolean("vds_positive")) {
                    affinityGroup.setVdsAffinityRule(EntityAffinityRule.POSITIVE);
                } else {
                    affinityGroup.setVdsAffinityRule(EntityAffinityRule.NEGATIVE);
                }
            } else {
                affinityGroup.setVdsAffinityRule(EntityAffinityRule.DISABLED);
            }

            if (rs.getBoolean("vms_affinity_enabled")) {
                if (rs.getBoolean("vm_positive")) {
                    affinityGroup.setVmAffinityRule(EntityAffinityRule.POSITIVE);
                } else {
                    affinityGroup.setVmAffinityRule(EntityAffinityRule.NEGATIVE);
                }
            } else {
                affinityGroup.setVmAffinityRule(EntityAffinityRule.DISABLED);
            }

            affinityGroup.setVmIds(readList(rs, "vm_ids", Guid::new));
            affinityGroup.setVdsIds(readList(rs, "vds_ids", Guid::new));
            affinityGroup.setVmLabels(readList(rs, "vm_label_ids", Guid::new));
            affinityGroup.setHostLabels(readList(rs, "host_label_ids", Guid::new));

            affinityGroup.setVmEntityNames(readList(rs, "vm_names"));
            affinityGroup.setVdsEntityNames(readList(rs, "vds_names"));
            affinityGroup.setVmLabelNames(readList(rs, "vm_label_names"));
            affinityGroup.setHostLabelNames(readList(rs, "host_label_names"));

            return affinityGroup;
        };
    }

    private List<String> readList(ResultSet rs, String columnLabel) throws SQLException {
        return readList(rs, columnLabel, str -> str);
    }

    private<T> List<T> readList(ResultSet rs, String columnLabel, Function<String, T> converter) throws SQLException {
        return Arrays.stream((String[]) rs.getArray(columnLabel).getArray())
                .filter(Objects::nonNull)
                .map(converter)
                .collect(Collectors.toList());
    }
}
