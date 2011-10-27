package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetUserTabsQuery<P extends MultilevelAdministrationByAdElementIdParameters>
        extends QueriesCommandBase<P> {
    public GetUserTabsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        Guid id = getParameters().getAdElementId();

        /*
         * java.util.ArrayList<user_action_map> list =
         * DbFacade.getInstance().GetActionsWithoutAdTagByUserId(id);
         * java.util.ArrayList<user_action_map> retVal = new
         * java.util.ArrayList<user_action_map>();
         *
         * for (user_action_map item : list) { if (item.getactionId() >=
         * VdcActionType.DataCenters.getValue() && item.getactionId() <=
         * VdcActionType.Monitor.getValue()) { retVal.add(item); } }
         *
         * getQueryReturnValue().setReturnValue(retVal);
         */
    }
}
