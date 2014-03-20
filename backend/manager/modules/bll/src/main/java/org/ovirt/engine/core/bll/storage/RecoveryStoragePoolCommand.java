package org.ovirt.engine.core.bll.storage;

import java.util.concurrent.Callable;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RecoveryStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainSharedStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.eventqueue.Event;
import org.ovirt.engine.core.common.eventqueue.EventQueue;
import org.ovirt.engine.core.common.eventqueue.EventResult;
import org.ovirt.engine.core.common.eventqueue.EventType;
import org.ovirt.engine.core.compat.Guid;
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
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties;
    }

    @Override
    public Guid getStorageDomainId() {
        super.setStorageDomainId(getMasterDomainIdFromDb());
        return super.getStorageDomainId();
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = checkStoragePool() && super.canDoAction();

        if (returnValue) {
            if (getStoragePool().getStatus() == StoragePoolStatus.Uninitialized) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_STATUS_ILLEGAL);
                return false;
            } else if (getStorageDomain() != null && getStorageDomain().getStatus() != null
                    && getStorageDomain().getStatus() == StorageDomainStatus.Active) {
                addStorageDomainStatusIllegalMessage();
                returnValue = false;
            } else if (electNewMaster(false) != null) {
                getReturnValue().getCanDoActionMessages().add(
                        VdcBllMessages.STORAGE_POOL_REINITIALIZE_WITH_MORE_THAN_ONE_DATA_DOMAIN.toString());
                returnValue = false;
            } else {
                StorageDomain domain = loadTargetedMasterDomain();
                if (domain.getStorageDomainSharedStatus() != StorageDomainSharedStatus.Unattached) {
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL);
                    returnValue = false;
                }
            }
        }
        return returnValue;
    }

    private StorageDomain loadTargetedMasterDomain() {
        return getStorageDomainDAO().get(
                _newMasterStorageDomainId);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__RECOVER_POOL);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
    }

    @Override
    protected void executeCommand() {
        if (StorageHelperDirector.getInstance().getItem(getNewMaster(false).getStorageType())
                .connectStorageToDomainByVdsId(getNewMaster(false), getVds().getId())) {

            ((EventQueue) EjbUtils.findBean(BeanType.EVENTQUEUE_MANAGER, BeanProxyType.LOCAL)).submitEventSync(new Event(getParameters().getStoragePoolId(),
                    _newMasterStorageDomainId,
                    null,
                    EventType.RECOVERY, ""),
                    new Callable<EventResult>() {
                        @Override
                        public EventResult call() {
                            getParameters().setStorageDomainId(getMasterDomainIdFromDb());
                            StoragePoolIsoMap domainPoolMap =
                                    new StoragePoolIsoMap(getParameters()
                                            .getNewMasterDomainId(),
                                            getParameters().getStoragePoolId(),
                                            StorageDomainStatus.Active);
                            DbFacade.getInstance()
                                    .getStoragePoolIsoMapDao()
                                    .save(domainPoolMap);

                            getParameters().setVdsId(getVds().getId());
                            VdcReturnValueBase returnVal = runInternalAction(
                                    VdcActionType.ReconstructMasterDomain, getParameters(), cloneContextAndDetachFromParent());

                            boolean reconstructVerbExecuted = (returnVal.getActionReturnValue() != null) ?
                                    (Boolean) returnVal.getActionReturnValue() : false;

                            getStoragePoolDAO().updateStatus(getStoragePool().getId(),
                                    StoragePoolStatus.NonResponsive);

                            if (!reconstructVerbExecuted) {
                                getStoragePoolIsoMapDAO().remove(domainPoolMap.getId());
                            }

                            if (returnVal.getSucceeded()) {
                                updateStorageDomainFormat(loadTargetedMasterDomain());
                            }

                            setSucceeded(returnVal.getSucceeded());
                            return new EventResult(reconstructVerbExecuted, EventType.RECONSTRUCT);
                        }
                    });
        } else {
            getReturnValue().setFault(new VdcFault(new VdcBLLException(VdcBllErrors.StorageServerConnectionError,
                    "Failed to connect storage"),
                    VdcBllErrors.StorageServerConnectionError));
        }
    }
}
