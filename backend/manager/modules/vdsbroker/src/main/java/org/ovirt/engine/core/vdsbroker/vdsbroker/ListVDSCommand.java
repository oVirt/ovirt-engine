package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder.buildVMDynamicDataFromList;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder.getVdsmCallTimestamp;
import static org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerObjectsBuilder.getVmDevicesHash;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.vdsbroker.monitoring.VdsmVm;

@Logged(executionLevel = LogLevel.TRACE)
public class ListVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    private VMListReturn vmListReturn;

    public ListVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmListReturn = getBroker().list();
        proceedProxyReturnValue();
        Map<Guid, VdsmVm> returnVMs = new HashMap<>();
        for (int idx = 0; idx < vmListReturn.vmList.length; ++idx) {
            Map<String, Object> vm = vmListReturn.vmList[idx];
            VmDynamic dynamicData = buildVMDynamicDataFromList(vm);
            VdsmVm vdsmVm = new VdsmVm(getVdsmCallTimestamp(vm))
                    .setVmDynamic(dynamicData)
                    .setDevicesHash(getVmDevicesHash(vm));
            returnVMs.put(dynamicData.getId(), vdsmVm);
        }
        setReturnValue(returnVMs);
    }

    @Override
    protected Status getReturnStatus() {
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
