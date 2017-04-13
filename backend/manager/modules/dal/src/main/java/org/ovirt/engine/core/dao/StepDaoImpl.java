package org.ovirt.engine.core.dao;

import java.util.Date;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.job.ExternalSystemType;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class StepDaoImpl extends DefaultGenericDao<Step, Guid> implements StepDao {

    private static final RowMapper<Step> stepRowMapper = (rs, rowNum) -> {
        Step step = new Step();
        step.setId(getGuidDefaultEmpty(rs, "step_id"));
        step.setParentStepId(getGuid(rs, "parent_step_id"));
        step.setJobId(getGuidDefaultEmpty(rs, "job_id"));
        step.setStepType(StepEnum.valueOf(rs.getString("step_type")));
        step.setDescription(rs.getString("description"));
        step.setStepNumber(rs.getInt("step_number"));
        step.setStatus(JobExecutionStatus.valueOf(rs.getString("status")));
        step.setStartTime(DbFacadeUtils.fromDate(rs.getTimestamp("start_time")));
        step.setEndTime(DbFacadeUtils.fromDate(rs.getTimestamp("end_time")));
        step.setCorrelationId(rs.getString("correlation_id"));
        step.setProgress(getInteger(rs, "progress"));
        step.getExternalSystem().setId(getGuid(rs, "external_id"));
        step.getExternalSystem().setType(ExternalSystemType.safeValueOf(rs.getString("external_system_type")));
        step.setExternal(rs.getBoolean("is_external"));
        return step;
    };

    public StepDaoImpl() {
        super("Step");
        setProcedureNameForGetAll("GetAllSteps");
    }

    @Override
    public boolean exists(Guid id) {
        return get(id) != null;
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("step_id", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(Step entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("parent_step_id", entity.getParentStepId())
                .addValue("job_id", entity.getJobId())
                .addValue("step_type", EnumUtils.nameOrNull(entity.getStepType()))
                .addValue("description", entity.getDescription())
                .addValue("step_number", entity.getStepNumber())
                .addValue("status", EnumUtils.nameOrNull(entity.getStatus()))
                .addValue("progress", entity.getProgress())
                .addValue("start_time", entity.getStartTime())
                .addValue("end_time", entity.getEndTime())
                .addValue("correlation_id", entity.getCorrelationId())
                .addValue("external_id", entity.getExternalSystem().getId())
                .addValue("external_system_type", EnumUtils.nameOrNull(entity.getExternalSystem().getType()))
                .addValue("is_external", entity.isExternal());
    }

    @Override
    protected RowMapper<Step> createEntityRowMapper() {
        return stepRowMapper;
    }

    @Override
    public List<Step> getStepsByJobId(Guid jobId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("job_id", jobId);
        return getCallsHandler().executeReadList("GetStepsByJobId", createEntityRowMapper(), parameterSource);
    }

    @Override
    public List<Step> getStepsByParentStepId(Guid parentStepId) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("parent_step_id", parentStepId);
        return getCallsHandler().executeReadList("GetStepsByParentStepId", createEntityRowMapper(), parameterSource);
    }

    @Override
    public void updateJobStepsCompleted(Guid jobId, JobExecutionStatus status, Date endTime) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("job_id", jobId)
                        .addValue("status", status.name())
                        .addValue("end_time", endTime);
        getCallsHandler().executeModification("updateJobStepsCompleted", parameterSource);

    }

    @Override
    public void updateStepProgress(Guid stepId, Integer progress) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("step_id", stepId)
                        .addValue("progress", progress);
        getCallsHandler().executeModification("updateStepProgress", parameterSource);
    }

    @Override
    public List<Step> getStepsByExternalId(Guid externalId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("external_id", externalId);
        return getCallsHandler().executeReadList("GetStepsByExternalTaskId", createEntityRowMapper(), parameterSource);
    }

    @Override
    public List<Guid> getExternalIdsForRunningSteps(ExternalSystemType systemType) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("external_system_type", systemType.name())
                                                .addValue("status", JobExecutionStatus.STARTED.name());
        return getCallsHandler().executeReadList("GetExternalIdsFromSteps", createGuidMapper(), parameterSource);
    }

    @Override
    public List<Step> getStartedStepsByStepSubjectEntity(SubjectEntity subjectEntity) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource().addValue("entity_type", subjectEntity.getEntityType().name())
                        .addValue("entity_id", subjectEntity.getEntityId())
                        .addValue("status", JobExecutionStatus.STARTED.name());
        return getCallsHandler().executeReadList("GetStepsForEntityByStatus", createEntityRowMapper(), parameterSource);
    }
}
