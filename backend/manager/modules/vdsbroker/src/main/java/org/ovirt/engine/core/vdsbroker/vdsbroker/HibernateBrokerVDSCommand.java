package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.HibernateVDSCommandParameters;

public class HibernateBrokerVDSCommand<P extends HibernateVDSCommandParameters> extends VdsBrokerCommand<P> {
    public HibernateBrokerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().hibernate(getParameters().getVmId().toString(),
                getParameters().getHibernationVolHandle());
        proceedProxyReturnValue();
    }
}
