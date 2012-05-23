package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolWithStoragesParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage_pool_iso_map;
import org.ovirt.engine.core.common.vdscommands.AttachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDAO;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AttachStorageDomainToPoolCommand<T extends StorageDomainPoolParametersBase> extends
        StorageDomainCommandBase<T> {
    private storage_pool_iso_map map;

    public AttachStorageDomainToPoolCommand(T parameters) {
        super(parameters);
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
            if (getStoragePool().getstatus() == StoragePoolStatus.Uninitialized) {
                StoragePoolWithStoragesParameter parameters = new StoragePoolWithStoragesParameter(getStoragePool(),
                        new java.util.ArrayList<Guid>(
                                java.util.Arrays.asList(new Guid[] { getStorageDomain().getId() })),
                        getParameters().getSessionId());
                parameters.setIsInternal(true);
                parameters.setTransactionScopeOption(TransactionScopeOption.Suppress);
                setSucceeded(getBackend().runInternalAction(VdcActionType.AddStoragePoolWithStorages,
                        parameters,
                        new CommandContext(getCompensationContext()))
                        .getSucceeded());

            } else {
                map = getStoragePoolIsoMapDAO().get(new StoragePoolIsoMapId(getStorageDomain().getId(),
                        getParameters().getStoragePoolId()));
                if (map == null) {
                    executeInNewTransaction(new TransactionMethod<Object>() {

                        @Override
                        public Object runInTransaction() {
                            map = new storage_pool_iso_map(getStorageDomain().getId(), getParameters()
                                    .getStoragePoolId(), StorageDomainStatus.Locked);
                            getStoragePoolIsoMapDAO().save(map);
                            getCompensationContext().snapshotNewEntity(map);
                            getCompensationContext().stateChanged();
                            return null;
                        }
                    });
                    ConnectAllHostsToPool();
                    VDSReturnValue returnValue =
                            getBackend().getResourceManager()
                                    .RunVdsCommand(
                                            VDSCommandType.AttachStorageDomain,
                                            new AttachStorageDomainVDSCommandParameters(getParameters().getStoragePoolId(),
                                                    getParameters().getStorageDomainId()));
                    DiconnectAllHostsInPool();
                    executeInNewTransaction(new TransactionMethod<Object>() {
                        @Override
                        public Object runInTransaction() {
                            getCompensationContext().snapshotEntityStatus(map, map.getstatus());
                            map.setstatus(StorageDomainStatus.Maintenance);
                            getStoragePoolIsoMapDAO().updateStatus(map.getId(), map.getstatus());
                            if (getStorageDomain().getstorage_domain_type() == StorageDomainType.Master) {
                                CalcStoragePoolStatusByDomainsStatus();
                            }
                            getCompensationContext().stateChanged();
                            return null;
                        }
                    });
                    setSucceeded(returnValue.getSucceeded());
                }
            }
        }

    }

    protected void executeInNewTransaction(TransactionMethod<?> method) {
        TransactionSupport.executeInNewTransaction(method);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ATTACH_STORAGE_DOMAIN_TO_POOL
                : AuditLogType.USER_ATTACH_STORAGE_DOMAIN_TO_POOL_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        super.canDoAction();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ATTACH);

        // We can share only ISO or Export domain , or a data domain
        // which is not attached.
        boolean returnValue =
                checkStoragePool()
                        && InitializeVds() && CheckStorageDomain() && checkDomainCanBeAttached(getStorageDomain());

        if (returnValue && getStoragePool().getstatus() == StoragePoolStatus.Uninitialized
                && getStorageDomain().getstorage_domain_type() != StorageDomainType.Data) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ADD_STORAGE_POOL_WITHOUT_DATA_DOMAIN);
        }
        if (returnValue && getStoragePool().getstatus() != StoragePoolStatus.Uninitialized) {
            returnValue = CheckMasterDomainIsUp();
        }
        return returnValue;
    }

    protected StoragePoolIsoMapDAO getStoragePoolIsoMapDAO() {
        return getDbFacade().getStoragePoolIsoMapDAO();
    }
}
