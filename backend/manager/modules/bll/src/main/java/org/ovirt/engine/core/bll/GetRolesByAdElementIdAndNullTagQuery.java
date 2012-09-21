package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetRolesByAdElementIdAndNullTagQuery<P extends MultilevelAdministrationByAdElementIdParameters>
        extends QueriesCommandBase<P> {
    public GetRolesByAdElementIdAndNullTagQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {

        getQueryReturnValue()
                .setReturnValue(DbFacade.getInstance().getRoleDao().getForAdElement(getParameters().getAdElementId()));
    }
}
