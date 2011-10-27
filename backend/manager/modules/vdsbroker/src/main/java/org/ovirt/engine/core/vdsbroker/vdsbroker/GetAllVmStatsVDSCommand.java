package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(executionLevel = LogLevel.DEBUG)
public class GetAllVmStatsVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends VmStatsVdsBrokerCommand<P> {
    public GetAllVmStatsVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        mVmListReturn = getBroker().getAllVmStats();
        ProceedProxyReturnValue();
        java.util.HashMap<Guid, java.util.Map.Entry<VmDynamic, VmStatistics>> returnVMs =
                new java.util.HashMap<Guid, java.util.Map.Entry<VmDynamic, VmStatistics>>();
        for (int idx = 0; idx < mVmListReturn.mInfoList.length; ++idx) {
            VmDynamic vmDynamic = new ExtendedVmDynamic(getVds());
            VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, mVmListReturn.mInfoList[idx]);
            VmStatistics vmStatistics = VdsBrokerObjectsBuilder.buildVMStatisticsData(mVmListReturn.mInfoList[idx]);
            returnVMs.put(vmDynamic.getId(), new KeyValuePairCompat<VmDynamic, VmStatistics>(vmDynamic,
                    vmStatistics));
        }
        setReturnValue(returnVMs);
    }

    @Override
    protected boolean getIsPrintReturnValue() {
        return false;
    }
}
