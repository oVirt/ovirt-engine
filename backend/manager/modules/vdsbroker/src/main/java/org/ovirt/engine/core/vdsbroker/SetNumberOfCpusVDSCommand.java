package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;

public class SetNumberOfCpusVDSCommand<P extends SetNumberOfCpusVDSCommand.Params> extends VdsBrokerCommand<P> {

    public SetNumberOfCpusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        try {
            status = getBroker().setNumberOfCpus(
                    getParameters().getVmId().toString(),
                    String.valueOf(getParameters().getNumberOfCpus()));
            proceedProxyReturnValue();
        } catch (RuntimeException e) {
            setVdsRuntimeErrorAndReport(e);
            // prevent exception handler from rethrowing an exception
            getVDSReturnValue().setExceptionString(null);
        }
    }

    public static class Params extends VdsAndVmIDVDSParametersBase{

        private int numberOfCpus;

        public Params(Guid vdsId, Guid vmId, int numberOfCpus) {
            super(vdsId, vmId);
            this.numberOfCpus = numberOfCpus;
        }

        public int getNumberOfCpus() {
            return numberOfCpus;
        }

        @Override
        protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
            return super.appendAttributes(tsb).append("numberOfCpus", getNumberOfCpus());
        }
    }
}
