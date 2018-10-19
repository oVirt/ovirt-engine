package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.architecture.MemoryUtils;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;

public class SetAmountOfMemoryVDSCommand <P extends SetAmountOfMemoryVDSCommand.Params> extends VdsBrokerCommand<P> {
    public SetAmountOfMemoryVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        try {
            status = getBroker().hotplugMemory(
                    MemoryUtils.createHotplugMemoryParamsMap(getParameters().getMemoryDevice(), getParameters().getMinAllocatedMem()));
            proceedProxyReturnValue();
        } catch (RuntimeException e) {
            setVdsRuntimeErrorAndReport(e);
            // prevent exception handler from rethrowing an exception
            getVDSReturnValue().setExceptionString(null);
        }
    }

    public static class Params extends VdsAndVmIDVDSParametersBase {

        private VmDevice memoryDevice;

        private final int minAllocatedMem;

        public Params(Guid vdsId, Guid vmId, VmDevice memoryDevice, int minAllocatedMem) {
            super(vdsId, vmId);
            this.memoryDevice = memoryDevice;
            this.minAllocatedMem = minAllocatedMem;
        }

        public VmDevice getMemoryDevice() {
            return memoryDevice;
        }

        public int getMinAllocatedMem() {
            return minAllocatedMem;
        }

        @Override
        protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
            return super.appendAttributes(tsb)
                    .append("memoryDevice", getMemoryDevice())
                    .append("minAllocatedMem", getMinAllocatedMem());
        }
    }
}
