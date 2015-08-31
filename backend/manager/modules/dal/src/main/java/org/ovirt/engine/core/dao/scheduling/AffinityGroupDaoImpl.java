package org.ovirt.engine.core.dao.scheduling;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.dao.DefaultGenericDao;
import org.ovirt.engine.core.utils.GuidUtils;
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
                .addValue("positive", entity.isPositive())
                .addValue("enforcing", entity.isEnforcing())
                .addValue("vm_ids",
                        entity.getEntityIds() == null ? StringUtils.EMPTY : StringUtils.join(entity.getEntityIds(), SEPARATOR));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<AffinityGroup> createEntityRowMapper() {
        return AffinityGropupRowMapper.instance;
    }

    private static class AffinityGropupRowMapper implements RowMapper<AffinityGroup> {
        public static AffinityGropupRowMapper instance = new AffinityGropupRowMapper();

        @Override
        public AffinityGroup mapRow(ResultSet rs, int arg1) throws SQLException {
            AffinityGroup affinityGroup = new AffinityGroup();
            affinityGroup.setId(getGuid(rs, "id"));
            affinityGroup.setName(rs.getString("name"));
            affinityGroup.setDescription(rs.getString("description"));
            affinityGroup.setClusterId(getGuid(rs, "cluster_id"));
            affinityGroup.setPositive(rs.getBoolean("positive"));
            affinityGroup.setEnforcing(rs.getBoolean("enforcing"));
            affinityGroup.setEntityIds(GuidUtils.getGuidListFromString(rs.getString("vm_ids")));
            affinityGroup.setEntityNames(split(rs.getString("vm_names")));

            return affinityGroup;
        }
    }

}
