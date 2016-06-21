package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.storage.StoragePoolDomainHelper;

public class ConnectStoragePoolVDSCommand<P extends ConnectStoragePoolVDSCommandParameters>
        extends VdsBrokerCommand<P> {

    public ConnectStoragePoolVDSCommand(P parameters) {
        super(parameters);
    }

    public void connectStoragePool() {
        Map<String, String> storageDomains = StoragePoolDomainHelper.buildStoragePoolDomainsMap
                (getParameters().getStorageDomains());
        log.info("Executing with domain map: {}", storageDomains);
        status = getBroker().connectStoragePool(
                getParameters().getStoragePoolId().toString(),
                getParameters().getVds().getVdsSpmId(),
                getParameters().getStoragePoolId().toString(),
                getParameters().getMasterDomainId().toString(),
                getParameters().getStoragePool().getMasterDomainVersion(),
                storageDomains);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        connectStoragePool();
        proceedProxyReturnValue();
    }

    protected void proceedProxyReturnValue() {
        proceedConnectProxyReturnValue();
    }

    // Don't throw exception on errors except StoragePoolMasterNotFound for
    // master domain failure treatment
    protected void proceedConnectProxyReturnValue() {
        EngineError returnStatus = getReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case Done:
        case StoragePoolMasterNotFound:
        case StoragePoolTooManyMasters:
        case StoragePoolWrongMaster:
            super.proceedProxyReturnValue();
            break;
        case ReleaseLockFailure:
        case AcquireHostIdFailure:
        case ReleaseHostIdFailure:
            VDSExceptionBase outEx = new VDSErrorException(String.format("Failed in vdscommand %1$s, error = %2$s",
                    getCommandName(), getReturnStatus().message));
            initializeVdsError(returnStatus);
            getVDSReturnValue().setSucceeded(false);
            throw outEx;
        default:
            getVDSReturnValue().setSucceeded(false);
            initializeVdsError(returnStatus);
            break;
        }
    }
}
