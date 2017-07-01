package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetStepsWithSubjectEntitiesByJobIdQueryParameters extends QueryParametersBase {

    private static final long serialVersionUID = 758029173419093849L;

    public GetStepsWithSubjectEntitiesByJobIdQueryParameters() {
    }

    public GetStepsWithSubjectEntitiesByJobIdQueryParameters(Guid jobId) {
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
