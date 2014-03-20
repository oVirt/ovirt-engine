package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolWithStoragesParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VersionStorageFormatUtil;
import org.ovirt.engine.core.common.vdscommands.CreateStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddStoragePoolWithStoragesCommand<T extends StoragePoolWithStoragesParameter> extends
        UpdateStoragePoolCommand<T> {
    public AddStoragePoolWithStoragesCommand(T parameters) {
        this(parameters, null);
    }

    public AddStoragePoolWithStoragesCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */

    protected AddStoragePoolWithStoragesCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    private StorageDomain masterStorageDomain = null;
    VDSReturnValue retVal;

    @Override
    protected void executeCommand() {
        if (UpdateStorageDomainsInDb()) {
            // setting storage pool status to maintenance
            StoragePool storagePool = getStoragePool();
            getCompensationContext().snapshotEntity(storagePool);
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
                @Override
                public Object runInTransaction() {
                    getStoragePool().setStatus(StoragePoolStatus.Maintenance);
                    getStoragePool().setStoragePoolFormatType(masterStorageDomain.getStorageFormat());
                    DbFacade.getInstance().getStoragePoolDao().update(getStoragePool());
                    getCompensationContext().stateChanged();
                    StoragePoolStatusHandler.poolStatusChanged(getStoragePool().getId(),
                            getStoragePool().getStatus());
                    return null;
                }
            });

            // Following code performs only read operations, therefore no need for new transaction
            boolean result = false;
            retVal = null;
            for (VDS vds : getAllRunningVdssInPool()) {
                setVds(vds);
                for (Guid storageDomainId : getParameters().getStorages()) {
                    // now the domain should have the mapping
                    // with the pool in db
                    StorageDomain storageDomain =
                            DbFacade.getInstance()
                                    .getStorageDomainDao()
                                    .getForStoragePool(storageDomainId,
                                            getStoragePool().getId());
                    StorageHelperDirector.getInstance()
                            .getItem(storageDomain.getStorageType())
                            .connectStorageToDomainByVdsId(storageDomain,
                                    getVds().getId());
                }
                retVal = addStoragePoolInIrs();
                if (!retVal.getSucceeded()
                        && retVal.getVdsError().getCode() == VdcBllErrors.StorageDomainAccessError) {
                    log.warnFormat("Error creating storage pool on vds {0} - continuing",
                            vds.getName());
                    continue;
                } else {
                    // storage pool creation succeeded or failed
                    // but didn't throw exception
                    result = retVal.getSucceeded();
                    break;
                }
            }

            setSucceeded(result);
            if (!result) {
                if (retVal != null && retVal.getVdsError().getCode() != null) {
                    throw new VdcBLLException(retVal.getVdsError().getCode(), retVal.getVdsError().getMessage());
                } else {
                    // throw exception to cause rollback and stop the
                    // command
                    throw new VdcBLLException(VdcBllErrors.ENGINE_ERROR_CREATING_STORAGE_POOL);
                }
            }
        }

        // Create pool phase completed, no rollback is needed here, so compensation information needs to be cleared!
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                getCompensationContext().resetCompensation();
                return null;
            }
        });
        freeLock();
        // if create succeeded activate
        if (getSucceeded()) {
            ActivateStorageDomains();
        }
    }

    private boolean UpdateStorageDomainsInDb() {
        boolean result  = TransactionSupport.executeInNewTransaction(new TransactionMethod<Boolean>() {

            @Override
            public Boolean runInTransaction() {
                for (Guid storageDomainId : getParameters().getStorages()) {
                    StorageDomain storageDomain = DbFacade.getInstance().getStorageDomainDao().get(
                                storageDomainId);
                    if (storageDomain != null) {
                        StoragePoolIsoMap mapFromDB =
                            DbFacade.getInstance()
                            .getStoragePoolIsoMapDao()
                                        .get(new StoragePoolIsoMapId(storageDomain.getId(), getStoragePool().getId()));
                        boolean existingInDb = mapFromDB != null;
                        if (existingInDb) {
                            getCompensationContext().snapshotEntity(mapFromDB);
                        }
                        final StorageDomainStatic staticDomain = storageDomain.getStorageStaticData();
                        boolean staticDomainChanged = false;
                        StorageFormatType requiredFormatType =
                                VersionStorageFormatUtil.getRequiredForVersion
                                        (getStoragePool().getcompatibility_version(), storageDomain.getStorageType());
                        if (staticDomain.getStorageFormat().compareTo(requiredFormatType) < 0) {
                            if (!staticDomainChanged) {
                                getCompensationContext().snapshotEntity(staticDomain);
                            }
                            staticDomain.setStorageFormat(requiredFormatType);
                            staticDomainChanged = true;
                        }
                        storageDomain.setStoragePoolId(getStoragePool().getId());
                        if (masterStorageDomain == null
                                && storageDomain.getStorageDomainType() == StorageDomainType.Data) {
                            if (!staticDomainChanged) {
                                getCompensationContext().snapshotEntity(staticDomain);
                            }
                            storageDomain.setStorageDomainType(StorageDomainType.Master);
                            staticDomainChanged = true;
                            masterStorageDomain = storageDomain;
                            // The update of storage pool should be without compensation,
                            // this is why we run it in a different SUPRESS transaction.
                            updateStoragePoolMasterDomainVersionInDiffTransaction();
                        }
                        if (staticDomainChanged) {
                            getStorageDomainStaticDAO().update(staticDomain);
                        }
                        storageDomain.setStatus(StorageDomainStatus.Locked);
                        if (existingInDb) {
                            DbFacade.getInstance()
                                        .getStoragePoolIsoMapDao()
                                        .update(storageDomain.getStoragePoolIsoMapData());
                        } else {
                            DbFacade.getInstance()
                                        .getStoragePoolIsoMapDao()
                                        .save(storageDomain.getStoragePoolIsoMapData());
                            getCompensationContext().snapshotNewEntity(storageDomain.getStoragePoolIsoMapData());
                        }
                    } else {
                        return false;
                    }
                }
                getCompensationContext().stateChanged();
                return true;
            }
        });
        return result && masterStorageDomain != null;
    }

    /**
     * Save the master version out of the transaction
     */

    private VDSReturnValue addStoragePoolInIrs() {
        return runVdsCommand(VDSCommandType.CreateStoragePool,
                new CreateStoragePoolVDSCommandParameters(getVds().getId(), getStoragePool().getId(),
                        getStoragePool().getName(),
                        masterStorageDomain.getId(), getParameters().getStorages(), getStoragePool()
                                .getmaster_domain_version()));
    }

    private boolean ActivateStorageDomains() {
        boolean returnValue = true;
        for (final Guid storageDomainId : getParameters().getStorages()) {
            StorageDomainPoolParametersBase activateParameters = new StorageDomainPoolParametersBase(storageDomainId,
                    getStoragePool().getId());
            activateParameters.setSessionId(getParameters().getSessionId());
            activateParameters.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
            returnValue = Backend.getInstance()
                    .runInternalAction(VdcActionType.ActivateStorageDomain, activateParameters).getSucceeded();

            // if activate domain failed then set domain status to inactive
            if (!returnValue) {
                TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                    @Override
                    public Void runInTransaction() {
                        DbFacade.getInstance()
                                .getStoragePoolIsoMapDao()
                                .updateStatus(
                                        new StoragePoolIsoMapId(storageDomainId, getStoragePool().getId()),
                                        StorageDomainStatus.Inactive);
                        return null;
                    }
                });
            }
        }

        return returnValue;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__STORAGE__DOMAIN);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ATTACH_ACTION_TO);
    }

    private boolean checkStorageDomainsInPool() {
        if (!getParameters().getIsInternal()) {
            boolean _hasData = false;
            StorageFormatType storageFormat = null;
            for (Guid storageDomainId : getParameters().getStorages()) {
                StorageDomain domain = DbFacade.getInstance().getStorageDomainDao().get(storageDomainId);
                if (isStorageDomainNotNull(domain) && checkDomainCanBeAttached(domain)) {
                    if (domain.getStorageDomainType() == StorageDomainType.Data) {
                        _hasData = true;
                        if (storageFormat == null) {
                            storageFormat = domain.getStorageFormat();
                        } else if (storageFormat != domain.getStorageFormat()) {
                            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ADD_STORAGE_POOL_WITH_DIFFERENT_STORAGE_FORMAT);
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
            if (!_hasData) {
                addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_ADD_STORAGE_POOL_WITHOUT_DATA_AND_ISO_DOMAINS);
                return false;
            }
        }
        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ATTACH_STORAGE_DOMAINS_TO_POOL
                : AuditLogType.USER_ATTACH_STORAGE_DOMAINS_TO_POOL_FAILED;
    }

    @Override
    protected boolean canDoAction() {
        boolean returnValue = super.canDoAction() && checkStoragePool()
                && checkStoragePoolStatus(StoragePoolStatus.Uninitialized) && initializeVds()
                && checkStorageDomainsInPool();
        return returnValue;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getStoragePoolId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.POOL, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }
}
