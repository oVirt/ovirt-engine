package org.ovirt.engine.api.restapi.types;

import java.sql.Date;
import java.util.Calendar;

import org.ovirt.engine.api.model.Job;
import org.ovirt.engine.api.model.JobOwner;
import org.ovirt.engine.api.model.Status;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.job.JobExecutionStatus;

public class JobMapper {

    @Mapping(from = org.ovirt.engine.core.common.job.Job.class, to = Job.class)
    public static Job map(org.ovirt.engine.core.common.job.Job entity, Job job) {

        Job model = job != null ? job : new Job();
        model.setId(entity.getId().toString());
        model.setDescription(entity.getDescription());
        model.setStatus(map(entity.getStatus(), null));
        if (entity.getOwnerId() != null) {
            JobOwner user = new JobOwner();
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
    public static org.ovirt.engine.core.common.job.Job map(Job job,
            org.ovirt.engine.core.common.job.Job entity) {
        org.ovirt.engine.core.common.job.Job target =
                entity != null ? entity : new org.ovirt.engine.core.common.job.Job();
        target.setId(GuidUtils.asGuid(job.getId()));
        if (job.isSetDescription()) {
            target.setDescription(job.getDescription());
        }
        if (job.isSetStatus()) {
            target.setStatus(map(job.getStatus(), null));
        }
        if (job.isSetOwner()) {
            target.setOwnerId(GuidUtils.asGuid(job.getOwner().getId()));
        }
        target.setStartTime(job.isSetStartTime() ? job.getStartTime().toGregorianCalendar().getTime()
                : new Date((Calendar.getInstance().getTimeInMillis())));
        target.setEndTime(job.isSetEndTime() ? job.getEndTime().toGregorianCalendar().getTime()
                : new Date((Calendar.getInstance().getTimeInMillis())));
        target.setLastUpdateTime(job.isSetLastUpdated() ? job.getLastUpdated().toGregorianCalendar().getTime()
                : new Date((Calendar.getInstance().getTimeInMillis())));
        target.setExternal(job.isSetExternal() ? job.isExternal() : true);
        target.setAutoCleared(job.isSetAutoCleared() ? job.isAutoCleared() : true);

        return target;
    }

    @Mapping(from = Status.class,
            to = org.ovirt.engine.core.common.job.JobExecutionStatus.class)
    public static org.ovirt.engine.core.common.job.JobExecutionStatus map(Status status,
            org.ovirt.engine.core.common.job.JobExecutionStatus incoming) {
        if (JobExecutionStatus.STARTED.name().equals(status.getState().toUpperCase())) {
            return JobExecutionStatus.STARTED;
        }
        if (JobExecutionStatus.FINISHED.name().equals(status.getState().toUpperCase())) {
            return JobExecutionStatus.FINISHED;
        }
        if (JobExecutionStatus.ABORTED.name().equals(status.getState().toUpperCase())) {
            return JobExecutionStatus.ABORTED;
        }
        if (JobExecutionStatus.FAILED.name().equals(status.getState().toUpperCase())) {
            return JobExecutionStatus.FAILED;
        }
        return JobExecutionStatus.UNKNOWN;
    }


    @Mapping(from = JobExecutionStatus.class,
            to = Status.class)
    public static Status map(JobExecutionStatus status,
            Status incoming) {
        Status st = new Status();
        if (JobExecutionStatus.STARTED == status) {
            st.setState(JobExecutionStatus.STARTED.name());
            return st;
        }
        if (JobExecutionStatus.FINISHED == status) {
            st.setState(JobExecutionStatus.FINISHED.name());
            return st;
        }
        if (JobExecutionStatus.ABORTED == status) {
            st.setState(JobExecutionStatus.ABORTED.name());
            return st;
        }
        if (JobExecutionStatus.FAILED == status) {
            st.setState(JobExecutionStatus.FAILED.name());
            return st;
        }
        st.setState(JobExecutionStatus.UNKNOWN.name());
        return st;
    }
}
