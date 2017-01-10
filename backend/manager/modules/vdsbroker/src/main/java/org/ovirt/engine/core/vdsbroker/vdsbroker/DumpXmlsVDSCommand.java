package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class DumpXmlsVDSCommand<P extends DumpXmlsVDSCommand.Params> extends VdsBrokerCommand<P> {
    private DomainXmlListReturn vmDevicesListReturn;

    public DumpXmlsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmDevicesListReturn = getBroker().dumpxmls(getParameters().getVmIds());
        proceedProxyReturnValue();
    }

    @Override
    protected Status getReturnStatus() {
        return vmDevicesListReturn.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return vmDevicesListReturn;
    }

    public static class Params extends VdsIdVDSCommandParametersBase {

        private List<String> vmIds;

        public Params(Guid vdsId, List<String> vmIds) {
            super(vdsId);
            this.vmIds = vmIds;
        }

        public Params() {
        }

        public List<String> getVmIds() {
            return vmIds;
        }

        @Override
        protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
            return super.appendAttributes(tsb)
                    .append("vmIds", getVmIds());
        }
    }

}
