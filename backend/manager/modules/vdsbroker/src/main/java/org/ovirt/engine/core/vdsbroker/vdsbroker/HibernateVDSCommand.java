package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.HibernateVDSCommandParameters;

public class HibernateVDSCommand<P extends HibernateVDSCommandParameters> extends VdsBrokerCommand<P> {
    public HibernateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().hibernate(getParameters().getVmId().toString(),
                getParameters().getHibernationVolHandle());
        ProceedProxyReturnValue();
    }
}
