package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.RecoveryStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
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
        boolean returnValue = super.canDoAction() && CheckStoragePool();
        getReturnValue().getCanDoActionMessages().remove(VdcBllMessages.VAR__ACTION__RECONSTRUCT_MASTER.toString());

        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RECOVER_POOL);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
        if (returnValue) {
            if (getStoragePool().getstatus() == StoragePoolStatus.Uninitialized) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
                return false;
            } else if (getStorageDomain() != null && getStorageDomain().getstatus() != null
                    && getStorageDomain().getstatus() == StorageDomainStatus.Active) {
                getReturnValue().getCanDoActionMessages().add(
                        VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2.toString());
                returnValue = false;
            } else if (electNewMaster() != null) {
                getReturnValue().getCanDoActionMessages().add(
                        VdcBllMessages.STORAGE_POOL_REINITIALIZE_WITH_MORE_THAN_ONE_DATA_DOMAIN.toString());
                returnValue = false;
            } else {
                storage_domains domain = DbFacade.getInstance().getStorageDomainDAO().get(
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
        TransactionSupport.executeInNewTransaction(
                new TransactionMethod<Object>() {
                    @Override
                    public Object runInTransaction() {
                        storage_pool_iso_map domainPoolMap =
                                new storage_pool_iso_map(getRecoveryStoragePoolParametersData()
                                        .getNewMasterDomainId(),
                                        getRecoveryStoragePoolParametersData().getStoragePoolId(),
                                        StorageDomainStatus.Active);
                        DbFacade.getInstance().getStoragePoolIsoMapDAO().save(domainPoolMap);
                        getCompensationContext().snapshotNewEntity(domainPoolMap);
                        getCompensationContext().stateChanged();
                        return null;
                    }
                });
        getStoragePool().setstatus(StoragePoolStatus.Problematic);
        if (StorageHelperDirector.getInstance().getItem(getStorageDomain().getstorage_type())
                .ConnectStorageToDomainByVdsId(getNewMaster(), getVds().getId())) {
            super.executeCommand();
        } else {
            getReturnValue().setFault(new VdcFault(new VdcBLLException(VdcBllErrors.StorageServerConnectionError,
                    "Failed to connect storage"),
                    VdcBllErrors.StorageServerConnectionError));
        }
    }
}
