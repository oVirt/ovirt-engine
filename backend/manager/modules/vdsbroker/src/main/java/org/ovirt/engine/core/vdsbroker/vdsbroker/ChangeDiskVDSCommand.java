package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.ChangeDiskVDSCommandParameters;

public class ChangeDiskVDSCommand<P extends ChangeDiskVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {

    public ChangeDiskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        if (getParameters().getIface() != null)  {
            Map<String, Object> driveSpec = new HashMap<>();
            driveSpec.put(VdsProperties.INTERFACE, getParameters().getIface());
            driveSpec.put(VdsProperties.Index, Integer.toString(getParameters().getIndex()));
            driveSpec.put(VdsProperties.Path, getParameters().getDiskPath());
            vmReturn = getBroker().changeDisk(vmId.toString(), driveSpec);
        } else {
            vmReturn = getBroker().changeDisk(vmId.toString(), getParameters().getDiskPath());
        }
        proceedProxyReturnValue();
        setReturnValue(VdsBrokerObjectsBuilder.buildVMDynamicData(vmReturn.vm, getVds()).getStatus());
    }

}
