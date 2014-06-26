package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.DetachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
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
                Guid storagePoolId = getParameters().getStoragePoolId();
                if (getParameters().isDetachFromOldStoragePool()) {
                    storagePoolId = Guid.Empty;
                }
                status = getIrsProxy().forcedDetachStorageDomain(getParameters().getStorageDomainId().toString(),
                        storagePoolId.toString());
                proceedProxyReturnValue();
            } catch (RuntimeException ex) {
                printReturnValue();
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

    private static final Log log = LogFactory.getLog(DetachStorageDomainVDSCommand.class);
}
