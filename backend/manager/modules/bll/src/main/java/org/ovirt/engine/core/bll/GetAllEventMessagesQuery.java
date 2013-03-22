package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;

public class GetAllEventMessagesQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetAllEventMessagesQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<AuditLog> eventsList = getDbFacade()
                .getAuditLogDao().getAll(getUserID(), getParameters().isFiltered());
        getQueryReturnValue().setReturnValue(eventsList);
    }
}
