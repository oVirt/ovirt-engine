package org.ovirt.engine.core.bll.scheduling.queries;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public class GetAffinityGroupByIdQuery extends QueriesCommandBase<IdQueryParameters> {

    @Inject
    private AffinityGroupDao affinityGroupDao;

    public GetAffinityGroupByIdQuery(IdQueryParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(affinityGroupDao.get(getParameters().getId()));
    }

}
