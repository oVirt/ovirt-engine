package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.VmStatistics;
import org.ovirt.engine.core.common.vdscommands.GetVmStatsVDSCommandParameters;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@Logged(executionLevel = LogLevel.DEBUG)
public class GetVmStatsVDSCommand<P extends GetVmStatsVDSCommandParameters> extends VmStatsVdsBrokerCommand<P> {
    public GetVmStatsVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        mVmListReturn = getBroker().getVmStats(getParameters().getVmId().toString());
        ProceedProxyReturnValue();
        VmDynamic vmDynamic = new ExtendedVmDynamic(getVds());
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, mVmListReturn.mInfoList[0]);
        setReturnValue(new KeyValuePairCompat<VmDynamic, VmStatistics>(
                vmDynamic,
                VdsBrokerObjectsBuilder.buildVMStatisticsData(mVmListReturn.mInfoList[0])));
    }
}
