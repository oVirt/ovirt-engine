package org.ovirt.engine.core.bll.profiles;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;

public class GetAllDiskProfilesQuery extends QueriesCommandBase<VdcQueryParametersBase> {
    @Inject
    private DiskProfileDao diskProfileDao;

    public GetAllDiskProfilesQuery(VdcQueryParametersBase parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(diskProfileDao.getAll());
    }

}
