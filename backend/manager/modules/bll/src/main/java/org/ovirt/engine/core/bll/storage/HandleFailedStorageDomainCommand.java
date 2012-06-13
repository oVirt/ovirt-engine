package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.action.ReconstructMasterParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.compat.TransactionScopeOption;

public class HandleFailedStorageDomainCommand<T extends StorageDomainPoolParametersBase> extends
        StorageDomainCommandBase<T> {
    private static final String STORAGE_DOMAIN_DOES_NOT_EXIST_MSG = "Storage domain %1$s does not exist";

    public HandleFailedStorageDomainCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        // This code is added in order to minimize a race which is described in
        // https://bugzilla.redhat.com/show_bug.cgi?id=702432
        // A race condition between HandleFailedStorageDomain and RemoveStorageDomainbool
        return super.canDoAction() && CheckStorageDomain();
    }

    @Override
    protected void executeCommand() {
        // Performing here a double check to see that storage is not null at beginning of execution.
        // As this code happens in negative flows which are more rare than positive flows, the penalty for
        // accessing the DB is less painful.
        if (!checkStorageDomainInDb()) {
            setSucceeded(false);
            log.error(String.format(STORAGE_DOMAIN_DOES_NOT_EXIST_MSG, getStorageDomain().getId()));
            return;
        }
        StorageDomainPoolParametersBase localParameters = getParameters();
        if (getStorageDomain().getstorage_domain_type() == StorageDomainType.Master) {
            // send required new and set succeeded to true
            // in order not to fail calling commands in case this failed
            ReconstructMasterParameters reconstructParameters = new ReconstructMasterParameters(
                    localParameters.getStoragePoolId(), localParameters.getStorageDomainId(), true);
            reconstructParameters.setTransactionScopeOption(TransactionScopeOption.RequiresNew);

            Backend.getInstance().runInternalAction(VdcActionType.ReconstructMasterDomain, reconstructParameters);
            setSucceeded(true);
        } else {
            localParameters.setIsInternal(true);
            localParameters.setInactive(true);
            setSucceeded(Backend.getInstance()
                    .runInternalAction(VdcActionType.DeactivateStorageDomain, localParameters).getSucceeded());
        }
    }
}
