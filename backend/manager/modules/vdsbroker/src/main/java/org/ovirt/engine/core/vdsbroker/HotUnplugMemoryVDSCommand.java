package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.architecture.MemoryUtils;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;

public class HotUnplugMemoryVDSCommand<P extends HotUnplugMemoryVDSCommand.Params> extends VdsBrokerCommand<P> {

    public HotUnplugMemoryVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        try {
            status = getBroker().hotUnplugMemory(
                    MemoryUtils.createVmMemoryDeviceMap(getParameters().getMemoryDeviceToUnplug(), true));
            proceedProxyReturnValue();
        } catch (RuntimeException e) {
            setVdsRuntimeErrorAndReport(e);
            // prevent exception handler from rethrowing an exception
            getVDSReturnValue().setExceptionString(null);
        }
    }

    public static class Params extends VdsIdVDSCommandParametersBase {

        private final VmDevice memoryDeviceToUnplug;

        public Params(Guid vdsId, VmDevice memoryDeviceToUnplug) {
            super(vdsId);
            this.memoryDeviceToUnplug = memoryDeviceToUnplug;
        }

        public VmDevice getMemoryDeviceToUnplug() {
            return memoryDeviceToUnplug;
        }
    }
}
