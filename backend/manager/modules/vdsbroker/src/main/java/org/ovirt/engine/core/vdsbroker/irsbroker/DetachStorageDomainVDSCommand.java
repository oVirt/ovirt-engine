package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.vdscommands.DetachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VDSExceptionBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DetachStorageDomainVDSCommand<P extends DetachStorageDomainVDSCommandParameters>
        extends IrsBrokerCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(DetachStorageDomainVDSCommand.class);

    public DetachStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        if (getParameters().getForce()) {
            Guid storagePoolId = getParameters().getStoragePoolId();
            if (getParameters().isDetachFromOldStoragePool()) {
                storagePoolId = Guid.Empty;
            }
            status = getIrsProxy().forcedDetachStorageDomain(getParameters().getStorageDomainId().toString(),
                    storagePoolId.toString());
            proceedProxyReturnValue();
        } else {
            status = getIrsProxy().detachStorageDomain(getParameters().getStorageDomainId().toString(),
                    getParameters().getStoragePoolId().toString(),
                    getParameters().getMasterStorageDomainId().toString(),
                    getParameters().getMasterVersion());
            proceedProxyReturnValue();
        }
    }

    @Override
    protected VDSExceptionBase createDefaultConcreteException(String errorMessage) {
        return new IrsOperationFailedNoFailoverException(errorMessage);
    }
}
