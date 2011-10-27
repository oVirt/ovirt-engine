package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetNotificationEventMapQuery<P extends VdcQueryParametersBase> extends QueriesCommandBase<P> {
    public GetNotificationEventMapQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {

        getQueryReturnValue().setReturnValue(DbFacade.getInstance().getEventDAO().getAllEventMaps());
    }
}
