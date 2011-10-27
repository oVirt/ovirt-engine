package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.GetEventNotificationMethodByTypeParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetEventNotificationMethodByTypeQuery<P extends GetEventNotificationMethodByTypeParameters>
        extends QueriesCommandBase<P> {
    public GetEventNotificationMethodByTypeQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        String method_typeField = getParameters().getMethodType().name();

        getQueryReturnValue()
                .setReturnValue(DbFacade.getInstance().getEventDAO().getEventNotificationMethodsByType(method_typeField));
    }
}
