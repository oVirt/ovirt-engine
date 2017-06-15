package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class EndExternalJobParameters extends ActionParametersBase {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Guid jobId;
    private boolean exitStatus;
    private boolean force;


    public Guid getJobId() {
        return jobId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    public boolean getStatus() {
        return exitStatus;
    }

    public void setStatus(Boolean status) {
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

    public EndExternalJobParameters(Guid jobId, boolean status, boolean force) {
        super();
        this.jobId = jobId;
        this.exitStatus = status;
        this.force = force;
    }
}
