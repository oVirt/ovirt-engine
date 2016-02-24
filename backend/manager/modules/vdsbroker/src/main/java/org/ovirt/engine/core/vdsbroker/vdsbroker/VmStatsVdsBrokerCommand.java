package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

public abstract class VmStatsVdsBrokerCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    protected VMInfoListReturnForXmlRpc vmListReturn;

    protected VmStatsVdsBrokerCommand(P parameters, VDS vds) {
        super(parameters, vds);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return vmListReturn.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return vmListReturn;
    }

    protected VmInternalData createVmInternalData(Map<String, Object> xmlRpcStruct) {
        VmDynamic vmDynamic = new VmDynamic();
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xmlRpcStruct, getVds());
        return new VmInternalData(vmDynamic,
                VdsBrokerObjectsBuilder.buildVMStatisticsData(xmlRpcStruct),
                VdsBrokerObjectsBuilder.buildVmGuestAgentInterfacesData(vmDynamic.getId(), xmlRpcStruct),
                VdsBrokerObjectsBuilder.buildVmLunDisksData(xmlRpcStruct),
                VdsBrokerObjectsBuilder.getVdsmCallTimestamp(xmlRpcStruct));
    }

    @Override
    protected boolean shouldLogToAudit() {
        return false;
    }
}
