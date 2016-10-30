package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;

public class GetCpuProfilesByCpuQosIdQuery extends QueriesCommandBase<IdQueryParameters> {
    @Inject
    CpuProfileDao cpuProfileDao;

    public GetCpuProfilesByCpuQosIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(cpuProfileDao.getAllForQos(getParameters().getId()));
    }
}
