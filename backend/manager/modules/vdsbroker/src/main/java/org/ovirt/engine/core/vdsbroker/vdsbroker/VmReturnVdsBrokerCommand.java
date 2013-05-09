package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;

public abstract class VmReturnVdsBrokerCommand<P extends VdsAndVmIDVDSParametersBase> extends VdsBrokerCommand<P> {
    protected OneVmReturnForXmlRpc mVmReturn;
    protected Guid mVmId = Guid.Empty;

    public VmReturnVdsBrokerCommand(P parameters) {
        super(parameters);
        mVmId = parameters.getVmId();
    }

    public VmReturnVdsBrokerCommand(P parameters, Guid vmId) {
        super(parameters);
        mVmId = vmId;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return mVmReturn.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return mVmReturn;
    }
}
