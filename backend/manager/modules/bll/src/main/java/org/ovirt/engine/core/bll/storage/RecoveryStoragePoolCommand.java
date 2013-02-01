package org.ovirt.engine.core.bll.storage;

import java.util.concurrent.Callable;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.RecoveryStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
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

@NonTransactiveCommandAttribute
public class RecoveryStoragePoolCommand extends ReconstructMasterDomainCommand<RecoveryStoragePoolParameters> {

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

    @Override
    public NGuid getStorageDomainId() {
        super.setStorageDomainId(getMasterDomainIdFromDb());
        return super.getStorageDomainId();
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = super.canDoAction() && checkStoragePool();

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
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RECOVER_POOL);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
    }

    @Override
    protected void executeCommand() {
        if (StorageHelperDirector.getInstance().getItem(getStorageDomain().getstorage_type())
                .connectStorageToDomainByVdsId(getNewMaster(false), getVds().getId())) {
            getParameters().setStorageDomainId(getStorageDomainId().getValue());

            ((EventQueue) EjbUtils.findBean(BeanType.EVENTQUEUE_MANAGER, BeanProxyType.LOCAL)).submitEventSync(new Event(getParameters().getStoragePoolId(),
                    _newMasterStorageDomainId,
                    null,
                    EventType.RECOVERY),
                    new Callable<EventResult>() {
                        @Override
                        public EventResult call() {
                            StoragePoolIsoMap domainPoolMap =
                                    new StoragePoolIsoMap(getParameters()
                                            .getNewMasterDomainId(),
                                            getParameters().getStoragePoolId(),
                                            StorageDomainStatus.Active);
                            DbFacade.getInstance()
                                    .getStoragePoolIsoMapDao()
                                    .save(domainPoolMap);

                            getParameters().setVdsId(getVds().getId());
                            VdcReturnValueBase returnVal = getBackend().runInternalAction(
                                    VdcActionType.ReconstructMasterDomain, getParameters());

                            boolean succeeded = (returnVal.getActionReturnValue() != null) ?
                                    (Boolean) returnVal.getActionReturnValue() : false;

                            getStoragePoolDAO().updateStatus(getStoragePool().getId(),
                                    StoragePoolStatus.Problematic);

                            if (!succeeded) {
                                getStoragePoolIsoMapDAO().remove(domainPoolMap.getId());
                            }

                            setSucceeded(returnVal.getSucceeded());
                            return new EventResult(succeeded, EventType.RECONSTRUCT);
                        }
                    });
        } else {
            getReturnValue().setFault(new VdcFault(new VdcBLLException(VdcBllErrors.StorageServerConnectionError,
                    "Failed to connect storage"),
                    VdcBllErrors.StorageServerConnectionError));
        }
    }
}
