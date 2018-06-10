package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class DumpXmlsVDSCommand<P extends DumpXmlsVDSCommand.Params> extends VdsBrokerCommand<P> {
    private DomainXmlListReturn domainXmlListReturn;

    public DumpXmlsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        domainXmlListReturn = getBroker().dumpxmls(getParameters().getVmIds().stream()
                .map(Guid::toString)
                .collect(Collectors.toList()));
        proceedProxyReturnValue();
        setReturnValue(domainXmlListReturn.getDomainXmls());
    }

    @Override
    protected Status getReturnStatus() {
        return domainXmlListReturn.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return domainXmlListReturn;
    }

    public static class Params extends VdsIdVDSCommandParametersBase {

        private List<Guid> vmIds;

        public Params(Guid vdsId, List<Guid> vmIds) {
            super(vdsId);
            this.vmIds = vmIds;
        }

        public Params() {
        }

        public List<Guid> getVmIds() {
            return vmIds;
        }

        @Override
        protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
            return super.appendAttributes(tsb)
                    .append("vmIds", getVmIds());
        }
    }

}
