package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVmNextRunConfigurationQuery<P extends IdQueryParameters> extends GetVmByVmIdQuery<P> {

    public GetVmNextRunConfigurationQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = vmHandler.getNextRunVmConfiguration(getParameters().getId(), getUserID(), getParameters().isFiltered(), true);
        if (vm != null) {
            getQueryReturnValue().setReturnValue(vm);
        } else {
            // in case no next_run return static configuration
            super.executeQueryCommand();
        }
    }
}
