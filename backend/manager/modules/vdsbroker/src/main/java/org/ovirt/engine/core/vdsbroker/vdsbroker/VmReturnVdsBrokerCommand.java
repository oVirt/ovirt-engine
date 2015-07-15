package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;

public abstract class VmReturnVdsBrokerCommand<P extends VdsAndVmIDVDSParametersBase> extends VdsBrokerCommand<P> {
    protected OneVmReturnForXmlRpc vmReturn;
    protected Guid vmId = Guid.Empty;

    public VmReturnVdsBrokerCommand(P parameters) {
        super(parameters);
        vmId = parameters.getVmId();
    }

    public VmReturnVdsBrokerCommand(P parameters, Guid vmId) {
        super(parameters);
        this.vmId = vmId;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return vmReturn.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return vmReturn;
    }
}
