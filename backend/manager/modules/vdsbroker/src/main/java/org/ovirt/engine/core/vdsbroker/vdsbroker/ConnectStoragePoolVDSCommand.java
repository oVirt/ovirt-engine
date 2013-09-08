package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.ConnectStoragePoolVDSCommandParameters;

public class ConnectStoragePoolVDSCommand<P extends ConnectStoragePoolVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    public ConnectStoragePoolVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().connectStoragePool(getParameters().getStoragePoolId().toString(),
                getParameters().getvds_spm_id(), getParameters().getStoragePoolId().toString(),
                getParameters().getMasterDomainId().toString(), getParameters().getMasterVersion());
        proceedProxyReturnValue();
    }

    // dont throw exception on errors except StoragePoolMasterNotFound for
    // master domain failure treatment
    @Override
    protected void proceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());
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
