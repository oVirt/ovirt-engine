package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.GetAuditLogByIdParameters;
import org.ovirt.engine.core.dao.AuditLogDao;

public class GetAuditLogByIdQuery <P extends GetAuditLogByIdParameters> extends QueriesCommandBase<P>{
    @Inject
    private AuditLogDao auditLogDao;

    public GetAuditLogByIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    /** Actually executes the query, and stores the result in {@link #getQueryReturnValue()} */
    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(auditLogDao.get(getParameters().getId()));
    }
}
