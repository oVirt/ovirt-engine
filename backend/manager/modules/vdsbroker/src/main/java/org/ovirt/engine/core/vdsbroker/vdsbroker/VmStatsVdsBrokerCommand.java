package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.monitoring.VdsmVm;

public abstract class VmStatsVdsBrokerCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    @Inject
    private VdsBrokerObjectsBuilder vdsBrokerObjectsBuilder;

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
        VmDynamic vmDynamic = vdsBrokerObjectsBuilder.buildVMDynamicData(struct, getVds());
        Guid vmId = vmDynamic.getId();
        return new VdsmVm(vdsBrokerObjectsBuilder.getVdsmCallTimestamp(struct))
                .setVmDynamic(vmDynamic)
                .setDevicesHash(vdsBrokerObjectsBuilder.getVmDevicesHash(struct))
                .setTpmDataHash(vdsBrokerObjectsBuilder.getTpmDataHash(struct))
                .setNvramDataHash(vdsBrokerObjectsBuilder.getNvramDataHash(struct))
                .setVmStatistics(vdsBrokerObjectsBuilder.buildVMStatisticsData(struct))
                .setVmJobs(vdsBrokerObjectsBuilder.buildVmJobsData(struct))
                .setInterfaceStatistics(vdsBrokerObjectsBuilder.buildInterfaceStatisticsData(struct))
                .setVmBalloonInfo(vdsBrokerObjectsBuilder.buildVmBalloonInfo(struct))
                .setVmGuestAgentInterfaces(vdsBrokerObjectsBuilder.buildVmGuestAgentInterfacesData(vmId, struct))
                .setLunsMap(vdsBrokerObjectsBuilder.buildVmLunDisksData(struct))
                .setDiskStatistics(vdsBrokerObjectsBuilder.buildVmDiskStatistics(struct));
    }

    @Override
    protected boolean shouldLogToAudit() {
        return false;
    }
}
