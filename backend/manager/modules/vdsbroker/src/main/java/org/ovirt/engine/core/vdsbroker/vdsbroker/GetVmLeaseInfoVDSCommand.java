package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.VmLeaseVDSParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;

public class GetVmLeaseInfoVDSCommand<T extends VmLeaseVDSParameters> extends IrsBrokerCommand<T> {

    private LeaseInfoReturn result;

    public GetVmLeaseInfoVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        result = getIrsProxy().getLeaseInfo(
                getParameters().getLeaseId().toString(),
                getParameters().getStorageDomainId().toString());
        proceedProxyReturnValue();
        Map<String, Object> leaseInfo = result.getLeaseInfo();
        leaseInfo.remove(VdsProperties.VmLeaseId);
        leaseInfo.remove(VdsProperties.VmLeaseSdId);
        leaseInfo.forEach((key, value) -> leaseInfo.put(key, String.valueOf(value)));
        setReturnValue(leaseInfo);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }
}
