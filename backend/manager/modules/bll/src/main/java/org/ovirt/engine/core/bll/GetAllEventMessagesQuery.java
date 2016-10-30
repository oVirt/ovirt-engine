package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dao.AuditLogDao;

public class GetAllEventMessagesQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    @Inject
    private AuditLogDao auditLogDao;

    public GetAllEventMessagesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<AuditLog> eventsList = auditLogDao.getAll(getUserID(), getParameters().isFiltered());
        getQueryReturnValue().setReturnValue(eventsList);
    }
}
