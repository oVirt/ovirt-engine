package org.ovirt.engine.core.common.businessentities.gluster;

import java.sql.Time;
import java.util.Date;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeSnapshotSchedule implements IVdcQueryable {
    private static final long serialVersionUID = 2L;
    private Guid clusterId;
    private Guid volumeId;
    private String jobId;
    private String snapshotNamePrefix;
    private String snapshotDescription;
    private GlusterVolumeSnapshotScheduleRecurrence recurrence;
    private String timeZone;
    private Integer interval;
    private Date startDate;
    private Time executionTime;
    private String days;
    private Date endByDate;

    public Guid getClusterId() {
        return this.clusterId;
    }

    public void setClusterId(Guid id) {
        this.clusterId = id;
    }

    public Guid getVolumeId() {
        return volumeId;
    }

    public void setVolumeId(Guid volumeId) {
        this.volumeId = volumeId;
    }

    public String getJobId() {
        return this.jobId;
    }

    public void setJobId(String id) {
        this.jobId = id;
    }

    @Override
    public Object getQueryableId() {
        return getJobId();
    }

    public String getSnapshotNamePrefix() {
        return this.snapshotNamePrefix;
    }

    public void setSnapshotNamePrefix(String prefix) {
        this.snapshotNamePrefix = prefix;
    }

    public String getSnapshotDescription() {
        return this.snapshotDescription;
    }

    public void setSnapshotDescription(String description) {
        this.snapshotDescription = description;
    }

    public GlusterVolumeSnapshotScheduleRecurrence getRecurrence() {
        return recurrence;
    }

    public void setRecurrence(GlusterVolumeSnapshotScheduleRecurrence recurrence) {
        this.recurrence = recurrence;
    }

    public String getTimeZone() {
        return this.timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Time getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Time executionTime) {
        this.executionTime = executionTime;
    }

    public String getDays() {
        return this.days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public Date getEndByDate() {
        return endByDate;
    }

    public void setEndByDate(Date endByDate) {
        this.endByDate = endByDate;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + (clusterId == null ? 0 : clusterId.hashCode());
        result = prime * result + (volumeId == null ? 0 : volumeId.hashCode());
        result = prime * result + (jobId == null ? 0 : jobId.hashCode());
        result = prime * result + (snapshotNamePrefix == null ? 0 : snapshotNamePrefix.hashCode());
        result = prime * result + (snapshotDescription == null ? 0 : snapshotDescription.hashCode());
        result = prime * result + (recurrence == null ? 0 : recurrence.hashCode());
        result = prime * result + (timeZone == null ? 0 : timeZone.hashCode());
        result = prime * result + (interval == null ? 0 : interval.hashCode());
        result = prime * result + (startDate == null ? 0 : startDate.hashCode());
        result = prime * result + (executionTime == null ? 0 : executionTime.hashCode());
        result = prime * result + (days == null ? 0 : days.hashCode());
        result = prime * result + (endByDate == null ? 0 : endByDate.hashCode());

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof GlusterVolumeSnapshotSchedule)) {
            return false;
        }

        GlusterVolumeSnapshotSchedule schedule = (GlusterVolumeSnapshotSchedule) obj;

        if (!(Objects.equals(clusterId, schedule.getClusterId()))) {
            return false;
        }

        if (!(Objects.equals(volumeId, schedule.getVolumeId()))) {
            return false;
        }

        if (!(Objects.equals(jobId, schedule.getJobId()))) {
            return false;
        }

        if (!Objects.equals(snapshotNamePrefix, schedule.getSnapshotNamePrefix())) {
            return false;
        }

        if (!Objects.equals(snapshotDescription, schedule.getSnapshotDescription())) {
            return false;
        }

        if (!(Objects.equals(recurrence, schedule.getRecurrence()))) {
            return false;
        }

        if (!Objects.equals(timeZone, schedule.getTimeZone())) {
            return false;
        }

        if (!(Objects.equals(interval, schedule.getInterval()))) {
            return false;
        }

        if (!(Objects.equals(startDate, schedule.getStartDate()))) {
            return false;
        }

        if (!(Objects.equals(executionTime, schedule.getExecutionTime()))) {
            return false;
        }

        if (!(Objects.equals(days, schedule.getDays()))) {
            return false;
        }

        if (!(Objects.equals(endByDate, schedule.getEndByDate()))) {
            return false;
        }

        return true;
    }
}
