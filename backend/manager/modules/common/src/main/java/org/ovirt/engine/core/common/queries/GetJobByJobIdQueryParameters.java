package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetJobByJobIdQueryParameters  extends VdcQueryParametersBase {

    private static final long serialVersionUID = 758029173419093849L;

    private Guid jobId;

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    public Guid getJobId() {
        return jobId;
    }

}
