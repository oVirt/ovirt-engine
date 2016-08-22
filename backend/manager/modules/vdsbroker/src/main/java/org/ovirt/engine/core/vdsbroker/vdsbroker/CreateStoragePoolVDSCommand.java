package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.vdscommands.CreateStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;

public class CreateStoragePoolVDSCommand<P extends CreateStoragePoolVDSCommandParameters> extends VdsBrokerCommand<P> {
    public CreateStoragePoolVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        Guid[] guids = getParameters().getDomainsIdList().toArray(new Guid[0]);
        String[] ids = new String[guids.length];
        for (int i = 0; i < guids.length; i++) {
            ids[i] = guids[i].toString();
        }

        //The first parameter, poolType parameter is ignored by VDSM and thus can be set to any arbitrary value
        status = getBroker().createStoragePool(0,
                getParameters().getStoragePoolId().toString(), getParameters().getStoragePoolName(),
                getParameters().getMasterDomainId().toString(), ids, getParameters().getMasterVersion(),
                Config.getValue(ConfigValues.LockPolicy),
                Config.getValue(ConfigValues.LockRenewalIntervalSec),
                Config.getValue(ConfigValues.LeaseTimeSec),
                Config.getValue(ConfigValues.IoOpTimeoutSec),
                Config.getValue(ConfigValues.LeaseRetries));
        proceedProxyReturnValue();
    }

    @Override
    protected void proceedProxyReturnValue() {
        EngineError returnStatus = getReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        // fail the operation without throwing exception
        case StorageDomainAccessError:
            getVDSReturnValue().setSucceeded(false);
            VDSError tempVar = new VDSError();
            tempVar.setCode(EngineError.StorageDomainAccessError);
            tempVar.setMessage(getReturnStatus().message);
            getVDSReturnValue().setVdsError(tempVar);
            break;

        default:
            super.proceedProxyReturnValue();
            initializeVdsError(returnStatus);
            break;
        }
    }
}
