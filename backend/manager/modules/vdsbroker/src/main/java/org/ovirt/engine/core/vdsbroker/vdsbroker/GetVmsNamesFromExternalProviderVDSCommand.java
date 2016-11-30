package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.GetVmsFromExternalProviderParameters;

public class GetVmsNamesFromExternalProviderVDSCommand<T extends GetVmsFromExternalProviderParameters> extends VdsBrokerCommand<T> {
    private VMNamesListReturn vmListReturn;

    public GetVmsNamesFromExternalProviderVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmListReturn = getBroker().getExternalVmNamesList(getParameters().getUrl(),
                getParameters().getUsername(), getParameters().getPassword());
        proceedProxyReturnValue();
        List<VM> vms = new ArrayList<>();
        for (String vmName : vmListReturn.getNamesList()) {
            VM vm = new VM();
            if (vmName != null) {
                vm.setName(vmName);
                vm.setOrigin(getParameters().getOriginType());
                vms.add(vm);
            }
        }
        setReturnValue(vms);
    }

    @Override
    protected Status getReturnStatus() {
        return vmListReturn.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return vmListReturn;
    }
}
