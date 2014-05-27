package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.HibernateVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.vdsbroker.vdsbroker.HibernateBrokerVDSCommand;

public class HibernateVDSCommand<P extends HibernateVDSCommandParameters> extends ManagingVmCommand<P> {

    public HibernateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVmCommand() {
        VDSReturnValue retVal = runHibernateBrokerVDSCommand();
        if (retVal.getSucceeded()) {
            vmManager.succededToHibernate();
            getVDSReturnValue().setSucceeded(true);
        }
        else {
            log.error("Failed to hibernate VM '{}' in VDS = '{}' : error = '{}'",
                    getParameters().getVmId(), getParameters().getVdsId(), retVal.getExceptionString());
            getVDSReturnValue().setSucceeded(false);
            getVDSReturnValue().setExceptionString(retVal.getExceptionString());
            getVDSReturnValue().setExceptionObject(retVal.getExceptionObject());
            getVDSReturnValue().setVdsError(retVal.getVdsError());
        }
    }

    private VDSReturnValue runHibernateBrokerVDSCommand() {
        HibernateBrokerVDSCommand<HibernateVDSCommandParameters> command =
                new HibernateBrokerVDSCommand<HibernateVDSCommandParameters>(getParameters());
        command.execute();
        return command.getVDSReturnValue();
    }
}
