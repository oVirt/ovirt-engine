package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.Date;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.businessentities.comparators.BusinessEntityComparator;
import org.ovirt.engine.core.compat.Guid;

public class GlusterJobDetails implements Queryable, BusinessEntity<Guid>, Comparable<GlusterJobDetails> {


    private Guid jobId;

    private String jobName;

    private String jobClassName;

    private String cronSchedule;

    private Date startDate;

    private Date endDate;

    private String timeZone;

    @Override
    public int compareTo(GlusterJobDetails obj) {
        return BusinessEntityComparator.<GlusterJobDetails, Guid> newInstance().compare(this, obj);
    }

    public Guid getJobId() {
        return jobId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobClassName() {
        return jobClassName;
    }

    public void setJobClassName(String jobClassName) {
        this.jobClassName = jobClassName;
    }

    public String getCronSchedule() {
        return cronSchedule;
    }

    public void setCronSchedule(String cronSchedule) {
        this.cronSchedule = cronSchedule;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public Guid getId() {
        if (jobId == null) {
            return jobId = Guid.newGuid();
        }
        return jobId;
    }

    @Override
    public void setId(Guid jobId) {
        this.jobId = jobId;

    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                jobId,
                jobName,
                jobClassName,
                cronSchedule,
                startDate,
                endDate,
                timeZone
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterJobDetails)) {
            return false;
        }

        GlusterJobDetails other = (GlusterJobDetails) obj;
        return Objects.equals(jobId, other.jobId)
                && Objects.equals(jobName, other.jobName)
                && Objects.equals(jobClassName, other.jobClassName)
                && Objects.equals(cronSchedule, other.cronSchedule)
                && Objects.equals(startDate, other.startDate)
                && Objects.equals(endDate, other.endDate)
                && Objects.equals(timeZone, other.timeZone);
    }

}
