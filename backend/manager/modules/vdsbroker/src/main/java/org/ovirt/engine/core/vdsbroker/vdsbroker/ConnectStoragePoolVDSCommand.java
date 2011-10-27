package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.errors.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class ConnectStoragePoolVDSCommand<P extends ConnectStoragePoolVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    public ConnectStoragePoolVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().connectStoragePool(getParameters().getStoragePoolId().toString(),
                getParameters().getvds_spm_id(), getParameters().getStoragePoolId().toString(),
                getParameters().getMasterDomainId().toString(), getParameters().getMasterVersion());
        ProceedProxyReturnValue();
    }

    // dont throw exception on errors except StoragePoolMasterNotFound for
    // master domain failure treatment
    @Override
    protected void ProceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case Done:
        case StoragePoolMasterNotFound:
        case StoragePoolTooManyMasters:
        case StoragePoolWrongMaster:
            super.ProceedProxyReturnValue();
            break;
        default:
            getVDSReturnValue().setSucceeded(false);
            InitializeVdsError(returnStatus);
            break;
        }
    }
}
