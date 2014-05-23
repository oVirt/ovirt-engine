package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.GraphicsInfo;
import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

public abstract class VmStatsVdsBrokerCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    protected VMInfoListReturnForXmlRpc mVmListReturn;

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

    protected VmInternalData createVmInternalData(Map<String, Object> xmlRpcStruct) {
        VmDynamic vmDynamic = new VmDynamic();
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xmlRpcStruct);
        adjustDisplayIp(vmDynamic.getGraphicsInfos());
        return new VmInternalData(vmDynamic,
                VdsBrokerObjectsBuilder.buildVMStatisticsData(xmlRpcStruct),
                VdsBrokerObjectsBuilder.buildVmGuestAgentInterfacesData(vmDynamic.getId(), xmlRpcStruct),
                VdsBrokerObjectsBuilder.buildVmLunDisksData(xmlRpcStruct));
    }

    /**
     * Adjusts displayIp for graphicsInfos:
     *  - if displayIp is overriden on cluster level then overriden address is used,
     *   or
     *  - if current displayIp starts with "0" then host's hostname is used.
     *
     * @param graphicsInfos - graphicsInfo to adjust
     */
    private void adjustDisplayIp(Map<GraphicsType, GraphicsInfo> graphicsInfos) {
        VDS host = getVds();

        for (GraphicsInfo graphicsInfo : graphicsInfos.values()) {
                if (graphicsInfo == null) {
                    continue;
                }

                if (host.getConsoleAddress() != null) {
                    graphicsInfo.setIp(host.getConsoleAddress());
                } else if (graphicsInfo.getIp().startsWith("0")) {
                    graphicsInfo.setIp(host.getHostName());
                }
            }
    }
}
