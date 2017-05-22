package org.ovirt.engine.core.bll.scheduling.queries;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;

public class GetAffinityGroupsByVmIdQuery extends QueriesCommandBase<IdQueryParameters> {

    @Inject
    private AffinityGroupDao affinityGroupDao;

    public GetAffinityGroupsByVmIdQuery(IdQueryParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(affinityGroupDao.getAllAffinityGroupsByVmId(getParameters().getId()));
    }

}
