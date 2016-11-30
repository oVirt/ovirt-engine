package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder.buildInterfaceStatisticsData;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder.buildVMStatisticsData;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder.buildVmBalloonInfo;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder.buildVmDiskStatistics;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder.buildVmGuestAgentInterfacesData;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder.buildVmJobsData;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder.buildVmLunDisksData;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder.getVdsmCallTimestamp;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder.getVmDevicesHash;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.monitoring.VdsmVm;

public abstract class VmStatsVdsBrokerCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    protected VMInfoListReturn vmListReturn;

    protected VmStatsVdsBrokerCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return vmListReturn.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return vmListReturn;
    }

    protected VdsmVm createVdsmVm(Map<String, Object> struct) {
        VmDynamic vmDynamic = new VmDynamic();
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, struct, getVds());
        Guid vmId = vmDynamic.getId();
        return new VdsmVm(getVdsmCallTimestamp(struct))
                .setVmDynamic(vmDynamic)
                .setDevicesHash(getVmDevicesHash(struct))
                .setVmStatistics(buildVMStatisticsData(struct))
                .setVmJobs(buildVmJobsData(struct))
                .setInterfaceStatistics(buildInterfaceStatisticsData(struct))
                .setVmBalloonInfo(buildVmBalloonInfo(struct))
                .setVmGuestAgentInterfaces(buildVmGuestAgentInterfacesData(vmId, struct))
                .setLunsMap(buildVmLunDisksData(struct))
                .setDiskStatistics(buildVmDiskStatistics(struct));
    }

    @Override
    protected boolean shouldLogToAudit() {
        return false;
    }
}
