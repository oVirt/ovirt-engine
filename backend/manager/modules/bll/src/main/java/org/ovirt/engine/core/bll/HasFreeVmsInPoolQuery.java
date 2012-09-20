package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.queries.*;

public class HasFreeVmsInPoolQuery<P extends HasFreeVmsInPoolParameters> extends QueriesCommandBase<P> {
    public HasFreeVmsInPoolQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue((!VmPoolCommandBase.getVmToAttach(getParameters().getPoolId())
                                                                .equals(Guid.Empty)));
    }
}
