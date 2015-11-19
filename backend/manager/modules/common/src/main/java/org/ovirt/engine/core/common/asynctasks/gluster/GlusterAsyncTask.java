package org.ovirt.engine.core.common.asynctasks.gluster;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class GlusterAsyncTask implements Serializable{

    private static final long serialVersionUID = 5165089908032934194L;

    private Guid taskId;
    private JobExecutionStatus status;
    private GlusterTaskType type;
    private String message;
    private Guid stepId;
    private GlusterTaskParameters taskParameters;
    private Guid jobId;
    private JobExecutionStatus jobStatus;

    public GlusterAsyncTask(){

    }

    public Guid getTaskId() {
        return taskId;
    }
    public void setTaskId(Guid taskId) {
        this.taskId = taskId;
    }
    public JobExecutionStatus getStatus() {
        return status;
    }
    public void setStatus(JobExecutionStatus status) {
        this.status = status;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public GlusterTaskType getType() {
        return type;
    }
    public void setType(GlusterTaskType type) {
        this.type = type;
    }

    public Guid getStepId() {
        return stepId;
    }

    public void setStepId(Guid stepId) {
        this.stepId = stepId;
    }

    public GlusterTaskParameters getTaskParameters() {
        return taskParameters;
    }

    public void setTaskParameters(GlusterTaskParameters taskParameters) {
        this.taskParameters = taskParameters;
    }

    public Guid getJobId() {
        return jobId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    public JobExecutionStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobExecutionStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                message,
                status,
                taskId,
                type,
                stepId,
                jobId,
                jobStatus
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterAsyncTask)) {
            return false;
        }
        GlusterAsyncTask other = (GlusterAsyncTask) obj;
        return Objects.equals(message, other.message)
                && Objects.equals(taskId, other.taskId)
                && type == other.type
                && status == other.status
                && Objects.equals(stepId, other.stepId)
                && Objects.equals(jobId, other.jobId)
                && jobStatus == other.jobStatus;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("taskId", getTaskId())
                .append("type", getType())
                .append("status", getStatus())
                .build();
    }


}
