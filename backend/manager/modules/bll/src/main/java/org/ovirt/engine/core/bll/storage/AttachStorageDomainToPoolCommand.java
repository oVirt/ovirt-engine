package org.ovirt.engine.core.bll.storage;

import java.util.Arrays;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolWithStoragesParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.vdscommands.AttachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AttachStorageDomainToPoolCommand<T extends AttachStorageDomainToPoolParameters> extends
        StorageDomainCommandBase<T> {
    private StoragePoolIsoMap map;

    public AttachStorageDomainToPoolCommand(T parameters) {
        this(parameters, null);
    }

    public AttachStorageDomainToPoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */

    protected AttachStorageDomainToPoolCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        if (getStorageDomain() != null) {
            if (getStoragePool().getStatus() == StoragePoolStatus.Uninitialized) {
                StoragePoolWithStoragesParameter parameters = new StoragePoolWithStoragesParameter(getStoragePool(),
                       Arrays.asList(getStorageDomain().getId()),
                        getParameters().getSessionId());
                parameters.setIsInternal(true);
                parameters.setTransactionScopeOption(TransactionScopeOption.Suppress);

                VdcReturnValueBase returnValue = runInternalAction(
                        VdcActionType.AddStoragePoolWithStorages,
                        parameters);
                setSucceeded(returnValue.getSucceeded());
                if (!returnValue.getSucceeded()) {
                    getReturnValue().setFault(returnValue.getFault());
                }
            } else {
                map = getStoragePoolIsoMapDAO().get(new StoragePoolIsoMapId(getStorageDomain().getId(),
                        getParameters().getStoragePoolId()));
                if (map == null) {
                    executeInNewTransaction(new TransactionMethod<Object>() {

                        @Override
                        public Object runInTransaction() {
                            map = new StoragePoolIsoMap(getStorageDomain().getId(), getParameters()
                                    .getStoragePoolId(), StorageDomainStatus.Locked);
                            getStoragePoolIsoMapDAO().save(map);
                            getCompensationContext().snapshotNewEntity(map);
                            getCompensationContext().stateChanged();
                            return null;
                        }
                    });
                    connectAllHostsToPool();
                    runVdsCommand(VDSCommandType.AttachStorageDomain,
                            new AttachStorageDomainVDSCommandParameters(getParameters().getStoragePoolId(),
                                    getParameters().getStorageDomainId()));
                    executeInNewTransaction(new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            final StorageDomainType sdType = getStorageDomain().getStorageDomainType();
                            map.setStatus(StorageDomainStatus.Maintenance);
                            getStoragePoolIsoMapDAO().updateStatus(map.getId(), map.getStatus());

                            if (sdType == StorageDomainType.Master) {
                                calcStoragePoolStatusByDomainsStatus();
                            }

                            // upgrade the domain format to the storage pool format
                            if (sdType == StorageDomainType.Data || sdType == StorageDomainType.Master) {
                                updateStorageDomainFormat(getStorageDomain());
                            }
                            return null;
                        }
                    });
                    if (getParameters().getActivate()) {
                        attemptToActivateDomain();
                    }
                    setSucceeded(true);
                }
            }
        }

    }

    protected void attemptToActivateDomain() {
        StorageDomainPoolParametersBase activateParameters = new StorageDomainPoolParametersBase(getStorageDomain().getId(),
                getStoragePool().getId());
        getBackend()
                .runInternalAction(VdcActionType.ActivateStorageDomain, activateParameters);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ATTACH_STORAGE_DOMAIN_TO_POOL
                : AuditLogType.USER_ATTACH_STORAGE_DOMAIN_TO_POOL_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        // We can share only ISO or Export domain , or a data domain
        // which is not attached.
        boolean returnValue =
                checkStoragePool()
                        && initializeVds() && checkStorageDomain() && checkDomainCanBeAttached(getStorageDomain());

        if (returnValue && getStoragePool().getStatus() == StoragePoolStatus.Uninitialized
                && getStorageDomain().getStorageDomainType() != StorageDomainType.Data) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ADD_STORAGE_POOL_WITHOUT_DATA_DOMAIN);
        }
        if (returnValue && getStoragePool().getStatus() != StoragePoolStatus.Uninitialized) {
            returnValue = checkMasterDomainIsUp();
        }
        return returnValue;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ATTACH);
    }
}
