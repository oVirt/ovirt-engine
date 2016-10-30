package org.ovirt.engine.core.bll.profiles;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;

public class GetDiskProfileByIdQuery extends QueriesCommandBase<IdQueryParameters> {
    @Inject
    private DiskProfileDao diskProfileDao;

    public GetDiskProfileByIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(diskProfileDao.get(getParameters().getId()));
    }

}
