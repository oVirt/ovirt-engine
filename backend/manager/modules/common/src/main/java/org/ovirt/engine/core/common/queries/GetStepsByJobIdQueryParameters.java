package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetStepsByJobIdQueryParameters extends VdcQueryParametersBase {

    private static final long serialVersionUID = 758029173419093849L;

    public GetStepsByJobIdQueryParameters() {
    }

    public GetStepsByJobIdQueryParameters(Guid jobId) {
        super();
        this.jobId = jobId;
    }

    private Guid jobId;

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    public Guid getJobId() {
        return jobId;
    }

}
