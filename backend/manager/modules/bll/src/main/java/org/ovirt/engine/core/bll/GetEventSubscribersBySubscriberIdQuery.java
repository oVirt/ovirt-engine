package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.dal.dbbroker.*;
import org.ovirt.engine.core.common.queries.*;

public class GetEventSubscribersBySubscriberIdQuery<P extends GetEventSubscribersBySubscriberIdParameters>
        extends QueriesCommandBase<P> {
    public GetEventSubscribersBySubscriberIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getEventDAO()
                        .getAllForSubscriber(getParameters().getSubscriberId()));
    }
}
