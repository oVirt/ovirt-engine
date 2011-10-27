package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

public class IsVdsWithSameHostExistQuery<P extends IsVdsWithSameHostExistParameters> extends QueriesCommandBase<P> {
    public IsVdsWithSameHostExistQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(VdsHandler.isVdsWithSameHostExistStatic(getParameters().getHostName()));
    }
}
