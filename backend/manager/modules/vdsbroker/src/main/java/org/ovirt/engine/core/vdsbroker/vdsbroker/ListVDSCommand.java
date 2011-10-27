package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(executionLevel = LogLevel.TRACE)
public class ListVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    private VMListReturnForXmlRpc mVmListReturn;

    public ListVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        mVmListReturn = getBroker().list();
        ProceedProxyReturnValue();
        java.util.HashMap<Guid, java.util.Map.Entry<VmDynamic, VmStatistics>> returnVMs =
                new java.util.HashMap<Guid, java.util.Map.Entry<VmDynamic, VmStatistics>>();
        for (int idx = 0; idx < mVmListReturn.mVmList.length; ++idx) // fix
                                                                     // GetLength(0)
        {
            VmDynamic dynamicData = VdsBrokerObjectsBuilder.buildVMDynamicDataFromList(mVmListReturn.mVmList[idx]);
            returnVMs.put(dynamicData.getId(), new KeyValuePairCompat<VmDynamic, VmStatistics>(dynamicData, null));
        }
        setReturnValue(returnVMs);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return mVmListReturn.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return mVmListReturn;
    }

    @Override
    protected boolean getIsPrintReturnValue() {
        return false;
    }
}
