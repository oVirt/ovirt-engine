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
                    MemoryUtils.createHotplugMemoryParamsMap(
                            getParameters().getMemoryDeviceToUnplug(),
                            getParameters().getMinAllocatedMemoryMb()));
            proceedProxyReturnValue();
        } catch (RuntimeException e) {
            setVdsRuntimeErrorAndReport(e);
            // prevent exception handler from rethrowing an exception
            getVDSReturnValue().setExceptionString(null);
        }
    }

    public static class Params extends VdsIdVDSCommandParametersBase {

        private final VmDevice memoryDeviceToUnplug;

        private final int minAllocatedMemoryMb;

        public Params(Guid vdsId, VmDevice memoryDeviceToUnplug, int minAllocatedMemoryMb) {
            super(vdsId);
            this.memoryDeviceToUnplug = memoryDeviceToUnplug;
            this.minAllocatedMemoryMb = minAllocatedMemoryMb;
        }

        public VmDevice getMemoryDeviceToUnplug() {
            return memoryDeviceToUnplug;
        }

        public int getMinAllocatedMemoryMb() {
            return minAllocatedMemoryMb;
        }
    }
}
