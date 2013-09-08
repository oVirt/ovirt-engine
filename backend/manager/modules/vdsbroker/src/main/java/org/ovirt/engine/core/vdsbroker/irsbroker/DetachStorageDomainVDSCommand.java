package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.DetachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class DetachStorageDomainVDSCommand<P extends DetachStorageDomainVDSCommandParameters>
        extends IrsBrokerCommand<P> {
    public DetachStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        if (getParameters().getForce()) {
            try {
                status = getIrsProxy().forcedDetachStorageDomain(getParameters().getStorageDomainId().toString(),
                        getParameters().getStoragePoolId().toString());
                proceedProxyReturnValue();
            } catch (RuntimeException ex) {
                PrintReturnValue();
                log.errorFormat("Could not force detach domain {0} on pool {1}. error: {2}", getParameters()
                        .getStorageDomainId(), getParameters().getStoragePoolId(), ex.toString());
                getVDSReturnValue().setSucceeded(false);
            }
        } else {
            status = getIrsProxy().detachStorageDomain(getParameters().getStorageDomainId().toString(),
                    getParameters().getStoragePoolId().toString(),
                    getParameters().getMasterStorageDomainId().toString(),
                    getParameters().getMasterVersion());
            proceedProxyReturnValue();
        }
    }

    private static Log log = LogFactory.getLog(DetachStorageDomainVDSCommand.class);
}
