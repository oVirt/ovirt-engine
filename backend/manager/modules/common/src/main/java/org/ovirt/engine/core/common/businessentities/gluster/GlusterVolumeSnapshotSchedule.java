package org.ovirt.engine.core.common.businessentities.gluster;

import java.sql.Time;
import java.util.Date;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeSnapshotSchedule implements Queryable {
    private static final long serialVersionUID = 2L;
    private Guid clusterId;
    private Guid volumeId;
    private Guid jobId;
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

    public Guid getJobId() {
        return this.jobId;
    }

    public void setJobId(Guid id) {
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
        return Objects.hash(
                clusterId,
                volumeId,
                jobId,
                snapshotNamePrefix,
                snapshotDescription,
                recurrence,
                timeZone,
                interval,
                startDate,
                executionTime,
                days,
                endByDate
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterVolumeSnapshotSchedule)) {
            return false;
        }

        GlusterVolumeSnapshotSchedule schedule = (GlusterVolumeSnapshotSchedule) obj;
        return Objects.equals(clusterId, schedule.clusterId)
                && Objects.equals(volumeId, schedule.volumeId)
                && Objects.equals(jobId, schedule.jobId)
                && Objects.equals(snapshotNamePrefix, schedule.snapshotNamePrefix)
                && Objects.equals(snapshotDescription, schedule.snapshotDescription)
                && Objects.equals(recurrence, schedule.recurrence)
                && Objects.equals(timeZone, schedule.timeZone)
                && Objects.equals(interval, schedule.interval)
                && Objects.equals(startDate, schedule.startDate)
                && Objects.equals(executionTime, schedule.executionTime)
                && Objects.equals(days, schedule.days)
                && Objects.equals(endByDate, schedule.endByDate);
    }
}
