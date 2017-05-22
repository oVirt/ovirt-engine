package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.PermissionDao;

public class GetPermissionsByAdElementIdQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private PermissionDao permissionDao;

    public GetPermissionsByAdElementIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                permissionDao.getAllForAdElement
                        (getParameters().getId(), getEngineSessionSeqId(), getParameters().isFiltered()));
    }
}
