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
    protected VMInfoListReturnForXmlRpc vmListReturn;

    protected VmStatsVdsBrokerCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return vmListReturn.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return vmListReturn;
    }

    protected VdsmVm createVdsmVm(Map<String, Object> xmlRpcStruct) {
        VmDynamic vmDynamic = new VmDynamic();
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xmlRpcStruct, getVds());
        Guid vmId = vmDynamic.getId();
        return new VdsmVm(getVdsmCallTimestamp(xmlRpcStruct))
                .setVmDynamic(vmDynamic)
                .setDevicesHash(getVmDevicesHash(xmlRpcStruct))
                .setVmStatistics(buildVMStatisticsData(xmlRpcStruct))
                .setVmJobs(buildVmJobsData(xmlRpcStruct))
                .setInterfaceStatistics(buildInterfaceStatisticsData(xmlRpcStruct))
                .setVmBalloonInfo(buildVmBalloonInfo(xmlRpcStruct))
                .setVmGuestAgentInterfaces(buildVmGuestAgentInterfacesData(vmId, xmlRpcStruct))
                .setLunsMap(buildVmLunDisksData(xmlRpcStruct))
                .setDiskStatistics(buildVmDiskStatistics(xmlRpcStruct));
    }

    @Override
    protected boolean shouldLogToAudit() {
        return false;
    }
}
