package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.vdsbroker.vdsbroker.entities.VmInternalData;

@Logged(executionLevel = LogLevel.TRACE)
public class ListVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    private VMListReturnForXmlRpc mVmListReturn;

    public ListVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        mVmListReturn = getBroker().list();
        proceedProxyReturnValue();
        Map<Guid, VmInternalData> returnVMs = new HashMap<Guid, VmInternalData>();
        for (int idx = 0; idx < mVmListReturn.mVmList.length; ++idx) {
            VmDynamic dynamicData = VdsBrokerObjectsBuilder.buildVMDynamicDataFromList(mVmListReturn.mVmList[idx]);
            VmInternalData vmData = new VmInternalData(dynamicData);
            returnVMs.put(dynamicData.getId(), vmData);
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

    @Override
    protected void logToAudit(){
    }
}
