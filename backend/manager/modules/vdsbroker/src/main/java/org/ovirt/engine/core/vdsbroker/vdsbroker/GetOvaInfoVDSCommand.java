package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.ArchitectureType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.vdscommands.GetOvaInfoParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetOvaInfoVDSCommand<T extends GetOvaInfoParameters> extends VdsBrokerCommand<T> {
    private VMListReturnForXmlRpc vmListReturn;

    public GetOvaInfoVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmListReturn = getBroker().getExternalVmFromOva(getParameters().getPath());
        proceedProxyReturnValue();
        Map<String, Object> map = vmListReturn.vmList[0];
        map.put(VdsProperties.vm_guid, Guid.newGuid().toString());
        map.put(VdsProperties.vm_arch, ArchitectureType.x86_64.toString());
        VM vm = VdsBrokerObjectsBuilder.buildVmsDataFromExternalProvider(map);
        setReturnValue(vm);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return vmListReturn.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return vmListReturn;
    }

    @Override
    protected boolean getIsPrintReturnValue() {
        return false;
    }
}
