package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder.buildVMDynamicDataFromList;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder.getVdsmCallTimestamp;

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
    private VMListReturnForXmlRpc vmListReturn;

    public ListVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmListReturn = getBroker().list();
        proceedProxyReturnValue();
        Map<Guid, VmInternalData> returnVMs = new HashMap<>();
        for (int idx = 0; idx < vmListReturn.vmList.length; ++idx) {
            Map<String, Object> vm = vmListReturn.vmList[idx];
            VmDynamic dynamicData = buildVMDynamicDataFromList(vm);
            VmInternalData vmData = new VmInternalData(getVdsmCallTimestamp(vm))
                    .setVmDynamic(dynamicData);
            returnVMs.put(dynamicData.getId(), vmData);
        }
        setReturnValue(returnVMs);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return vmListReturn.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return vmListReturn;
    }

    @Override
    protected boolean getIsPrintReturnValue() {
        return false;
    }

    @Override
    protected boolean shouldLogToAudit(){
        return false;
    }
}
