package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

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

    protected VmInternalData createVmInternalData(XmlRpcStruct xmlRpcStruct) {
        VmDynamic vmDynamic = new ExtendedVmDynamic(getVds());
        VdsBrokerObjectsBuilder.updateVMDynamicData(vmDynamic, xmlRpcStruct);
        return new VmInternalData(vmDynamic,
                VdsBrokerObjectsBuilder.buildVMStatisticsData(xmlRpcStruct),
                VdsBrokerObjectsBuilder.buildVmGuestAgentInterfacesData(vmDynamic.getId(), xmlRpcStruct));
    }
}
