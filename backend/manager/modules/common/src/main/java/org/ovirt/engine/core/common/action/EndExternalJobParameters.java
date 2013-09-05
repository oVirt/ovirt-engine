package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.compat.Guid;

public class EndExternalJobParameters extends VdcActionParametersBase {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Guid jobId;
    private JobExecutionStatus exitStatus;
    private boolean force;


    public Guid getJobId() {
        return jobId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    public JobExecutionStatus getStatus() {
        return exitStatus;
    }

    public void setStatus(JobExecutionStatus status) {
        this.exitStatus = status;
    }

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public EndExternalJobParameters() {
    }

    public EndExternalJobParameters(Guid jobId, JobExecutionStatus status, boolean force) {
        super();
        this.jobId = jobId;
        this.exitStatus = status;
        this.force = force;
    }
}
