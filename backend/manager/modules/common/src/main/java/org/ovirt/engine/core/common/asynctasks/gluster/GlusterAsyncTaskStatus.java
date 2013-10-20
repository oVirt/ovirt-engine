package org.ovirt.engine.core.common.asynctasks.gluster;

import org.ovirt.engine.core.common.job.JobExecutionStatus;

/**
 * This enum represents the gluster volume async task status values returned from VDSM
 */
public enum GlusterAsyncTaskStatus {
    COMPLETED("COMPLETED"),
    STARTED("STARTED"),
    STOPPED("STOPPED"),
    FAILED("FAILED"),
    UNKNOWN("UNKNOWN"),
    NOT_STARTED("NOT_STARTED"),
    IN_PROGRESS("IN_PROGRESS");

    private String statusMsg;

    private GlusterAsyncTaskStatus(String status) {
        statusMsg = status;
    }

    public String value() {
        return statusMsg;
    }

    public static GlusterAsyncTaskStatus from(String status) {
        for (GlusterAsyncTaskStatus taskStatus : values()) {
            if (taskStatus.value().equalsIgnoreCase(status)) {
                return taskStatus;
            }
        }

        return GlusterAsyncTaskStatus.UNKNOWN;
    }

    public JobExecutionStatus getJobExecutionStatus() {
        switch (this) {
            case COMPLETED:
                return JobExecutionStatus.FINISHED;
            case STARTED:
            case IN_PROGRESS:
                return JobExecutionStatus.STARTED;
            case STOPPED:
                return JobExecutionStatus.ABORTED;
            case FAILED:
                return JobExecutionStatus.FAILED;
            case UNKNOWN:
            case NOT_STARTED:
            default:
                return JobExecutionStatus.UNKNOWN;
        }
    }
}
