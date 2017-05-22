package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.queries.GetStepsWithSubjectEntitiesByJobIdQueryParameters;
import org.ovirt.engine.core.dao.StepDao;
import org.ovirt.engine.core.dao.StepSubjectEntityDao;

public class GetStepsWithSubjectEntitiesByJobIdQuery<P extends GetStepsWithSubjectEntitiesByJobIdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private StepDao stepDao;

    @Inject
    private StepSubjectEntityDao stepSubjectEntityDao;

    public GetStepsWithSubjectEntitiesByJobIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<Step> steps = stepDao.getStepsByJobId(getParameters().getJobId());
        steps.forEach(s -> s.setSubjectEntities(stepSubjectEntityDao.getStepSubjectEntitiesByStepId(s.getId())));
        getQueryReturnValue().setReturnValue(steps);
    }
}
