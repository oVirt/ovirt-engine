package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.queries.IdQueryParameters;

public class GetVmByVmIdForUpdateQuery<P extends IdQueryParameters> extends GetVmNextRunConfigurationQuery<P> {

    public GetVmByVmIdForUpdateQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        VM vm = getVm();
        if (vm == null) {
            return;
        }
        VM nextRunVm = vmHandler.getNextRunVmConfiguration(getParameters().getId(), getUserID(), getParameters().isFiltered(), false);
        if (nextRunVm != null) {
            vm.setCustomCompatibilityVersion(nextRunVm.getCustomCompatibilityVersion());
        }
        updateVMDetails(vm);
        getQueryReturnValue().setReturnValue(vm);
    }

}
