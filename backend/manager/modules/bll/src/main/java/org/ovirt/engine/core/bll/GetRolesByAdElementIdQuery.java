package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class GetRolesByAdElementIdQuery<P extends MultilevelAdministrationByAdElementIdParameters>
        extends QueriesCommandBase<P> {
    public GetRolesByAdElementIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {

        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getRoleDao()
                        .getAllForAdElement(getParameters().getAdElementId()));
    }
}
