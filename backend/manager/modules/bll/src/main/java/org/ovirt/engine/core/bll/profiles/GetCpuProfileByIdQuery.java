package org.ovirt.engine.core.bll.profiles;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;

public class GetCpuProfileByIdQuery extends QueriesCommandBase<IdQueryParameters> {
    @Inject
    private CpuProfileDao cpuProfileDao;

    public GetCpuProfileByIdQuery(IdQueryParameters parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(cpuProfileDao.get(getParameters().getId()));
    }

}
