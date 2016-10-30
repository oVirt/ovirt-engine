package org.ovirt.engine.core.bll.profiles;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.profiles.CpuProfileDao;

public class GetAllCpuProfilesQuery extends QueriesCommandBase<VdcQueryParametersBase> {
    @Inject
    private CpuProfileDao cpuProfileDao;

    public GetAllCpuProfilesQuery(VdcQueryParametersBase parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(cpuProfileDao.getAll());
    }

}
