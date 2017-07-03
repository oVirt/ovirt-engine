package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.libvirt.VmDevicesConverter;

public class DumpXmlsVDSCommand<P extends DumpXmlsVDSCommand.Params> extends VdsBrokerCommand<P> {
    private DomainXmlListReturn domainXmlListReturn;

    @Inject
    private VmDevicesConverter converter;

    public DumpXmlsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        domainXmlListReturn = getBroker().dumpxmls(getParameters().getVmIds().stream()
                .map(Guid::toString)
                .collect(Collectors.toList()));
        proceedProxyReturnValue();
        @SuppressWarnings("unchecked")
        Map<String, Object>[] devices = getParameters().getVmIds().stream()
                .map(vmId -> extractDevices(vmId, domainXmlListReturn.getDomainXmls().get(vmId)))
                .filter(Objects::nonNull)
                .toArray(Map[]::new);
        setReturnValue(devices);
    }

    private Map<String, Object> extractDevices(Guid vmId, String domxml) {
        try {
            return converter.convert(vmId, getVds().getId(), domxml);
        } catch (Exception ex) {
            log.error("Failed during parsing devices of VM {} ({}) error is: {}", resourceManager.getVmManager(vmId).getName(), vmId, ex);
            log.error("Exception:", ex);
            return null;
        }
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
