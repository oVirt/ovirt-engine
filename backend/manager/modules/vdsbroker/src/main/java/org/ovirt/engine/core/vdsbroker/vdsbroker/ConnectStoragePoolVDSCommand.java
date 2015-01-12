package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.storage.StoragePoolDomainHelper;

import java.util.Map;

public class ConnectStoragePoolVDSCommand<P extends ConnectStoragePoolVDSCommandParameters>
        extends VdsBrokerCommand<P> {

    public ConnectStoragePoolVDSCommand(P parameters) {
        super(parameters);
    }

    private boolean isStoragePoolMemoryBackend() {
        return FeatureSupported.storagePoolMemoryBackend(
                getParameters().getStoragePool().getCompatibilityVersion());
    }

    public void connectStoragePool() {
        Map<String, String> storageDomains = null;

        if (isStoragePoolMemoryBackend()) {
            storageDomains = StoragePoolDomainHelper.buildStoragePoolDomainsMap(
                    getParameters().getStorageDomains());
        }

        status = getBroker().connectStoragePool(
                getParameters().getStoragePoolId().toString(),
                getParameters().getVds().getVdsSpmId(),
                getParameters().getStoragePoolId().toString(),
                getParameters().getMasterDomainId().toString(),
                getParameters().getStoragePool().getMasterDomainVersion(),
                storageDomains);
    }

    public void refreshStoragePool() {
        status = getBroker().refreshStoragePool(
                getParameters().getStoragePoolId().toString(),
                getParameters().getMasterDomainId().toString(),
                getParameters().getStoragePool().getMasterDomainVersion());
    }

    protected boolean isRefreshStoragePool() {
        return getParameters().isRefreshOnly() && !isStoragePoolMemoryBackend();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        if (isRefreshStoragePool()) {
            refreshStoragePool();
        } else {
            connectStoragePool();
        }
        proceedProxyReturnValue();
    }

    protected void proceedProxyReturnValue() {
        if (isRefreshStoragePool()) {
            super.proceedProxyReturnValue();
        } else {
            proceedConnectProxyReturnValue();
        }
    }

    // Don't throw exception on errors except StoragePoolMasterNotFound for
    // master domain failure treatment
    protected void proceedConnectProxyReturnValue() {
        VdcBllErrors returnStatus = getReturnValueFromStatus(getReturnStatus());
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
                    getCommandName(), getReturnStatus().mMessage));
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
