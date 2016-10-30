package org.ovirt.engine.core.bll.profiles;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;

public class GetAllDiskProfilesQuery extends QueriesCommandBase<VdcQueryParametersBase> {
    @Inject
    private DiskProfileDao diskProfileDao;

    public GetAllDiskProfilesQuery(VdcQueryParametersBase parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(diskProfileDao.getAll());
    }

}
