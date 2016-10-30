package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VdsStaticDao;

public class GetAllHostNamesPinnedToVmByIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VdsStaticDao vdsStaticDao;

    public GetAllHostNamesPinnedToVmByIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(vdsStaticDao.getAllHostNamesPinnedToVm(getParameters().getId()));
    }

}
