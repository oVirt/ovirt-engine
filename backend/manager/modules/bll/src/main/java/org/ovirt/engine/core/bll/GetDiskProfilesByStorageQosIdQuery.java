package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;

public class GetDiskProfilesByStorageQosIdQuery extends QueriesCommandBase<IdQueryParameters> {
    @Inject
    private DiskProfileDao diskProfileDao;

    public GetDiskProfilesByStorageQosIdQuery(IdQueryParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(diskProfileDao.getAllForQos(getParameters().getId()));
    }

}
