package org.ovirt.engine.core.dao.scheduling;

import java.util.Arrays;
import java.util.List;
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
    public List<AffinityGroup> getAllAffinityGroupsByVmId(Guid vmId) {
        return getCallsHandler().executeReadList("getAllAffinityGroupsByVmId",
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
    public void removeVmFromAffinityGroups(Guid vmId) {
        getCallsHandler().executeModification("RemoveVmFromAffinityGroups",
                getCustomMapSqlParameterSource().addValue("vm_id", vmId));
    }

    @Override
    public void removeVdsFromAffinityGroups(Guid vdsId) {
        getCallsHandler().executeModification("RemoveVdsFromAffinityGroups",
                getCustomMapSqlParameterSource().addValue("vds_id", vdsId));
    }

    @Override
    public List<AffinityGroup> getPositiveEnforcingAffinityGroupsByRunningVmsOnVdsId(Guid vdsId) {
        return getCallsHandler().executeReadList("getPositiveEnforcingAffinityGroupsByRunningVmsOnVdsId",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("vds_id", vdsId));
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
                .addValue("vm_ids", createArrayOf("uuid", entity.getVmIds().toArray()))
                .addValue("vds_ids", createArrayOf("uuid", entity.getVdsIds().toArray()));
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

            String[] rawUuids = (String[]) rs.getArray("vm_ids").getArray();
            List<String> vms = Arrays.asList(rawUuids);
            rawUuids = (String[]) rs.getArray("vds_ids").getArray();
            List<String> hosts = Arrays.asList(rawUuids);
            List<String> vmNames = Arrays.asList((String[]) rs.getArray("vm_names").getArray());
            List<String> vdsNames = Arrays.asList((String[]) rs.getArray("vds_names").getArray());

            affinityGroup.setVmIds(vms.stream().filter(v -> v != null).map(Guid::new).collect(Collectors.toList()));
            affinityGroup.setVdsIds(hosts.stream().filter(v -> v != null).map(Guid::new).collect(Collectors.toList()));
            affinityGroup.setVmEntityNames(vmNames.stream().filter(v -> v != null).collect(Collectors.toList()));
            affinityGroup.setVdsEntityNames(vdsNames.stream().filter(v -> v != null).collect(Collectors.toList()));

            return affinityGroup;
        };
    }
}
