package org.ovirt.engine.core.dao.profiles;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class CpuProfileDaoImpl extends ProfileBaseDaoImpl<CpuProfile> implements CpuProfileDao {
    private static final CpuProfileDaoDbFacadaeImplMapper MAPPER = new CpuProfileDaoDbFacadaeImplMapper();

    public CpuProfileDaoImpl() {
        super("CpuProfile");
    }

    @Override
    public List<CpuProfile> getAllForCluster(Guid clusterId) {
        return getAllForCluster(clusterId, null, false, ActionGroup.ASSIGN_CPU_PROFILE);
    }

    @Override
    public List<CpuProfile> getAllForCluster(Guid clusterId, Guid userId, boolean isFiltered, ActionGroup actionGroup) {
        return getCallsHandler().executeReadList("GetCpuProfilesByClusterId",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("cluster_id", clusterId)
                        .addValue("user_id", userId)
                        .addValue("is_filtered", isFiltered)
                        .addValue("action_group_id", actionGroup.getId()));
    }

    @Override
    public List<CpuProfile> getAllForQos(Guid qosId) {
        return getCallsHandler().executeReadList("GetCpuProfilesByQosId",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("qos_id", qosId));
    }

    @Override
    protected RowMapper<CpuProfile> createEntityRowMapper() {
        return MAPPER;
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(CpuProfile obj) {
        MapSqlParameterSource map = super.createFullParametersMapper(obj);
        map.addValue("cluster_id", obj.getClusterId());
        return map;
    }

    protected static class CpuProfileDaoDbFacadaeImplMapper extends ProfileBaseDaoFacadaeImplMapper<CpuProfile> {

        @Override
        protected CpuProfile createProfileEntity(ResultSet rs) throws SQLException {
            CpuProfile cpuProfile = new CpuProfile();
            cpuProfile.setClusterId(getGuid(rs, "cluster_id"));
            return cpuProfile;
        }

    }
}
