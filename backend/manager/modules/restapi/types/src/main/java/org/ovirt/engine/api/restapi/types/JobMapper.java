package org.ovirt.engine.api.restapi.types;

import java.sql.Date;
import java.util.Calendar;

import org.ovirt.engine.api.model.Job;
import org.ovirt.engine.api.model.JobStatus;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.job.JobExecutionStatus;

public class JobMapper {
    @Mapping(from = org.ovirt.engine.core.common.job.Job.class, to = Job.class)
    public static Job map(org.ovirt.engine.core.common.job.Job entity, Job job) {
        Job model = job != null ? job : new Job();
        model.setId(entity.getId().toString());
        model.setDescription(entity.getDescription());
        model.setStatus(mapJobStatus(entity.getStatus()));
        if (entity.getOwnerId() != null) {
            User user = new User();
            user.setId(entity.getOwnerId().toString());
            model.setOwner(user);
        }
        model.setStartTime(DateMapper.map(entity.getStartTime(), null));
        if (entity.getEndTime() != null) {
            model.setEndTime(DateMapper.map(entity.getEndTime(), null));
        }
        if (entity.getLastUpdateTime() != null) {
            model.setLastUpdated(DateMapper.map(entity.getLastUpdateTime(), null));
        }
        model.setExternal(entity.isExternal());
        model.setAutoCleared(entity.isAutoCleared());

        return model;
    }

    @Mapping(from = Job.class, to = org.ovirt.engine.core.common.job.Job.class)
    public static org.ovirt.engine.core.common.job.Job map(Job job, org.ovirt.engine.core.common.job.Job entity) {
        org.ovirt.engine.core.common.job.Job target =
                entity != null ? entity : new org.ovirt.engine.core.common.job.Job();
        target.setId(GuidUtils.asGuid(job.getId()));
        if (job.isSetDescription()) {
            target.setDescription(job.getDescription());
        }
        if (job.isSetStatus()) {
            target.setStatus(mapJobStatus(job.getStatus()));
        }
        if (job.isSetOwner()) {
            target.setOwnerId(GuidUtils.asGuid(job.getOwner().getId()));
        }
        target.setStartTime(job.isSetStartTime() ? job.getStartTime().toGregorianCalendar().getTime()
                : new Date(Calendar.getInstance().getTimeInMillis()));
        target.setEndTime(job.isSetEndTime() ? job.getEndTime().toGregorianCalendar().getTime()
                : new Date(Calendar.getInstance().getTimeInMillis()));
        target.setLastUpdateTime(job.isSetLastUpdated() ? job.getLastUpdated().toGregorianCalendar().getTime()
                : new Date(Calendar.getInstance().getTimeInMillis()));
        target.setExternal(job.isSetExternal() ? job.isExternal() : true);
        target.setAutoCleared(job.isSetAutoCleared() ? job.isAutoCleared() : true);

        return target;
    }

    private static JobStatus mapJobStatus(JobExecutionStatus status) {
        switch (status) {
        case STARTED:
            return JobStatus.STARTED;
        case FINISHED:
            return JobStatus.FINISHED;
        case FAILED:
            return JobStatus.FAILED;
        case ABORTED:
            return JobStatus.ABORTED;
        default:
            return JobStatus.UNKNOWN;
        }
    }

    public static JobExecutionStatus mapJobStatus(JobStatus status) {
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
