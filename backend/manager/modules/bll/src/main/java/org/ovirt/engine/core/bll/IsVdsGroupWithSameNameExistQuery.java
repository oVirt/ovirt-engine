package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.dal.dbbroker.*;

public class IsVdsGroupWithSameNameExistQuery<P extends IsVdsGroupWithSameNameExistParameters>
        extends QueriesCommandBase<P> {
    public IsVdsGroupWithSameNameExistQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                DbFacade.getInstance().getVdsGroupDao().getByName(getParameters().getName()) != null);
    }
}
