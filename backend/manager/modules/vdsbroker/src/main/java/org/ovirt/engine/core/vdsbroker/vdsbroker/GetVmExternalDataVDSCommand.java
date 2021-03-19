package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.action.VmExternalDataKind;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class GetVmExternalDataVDSCommand<P extends GetVmExternalDataVDSCommand.Parameters> extends VdsBrokerCommand<P> {

    private VmExternalDataReturn vmDataReturn;

    public GetVmExternalDataVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmDataReturn = getBroker().getVmExternalData(getParameters().getVmId().toString(), getParameters().getKind(),
                getParameters().getForceUpdate());
        proceedProxyReturnValue();
        setReturnValue(vmDataReturn);
    }

    @Override
    protected Status getReturnStatus() {
        return vmDataReturn.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return vmDataReturn;
    }

    public static class Parameters extends VdsAndVmIDVDSParametersBase {

        private boolean forceUpdate;
        private VmExternalDataKind kind;

        public Parameters(Guid vdsId, Guid vmId, VmExternalDataKind kind, boolean forceUpdate) {
            super(vdsId, vmId);
            this.forceUpdate = forceUpdate;
            this.kind = kind;
        }

        public boolean getForceUpdate() {
            return forceUpdate;
        }

        public VmExternalDataKind getKind() {
            return kind;
        }

        @Override
        protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
            return super.appendAttributes(tsb)
                    .append("kind", getKind());
        }
    }
}
