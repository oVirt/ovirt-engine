package org.ovirt.engine.core.bll;

import java.util.List;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.queries.*;

// not in use
public class GetAllEventMessagesQuery<P extends GetEventMessagesParameters> extends QueriesCommandBase<P> {
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
