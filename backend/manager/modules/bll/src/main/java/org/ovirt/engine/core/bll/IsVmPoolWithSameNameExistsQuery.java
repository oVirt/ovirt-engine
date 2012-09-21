package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IsVmPoolWithSameNameExistsParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class IsVmPoolWithSameNameExistsQuery<P extends IsVmPoolWithSameNameExistsParameters>
        extends QueriesCommandBase<P> {
    public IsVmPoolWithSameNameExistsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getVmPoolDao().getByName(getParameters().getVmPoolName()) != null);
    }
}
