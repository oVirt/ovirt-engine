package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.RecoveryStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class RecoveryStoragePoolCommand extends ReconstructMasterDomainCommand {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected RecoveryStoragePoolCommand(Guid commandId) {
        super(commandId);
    }

    public RecoveryStoragePoolCommand(RecoveryStoragePoolParameters parameters) {
        super(parameters);
        _newMasterStorageDomainId = getRecoveryStoragePoolParametersData().getNewMasterDomainId();
    }

    private RecoveryStoragePoolParameters getRecoveryStoragePoolParametersData() {
        VdcActionParametersBase tempVar = getParameters();
        return (RecoveryStoragePoolParameters) ((tempVar instanceof RecoveryStoragePoolParameters) ? tempVar : null);
    }

    @Override
    public NGuid getStorageDomainId() {
        super.setStorageDomainId(getMasterDomainIdFromDb());
        return super.getStorageDomainId();
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = super.canDoAction() && checkStoragePool();
        getReturnValue().getCanDoActionMessages().remove(VdcBllMessages.VAR__ACTION__RECONSTRUCT_MASTER.toString());

        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RECOVER_POOL);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
        if (returnValue) {
            if (getStoragePool().getstatus() == StoragePoolStatus.Uninitialized) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
                return false;
            } else if (getStorageDomain() != null && getStorageDomain().getstatus() != null
                    && getStorageDomain().getstatus() == StorageDomainStatus.Active) {
                addInvalidSDStatusMessage(getStorageDomain().getstatus());
                returnValue = false;
            } else if (electNewMaster(false) != null) {
                getReturnValue().getCanDoActionMessages().add(
                        VdcBllMessages.STORAGE_POOL_REINITIALIZE_WITH_MORE_THAN_ONE_DATA_DOMAIN.toString());
                returnValue = false;
            } else {
                storage_domains domain = DbFacade.getInstance().getStorageDomainDao().get(
                        _newMasterStorageDomainId);
                if (domain.getstorage_domain_shared_status() != StorageDomainSharedStatus.Unattached) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
                    returnValue = false;
                } else if (domain.getstorage_type() != getStoragePool().getstorage_pool_type()) {
                    addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_RECOVERY_STORAGE_POOL_STORAGE_TYPE_MISSMATCH);
                    returnValue = false;
                }
            }
        }
        return returnValue;
    }

    @Override
    protected void executeCommand() {
        StoragePoolIsoMap domainPoolMap =
        TransactionSupport.executeInNewTransaction(
                new TransactionMethod<StoragePoolIsoMap>() {
                    @Override
                    public StoragePoolIsoMap runInTransaction() {
                        StoragePoolIsoMap domainPoolMap =
                                new StoragePoolIsoMap(getRecoveryStoragePoolParametersData()
                                        .getNewMasterDomainId(),
                                        getRecoveryStoragePoolParametersData().getStoragePoolId(),
                                        StorageDomainStatus.Active);
                        DbFacade.getInstance().getStoragePoolIsoMapDao().save(domainPoolMap);
                        return domainPoolMap;
                    }
                });
        getStoragePool().setstatus(StoragePoolStatus.Problematic);
        try {
            if (StorageHelperDirector.getInstance().getItem(getStorageDomain().getstorage_type())
                    .connectStorageToDomainByVdsId(getNewMaster(false), getVds().getId())) {
                super.executeCommand();
            } else {
                getReturnValue().setFault(new VdcFault(new VdcBLLException(VdcBllErrors.StorageServerConnectionError,
                        "Failed to connect storage"),
                        VdcBllErrors.StorageServerConnectionError));
            }
        } finally {
            if (!reconstructOpSucceeded) {
                getStoragePoolIsoMapDAO().remove(new StoragePoolIsoMapId(getRecoveryStoragePoolParametersData()
                        .getNewMasterDomainId(), getRecoveryStoragePoolParametersData().getStoragePoolId()));
            }
        }
    }
}
