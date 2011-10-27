package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

public class IsVdsWithSameNameExistQuery<P extends IsVdsWithSameNameExistParameters> extends QueriesCommandBase<P> {
    public IsVdsWithSameNameExistQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(VdsHandler.isVdsWithSameNameExistStatic(getParameters().getVmName()));
    }
}
