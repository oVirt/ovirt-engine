package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.VdsDao;

/** A query to return all the hosts in a given data center. */
public class GetAllVdsByStoragePoolQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private VdsDao vdsDao;

    public GetAllVdsByStoragePoolQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                vdsDao.getAllForStoragePool(getParameters().getId(), getUserID(), getParameters().isFiltered()));
    }
}
