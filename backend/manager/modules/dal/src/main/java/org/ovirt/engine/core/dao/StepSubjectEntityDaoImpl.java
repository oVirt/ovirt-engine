package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.job.StepSubjectEntity;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class StepSubjectEntityDaoImpl extends BaseDao implements StepSubjectEntityDao {

    @Override
    public List<StepSubjectEntity> getStepSubjectEntitiesByStepId(Guid stepId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("step_id", stepId);

        return getCallsHandler().executeReadList("GetStepSubjectEntitiesByStepId",
                        StepSubjectEntityRowMapper.getInstance(),
                        parameterSource);
    }

    @Override
    public void remove(Guid entityId, Guid stepId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("entity_id", entityId)
                .addValue("step_id", stepId);
        getCallsHandler().executeModification("DeleteStepSubjectEntity", parameterSource);
    }

    public void saveAll(Collection<StepSubjectEntity> entities) {
        if (entities.isEmpty()) {
            return;
        }

        getCallsHandler().executeStoredProcAsBatch("InsertStepSubjectEntity", entities, getMapper());
    }

    private MapSqlParameterMapper<StepSubjectEntity> getMapper() {
        return (StepSubjectEntity stepSubjectEntity) -> getCustomMapSqlParameterSource()
                .addValue("step_id", stepSubjectEntity.getStepId())
                .addValue("entity_id", stepSubjectEntity.getEntityId())
                .addValue("step_entity_weight", stepSubjectEntity.getStepEntityWeight())
                .addValue("entity_type", EnumUtils.nameOrNull(stepSubjectEntity.getEntityType()));
    }

    public static class StepSubjectEntityRowMapper extends AbstractSubjectEntityRowMapper<StepSubjectEntity> {

        private static final StepSubjectEntityRowMapper instance = new StepSubjectEntityRowMapper();

        @Override
        public StepSubjectEntity mapRow(ResultSet rs, int rowNum) throws SQLException {
            StepSubjectEntity stepSubjectEntity = super.mapRow(rs, rowNum);
            stepSubjectEntity.setStepEntityWeight(getInteger(rs, "step_entity_weight"));
            stepSubjectEntity.setStepId(getGuid(rs, "step_id"));
            return stepSubjectEntity;
        }

        protected StepSubjectEntity createSubjectEntity() {
            return new StepSubjectEntity();
        }

        public static StepSubjectEntityRowMapper getInstance() {
            return instance;
        }
    }
}
