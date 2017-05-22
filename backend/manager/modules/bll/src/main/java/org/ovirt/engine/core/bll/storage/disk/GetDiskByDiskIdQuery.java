package org.ovirt.engine.core.bll.storage.disk;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.DiskDao;

public class GetDiskByDiskIdQuery <P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private DiskDao diskDao;

    public GetDiskByDiskIdQuery(P parameters, EngineContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(diskDao.get(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
