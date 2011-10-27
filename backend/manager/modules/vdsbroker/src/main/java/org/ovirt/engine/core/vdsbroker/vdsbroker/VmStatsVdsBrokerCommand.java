package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;

public abstract class VmStatsVdsBrokerCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    protected VMInfoListReturnForXmlRpc mVmListReturn;

    public VmStatsVdsBrokerCommand(P parameters) {
        super(parameters);
    }

    protected VmStatsVdsBrokerCommand(P parameters, VDS vds) {
        super(parameters, vds);
    }


    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return mVmListReturn.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return mVmListReturn;
    }
}
