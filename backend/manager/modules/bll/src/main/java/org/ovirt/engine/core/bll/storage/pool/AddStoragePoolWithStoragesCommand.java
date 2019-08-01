package org.ovirt.engine.core.bll.storage.pool;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainToPoolRelationValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolWithStoragesParameter;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VersionStorageFormatUtil;
import org.ovirt.engine.core.common.vdscommands.CreateStoragePoolVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.StorageDomainVdsCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddStoragePoolWithStoragesCommand<T extends StoragePoolWithStoragesParameter> extends
        UpdateStoragePoolCommand<T> {

    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private UnregisteredOVFDataDao unregisteredOVFDataDao;

    public AddStoragePoolWithStoragesCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public AddStoragePoolWithStoragesCommand(Guid commandId) {
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
        if (updateStorageDomainsInDb()) {
            // setting storage pool status to maintenance
            StoragePool storagePool = getStoragePool();
            getCompensationContext().snapshotEntity(storagePool);
            TransactionSupport.executeInNewTransaction(() -> {
                getStoragePool().setStatus(StoragePoolStatus.Maintenance);
                getStoragePool().setStoragePoolFormatType(masterStorageDomain.getStorageFormat());
                storagePoolDao.update(getStoragePool());
                getCompensationContext().stateChanged();
                storagePoolStatusHandler.poolStatusChanged(getStoragePool().getId(),
                        getStoragePool().getStatus());
                return null;
            });

            // Following code performs only read operations, therefore no need for new transaction
            boolean result = false;

            // Once we create a storage pool with multiple hosts, the engine should connect all
            // the hosts in the storage pool,
            // since the engine picks a random host to fetch all the unregistered disks.
            boolean isStoragePoolCreated = false;
            retVal = null;
            for (VDS vds : getAllRunningVdssInPool()) {
                setVds(vds);
                for (Guid storageDomainId : getParameters().getStorages()) {
                    // now the domain should have the mapping
                    // with the pool in db
                    StorageDomain storageDomain =
                            storageDomainDao.getForStoragePool(storageDomainId, getStoragePool().getId());
                    storageHelperDirector.getItem(storageDomain.getStorageType())
                            .connectStorageToDomainByVdsId(storageDomain,
                                    getVds().getId());
                }
                if (!isStoragePoolCreated) {
                    // storage pool creation succeeded or failed
                    // but didn't throw exception
                    if (!cleanDirtyMetaDataIfNeeded()) {
                        result = false;
                    } else {
                        retVal = addStoragePoolInIrs();
                        if (!retVal.getSucceeded()
                                && retVal.getVdsError().getCode() == EngineError.StorageDomainAccessError) {
                            log.warn("Error creating storage pool on vds '{}' - continuing",
                                    vds.getName());
                            continue;
                        }
                        result = retVal.getSucceeded();
                    }
                    isStoragePoolCreated = true;
                }
            }

            setSucceeded(result);
            if (!result) {
                if (retVal != null && retVal.getVdsError().getCode() != null) {
                    throw new EngineException(retVal.getVdsError().getCode(), retVal.getVdsError().getMessage());
                } else {
                    // throw exception to cause rollback and stop the
                    // command
                    throw new EngineException(EngineError.ENGINE_ERROR_CREATING_STORAGE_POOL);
                }
            }
            registerOvfStoreDisks();
        }

        // Create pool phase completed, no rollback is needed here, so compensation information needs to be cleared!
        TransactionSupport.executeInNewTransaction(() -> {
            getCompensationContext().cleanupCompensationDataAfterSuccessfulCommand();
            return null;
        });
        freeLock();
        // if create succeeded activate
        if (getSucceeded()) {
            activateStorageDomains();
        }
    }

    private boolean updateStorageDomainsInDb() {
        boolean result  = TransactionSupport.executeInNewTransaction(() -> {
            for (Guid storageDomainId : getParameters().getStorages()) {
                StorageDomain storageDomain = storageDomainDao.get(storageDomainId);
                if (storageDomain != null) {
                    StoragePoolIsoMap mapFromDB =
                            storagePoolIsoMapDao.get(new StoragePoolIsoMapId(storageDomain.getId(), getStoragePool().getId()));
                    boolean existingInDb = mapFromDB != null;
                    if (existingInDb) {
                        getCompensationContext().snapshotEntity(mapFromDB);
                    }
                    final StorageDomainStatic staticDomain = storageDomain.getStorageStaticData();
                    boolean staticDomainChanged = false;
                    StorageFormatType requiredFormatType =
                            VersionStorageFormatUtil.getForVersion(getStoragePool().getCompatibilityVersion());
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
                        storageDomainStaticDao.update(staticDomain);
                    }
                    storageDomain.setStatus(StorageDomainStatus.Locked);
                    if (existingInDb) {
                        storagePoolIsoMapDao.update(storageDomain.getStoragePoolIsoMapData());
                    } else {
                        storagePoolIsoMapDao.save(storageDomain.getStoragePoolIsoMapData());
                        getCompensationContext().snapshotNewEntity(storageDomain.getStoragePoolIsoMapData());
                    }
                } else {
                    return false;
                }
            }
            getCompensationContext().stateChanged();
            return true;
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
                                .getMasterDomainVersion()));
    }

    private boolean activateStorageDomains() {
        boolean returnValue = true;
        for (final Guid storageDomainId : getParameters().getStorages()) {
            StorageDomainPoolParametersBase activateParameters = new StorageDomainPoolParametersBase(storageDomainId,
                    getStoragePool().getId());
            activateParameters.setSessionId(getParameters().getSessionId());
            activateParameters.setTransactionScopeOption(TransactionScopeOption.RequiresNew);
            returnValue =
                    backend.runInternalAction(ActionType.ActivateStorageDomain, activateParameters).getSucceeded();

            // if activate domain failed then set domain status to inactive
            if (!returnValue) {
                TransactionSupport.executeInNewTransaction(() -> {
                    storagePoolIsoMapDao.updateStatus(
                            new StoragePoolIsoMapId(storageDomainId, getStoragePool().getId()),
                            StorageDomainStatus.Inactive);
                    return null;
                });
            }
        }

        return returnValue;
    }

    private void registerOvfStoreDisks() {
        for (final Guid storageDomainId : getParameters().getStorages()) {
            if (storageDomainStaticDao.get(storageDomainId).getStorageDomainType().isDataDomain()) {
                resetOvfStoreAndUnregisteredDisks();
                TransactionSupport.executeInNewTransaction(() -> {
                    List<DiskImage> ovfStoreDiskImages = getAllOVFDisks(storageDomainId, getStoragePool().getId());
                    registerAllOvfDisks(ovfStoreDiskImages, storageDomainId);

                    List<OvfEntityData> entitiesFromStorageOvfDisk =
                            getEntitiesFromStorageOvfDisk(storageDomainId, getStoragePool().getId());
                    // Update unregistered entities
                    for (Object ovf : entitiesFromStorageOvfDisk) {
                        OvfEntityData ovfEntityData = (OvfEntityData) ovf;
                        unregisteredOVFDataDao.removeEntity(ovfEntityData.getEntityId(), storageDomainId);
                        unregisteredOVFDataDao.saveOVFData(ovfEntityData);
                        log.info("Adding OVF data of entity id '{}' and entity name '{}'",
                                ovfEntityData.getEntityId(),
                                ovfEntityData.getEntityName());
                    }
                    initUnregisteredDisksToDB(storageDomainId);
                    return null;
                });
            }
        }
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__DOMAIN);
        addValidationMessage(EngineMessage.VAR__ACTION__ATTACH_ACTION_TO);
    }

    private boolean checkStorageDomainsInPool() {
        if (!getParameters().getIsInternal()) {
            boolean hasData = false;
            StorageFormatType storageFormat = null;
            for (Guid storageDomainId : getParameters().getStorages()) {
                StorageDomain domain = storageDomainDao.get(storageDomainId);
                StorageDomainToPoolRelationValidator
                        storageDomainToPoolRelationValidator = new StorageDomainToPoolRelationValidator(domain.getStorageStaticData(), getStoragePool());
                if (isStorageDomainNotNull(domain) && validate(storageDomainToPoolRelationValidator.validateDomainCanBeAttachedToPool())) {
                    if (domain.getStorageDomainType() == StorageDomainType.Data) {
                        hasData = true;
                        if (storageFormat == null) {
                            storageFormat = domain.getStorageFormat();
                        } else if (storageFormat != domain.getStorageFormat()) {
                            addValidationMessage(EngineMessage.ERROR_CANNOT_ADD_STORAGE_POOL_WITH_DIFFERENT_STORAGE_FORMAT);
                            return false;
                        }
                    }
                } else {
                    return false;
                }
            }
            if (!hasData) {
                addValidationMessage(EngineMessage.ERROR_CANNOT_ADD_STORAGE_POOL_WITHOUT_DATA_AND_ISO_DOMAINS);
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
    protected boolean validate() {
        StoragePoolValidator spValidator = createStoragePoolValidator();
        return super.validate()
                && validate(spValidator.exists())
                && validate(spValidator.isInStatus(StoragePoolStatus.Uninitialized))
                && initializeVds()
                && checkStorageDomainsInPool();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getStoragePoolId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.POOL, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    private boolean cleanDirtyMetaDataIfNeeded() {
        if (getStoragePool().getStatus() == StoragePoolStatus.Maintenance) {
            for (Guid storageDomainId : getParameters().getStorages()) {
                StorageDomain domain = storageDomainDao.get(storageDomainId);
                if (domain.getStorageDomainType().isDataDomain() &&
                        isStorageDomainAttachedToStoragePool(domain) &&
                        !detachStorageDomainSucceeded(storageDomainId)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean detachStorageDomainSucceeded(Guid storageDomainId) {
        log.info("Domain '{}' is already attached to a different storage pool, clean the storage domain metadata.",
                storageDomainId);
        StorageDomainVdsCommandParameters params =
                new StorageDomainVdsCommandParameters(storageDomainId, getVds().getId());
        VDSReturnValue ret = runVdsCommand(VDSCommandType.CleanStorageDomainMetaData, params);
        if (!ret.getSucceeded()) {
            log.error("Failed to clean metadata for storage domain '{}'.",
                    storageDomainId);
            return false;
        }
        log.info("Successfully cleaned metadata for storage domain '{}'.",
                storageDomainId);
        return true;
    }

    private boolean isStorageDomainAttachedToStoragePool(StorageDomain storageDomain) {
        try {
            VDSReturnValue vdsReturnValue =
                    runVdsCommand(VDSCommandType.HSMGetStorageDomainInfo,
                            new HSMGetStorageDomainInfoVDSCommandParameters(getVdsId(), storageDomain.getId()));
            if (!vdsReturnValue.getSucceeded()) {
                logErrorMessage(storageDomain);
            }
            Pair<StorageDomainStatic, Guid> domainFromIrs =
                    (Pair<StorageDomainStatic, Guid>) vdsReturnValue.getReturnValue();
            if (domainFromIrs.getSecond() != null) {
                return true;
            }
        } catch (RuntimeException e) {
            logErrorMessage(storageDomain);
        }
        return false;
    }

    private void logErrorMessage(StorageDomain storageDomain) {
        if (storageDomain != null) {
            log.error("Could not get Storage Domain info for Storage Domain (name:'{}', id:'{}') with VDS '{}'. ",
                    storageDomain.getName(),
                    storageDomain.getId(),
                    getVdsId());
        } else {
            log.error("Could not get Storage Domain info with VDS '{}'. ", getVdsId());
        }
    }
}
