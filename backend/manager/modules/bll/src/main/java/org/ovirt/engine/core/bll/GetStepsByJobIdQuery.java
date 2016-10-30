package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.GetStepsByJobIdQueryParameters;
import org.ovirt.engine.core.dao.StepDao;

public class GetStepsByJobIdQuery <P extends GetStepsByJobIdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private StepDao stepDao;

    public GetStepsByJobIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(stepDao.getStepsByJobId(getParameters().getJobId()));
    }
}
