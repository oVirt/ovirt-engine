package org.ovirt.engine.api.restapi.types;

import java.sql.Date;
import java.util.Calendar;

import org.ovirt.engine.api.model.ExternalSystemType;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Job;
import org.ovirt.engine.api.model.Step;
import org.ovirt.engine.api.model.StepEnum;
import org.ovirt.engine.api.model.StepStatus;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.api.restapi.utils.TypeConversionHelper;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.job.JobExecutionStatus;

public class StepMapper {

    @Mapping(from = org.ovirt.engine.core.common.job.Step.class, to = Step.class)
    public static Step map(org.ovirt.engine.core.common.job.Step entity,
            Step step) {

        Step model = step != null ? step : new Step();
        model.setId(entity.getId().toString());
        if (entity.getParentStepId() != null) {
            Step parentStep = new Step();
            parentStep.setId(entity.getParentStepId().toString());
            model.setParentStep(parentStep);
        }
        Job job = new Job();
        job.setId(entity.getJobId().toString());
        model.setJob(job);
        model.setType(map(entity.getStepType()));
        model.setDescription(entity.getDescription());
        model.setNumber(entity.getStepNumber());
        model.setStatus(mapStepStatus(entity.getStatus()));
        model.setProgress(entity.getProgress());
        model.setStartTime(DateMapper.map(entity.getStartTime(), null));
        if (entity.getEndTime() != null) {
            model.setEndTime(TypeConversionHelper.toXMLGregorianCalendar(entity.getEndTime(), null));
        }
        model.setExternal(entity.isExternal());
        if (entity.getExternalSystem() != null && entity.getExternalSystem().getType() != null) {
            model.setExternalType(map(entity.getExternalSystem().getType()));
        }

        mapStepSubjectEntities(entity, model);

        return model;
    }

    private static void mapStepSubjectEntities(org.ovirt.engine.core.common.job.Step entity,
                                        Step model) {
        if (entity.getSubjectEntities() != null) {
            for (SubjectEntity subjectEntity : entity.getSubjectEntities()) {
                if (subjectEntity.getEntityType() == VdcObjectType.EXECUTION_HOST) {
                    model.setExecutionHost(new Host());
                    model.getExecutionHost().setId(subjectEntity.getEntityId().toString());
                    break;
                }
            }
        }
    }

    @Mapping(from = Step.class, to = org.ovirt.engine.core.common.job.Step.class)
    public static org.ovirt.engine.core.common.job.Step map(Step step,
            org.ovirt.engine.core.common.job.Step entity) {
        org.ovirt.engine.core.common.job.Step target =
                entity != null ? entity : new org.ovirt.engine.core.common.job.Step();
        target.setId(GuidUtils.asGuid(step.getId()));
        if (step.isSetParentStep()) {
            target.setParentStepId(GuidUtils.asGuid(step.getParentStep().getId()));
        }
        target.setJobId(GuidUtils.asGuid(step.getJob().getId()));
        if (step.isSetType()) {
            target.setStepType(map(step.getType()));
        }
        if (step.isSetDescription()) {
            target.setDescription(step.getDescription());
        }
        if (step.isSetNumber()) {
            target.setStepNumber(step.getNumber());
        }
        if (step.isSetStatus()) {
            target.setStatus(mapStepStatus(step.getStatus()));
        }
        if (step.isSetProgress()) {
            target.setProgress(step.getProgress());
        }
        target.setStartTime(step.isSetStartTime() ? step.getStartTime().toGregorianCalendar().getTime()
                : new Date(Calendar.getInstance().getTimeInMillis()));
        target.setEndTime(step.isSetEndTime() ? step.getEndTime().toGregorianCalendar().getTime()
                : new Date(Calendar.getInstance().getTimeInMillis()));
        target.setExternal(step.isSetExternal() ? step.isExternal() : true);
        return target;
    }

    @Mapping(from = StepEnum.class,
            to = org.ovirt.engine.core.common.job.StepEnum.class)
    public static org.ovirt.engine.core.common.job.StepEnum map(StepEnum type) {
        if (StepEnum.VALIDATING.name().equals(type.name().toUpperCase())) {
            return org.ovirt.engine.core.common.job.StepEnum.VALIDATING;
        }
        if (StepEnum.EXECUTING.name().equals(type.name().toUpperCase())) {
            return org.ovirt.engine.core.common.job.StepEnum.EXECUTING;
        }
        if (StepEnum.FINALIZING.name().equals(type.name().toUpperCase())) {
            return org.ovirt.engine.core.common.job.StepEnum.FINALIZING;
        }
        if (StepEnum.REBALANCING_VOLUME.name().equals(type.name().toUpperCase())) {
            return org.ovirt.engine.core.common.job.StepEnum.REBALANCING_VOLUME;
        }
        if (StepEnum.REMOVING_BRICKS.name().equals(type.name().toUpperCase())) {
            return org.ovirt.engine.core.common.job.StepEnum.REMOVING_BRICKS;
        }
        return org.ovirt.engine.core.common.job.StepEnum.UNKNOWN;
    }

    @Mapping(from = org.ovirt.engine.core.common.job.StepEnum.class,
            to = StepEnum.class)
    public static StepEnum map(org.ovirt.engine.core.common.job.StepEnum type) {
        if (StepEnum.VALIDATING.name().equals(type.name())) {
            return StepEnum.VALIDATING;
        }
        if (StepEnum.EXECUTING.name().equals(type.name())) {
            return StepEnum.EXECUTING;
        }
        if (StepEnum.FINALIZING.name().equals(type.name())) {
            return StepEnum.FINALIZING;
        }
        if (StepEnum.REBALANCING_VOLUME.name().equals(type.name())) {
            return StepEnum.REBALANCING_VOLUME;
        }
        if (StepEnum.REMOVING_BRICKS.name().equals(type.name())) {
            return StepEnum.REMOVING_BRICKS;
        }
        return StepEnum.UNKNOWN;
    }

    @Mapping(from = org.ovirt.engine.core.common.job.ExternalSystemType.class,
            to = ExternalSystemType.class)
    public static ExternalSystemType map(org.ovirt.engine.core.common.job.ExternalSystemType type) {
        switch (type) {
        case VDSM:
            return ExternalSystemType.VDSM;
        case GLUSTER:
            return ExternalSystemType.GLUSTER;
        default:
            return null;
        }
    }

    private static StepStatus mapStepStatus(JobExecutionStatus status) {
        switch (status) {
        case STARTED:
            return StepStatus.STARTED;
        case FINISHED:
            return StepStatus.FINISHED;
        case FAILED:
            return StepStatus.FAILED;
        case ABORTED:
            return StepStatus.ABORTED;
        default:
            return StepStatus.UNKNOWN;
        }
    }

    private static JobExecutionStatus mapStepStatus(StepStatus status) {
        switch (status) {
        case STARTED:
            return JobExecutionStatus.STARTED;
        case FINISHED:
            return JobExecutionStatus.FINISHED;
        case FAILED:
            return JobExecutionStatus.FAILED;
        case ABORTED:
            return JobExecutionStatus.ABORTED;
        default:
            return JobExecutionStatus.UNKNOWN;
        }
    }
}
