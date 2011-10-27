package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

public class IsVdsWithSameIpExistsQuery<P extends IsVdsWithSameIpExistsParameters> extends QueriesCommandBase<P> {
    public IsVdsWithSameIpExistsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(VdsHandler.isVdsWithSameIpExistsStatic(getParameters().getIpAddress()));
    }
}
