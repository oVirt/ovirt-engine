package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.IsStoragePoolWithSameNameExistParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;


public class IsStoragePoolWithSameNameExistQuery<P extends IsStoragePoolWithSameNameExistParameters> extends QueriesCommandBase<P> {
    public IsStoragePoolWithSameNameExistQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(DbFacade.getInstance()
                .getStoragePoolDAO()
                .getByName(getParameters().getStoragePoolName()) != null);
    }
}
