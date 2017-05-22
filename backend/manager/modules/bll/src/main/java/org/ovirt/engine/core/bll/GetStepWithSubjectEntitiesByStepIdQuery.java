package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.job.JobRepository;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

/**
 * Returns a Step with its subject entities.
 */
public class GetStepWithSubjectEntitiesByStepIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {

    @Inject
    private JobRepository jobRepository;

    public GetStepWithSubjectEntitiesByStepIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(jobRepository.getStep(getParameters().getId(), true));
    }
}
