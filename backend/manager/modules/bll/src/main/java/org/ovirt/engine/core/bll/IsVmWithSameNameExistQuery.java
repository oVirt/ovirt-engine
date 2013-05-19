package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.NameQueryParameters;

public class IsVmWithSameNameExistQuery<P extends NameQueryParameters> extends QueriesCommandBase<P> {
    public IsVmWithSameNameExistQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(isVmWithSameNameExistStatic());
    }

    protected boolean isVmWithSameNameExistStatic() {
        return VmHandler.isVmWithSameNameExistStatic(getParameters().getName());
    }
}
