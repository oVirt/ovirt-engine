package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.VmPoolDao;


public class GetAllVmPoolsAttachedToUserQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private VmPoolDao vmPoolDao;

    public GetAllVmPoolsAttachedToUserQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    public GetAllVmPoolsAttachedToUserQuery(P parameters) {
        this(parameters, null);
    }

    @Override
    protected void executeQueryCommand() {
        setReturnValue(vmPoolDao.getAllForUser(getUserID()));
    }
}
