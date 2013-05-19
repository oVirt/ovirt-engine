package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class IsVmPoolWithSameNameExistsQuery<P extends NameQueryParameters>
        extends QueriesCommandBase<P> {
    public IsVmPoolWithSameNameExistsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getVmPoolDao().getByName(getParameters().getName()) != null);
    }
}
