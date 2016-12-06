package org.ovirt.engine.core.common.utils;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class PersistedCommandContext implements Serializable {

    private Guid jobId;
    private Guid stepId;
    private ExecutionMethod executionMethod;
    private boolean monitored;
    private boolean shouldEndJob;
    private boolean tasksMonitored = true;
    private boolean completed;
    private boolean jobRequired;

    public PersistedCommandContext() {
        // empty
    }

    public Guid getJobId() {
        return jobId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    public Guid getStepId() {
        return stepId;
    }

    public void setStepId(Guid stepId) {
        this.stepId = stepId;
    }

    public ExecutionMethod getExecutionMethod() {
        return executionMethod;
    }

    public void setExecutionMethod(ExecutionMethod executionMethod) {
        this.executionMethod = executionMethod;
    }

    public boolean isMonitored() {
        return monitored;
    }

    public void setMonitored(boolean monitored) {
        this.monitored = monitored;
    }

    public boolean shouldEndJob() {
        return shouldEndJob;
    }

    public void setShouldEndJob(boolean shouldEndJob) {
        this.shouldEndJob = shouldEndJob;
    }

    public boolean isTasksMonitored() {
        return tasksMonitored;
    }

    public void setTasksMonitored(boolean tasksMonitored) {
        this.tasksMonitored = tasksMonitored;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isJobRequired() {
        return jobRequired;
    }

    public void setJobRequired(boolean jobRequired) {
        this.jobRequired = jobRequired;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("jobId", jobId)
                .append("stepId", stepId)
                .append("executionMethod", executionMethod)
                .append("monitored", monitored)
                .append("shouldEndJob", shouldEndJob)
                .append("tasksMonitored", tasksMonitored)
                .append("completed", completed)
                .append("jobRequired", jobRequired).build();
    }
}
