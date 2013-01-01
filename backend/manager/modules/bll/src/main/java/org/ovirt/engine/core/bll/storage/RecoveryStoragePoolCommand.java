package org.ovirt.engine.core.bll.storage;

import java.util.concurrent.Callable;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.RecoveryStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.ejb.BeanProxyType;
import org.ovirt.engine.core.utils.ejb.BeanType;
import org.ovirt.engine.core.utils.ejb.EjbUtils;

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

    protected void executeReconstruct(){
        super.executeCommand();
    }

    @Override
    protected void executeCommand() {
        try {
            if (StorageHelperDirector.getInstance().getItem(getStorageDomain().getstorage_type())
                    .connectStorageToDomainByVdsId(getNewMaster(false), getVds().getId())) {
                getRecoveryStoragePoolParametersData().setStorageDomainId(getStorageDomainId().getValue());
                ((EventQueue) EjbUtils.findBean(BeanType.EVENTQUEUE_MANAGER, BeanProxyType.LOCAL)).submitEventSync(new Event(getRecoveryStoragePoolParametersData().getStoragePoolId(),
                        _newMasterStorageDomainId,
                        null,
                        EventType.RECOVERY),
                        new Callable<EventResult>() {
                            @Override
                            public EventResult call() {
                                // set those to null in order to reload them during canDoAction -
                                // canDo checks should be performed on updated values and not staled ones.
                                // canDoAction checks are needed here as Recovery operations aren't cleared from the event queue
                                // after reconstruct, in order to provide the user ability to recover from the state of non working pool
                                // without him to be in a race with automatic triggered reconstruct. because of that, we don't want to run
                                // the recovery operation if it's unneeded so we need to re-check the canDoAction result. canDoAction() method was
                                // as is in order to provide the user immediate response whether it's possible to initiate the command when
                                // he attempts to run recovery.
                                setStorageDomain(null);
                                setStoragePool(null);
                                if (canDoAction()) {
                                    StoragePoolIsoMap domainPoolMap =
                                            new StoragePoolIsoMap(getRecoveryStoragePoolParametersData()
                                                    .getNewMasterDomainId(),
                                                    getRecoveryStoragePoolParametersData().getStoragePoolId(),
                                                    StorageDomainStatus.Active);
                                    DbFacade.getInstance()
                                            .getStoragePoolIsoMapDao()
                                            .save(domainPoolMap);

                                    getStoragePool().setstatus(StoragePoolStatus.Problematic);
                                    executeReconstruct();
                                }
                                return new EventResult(reconstructOpSucceeded, EventType.RECONSTRUCT);
                            }
                        });
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
