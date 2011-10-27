package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.queries.*;

public class IsVmWithSameNameExistQuery<P extends IsVmWithSameNameExistParameters> extends QueriesCommandBase<P> {
    public IsVmWithSameNameExistQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
                VmHandler.isVmWithSameNameExistStatic(((IsVmWithSameNameExistParameters) getParameters()).getVmName()));
    }
}
