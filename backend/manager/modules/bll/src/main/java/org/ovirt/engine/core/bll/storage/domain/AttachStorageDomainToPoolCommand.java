package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.CINDERStorageHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainToPoolRelationValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolWithStoragesParameter;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.StorageBlockSize;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.AttachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.DetachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.dao.profiles.DiskProfileDao;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class AttachStorageDomainToPoolCommand<T extends AttachStorageDomainToPoolParameters> extends
        StorageDomainCommandBase<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private DiskProfileDao diskProfileDao;

    private StoragePoolIsoMap map;
    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;
    @Inject
    private UnregisteredOVFDataDao unregisteredOVFDataDao;
    @Inject
    private CINDERStorageHelper cinderStorageHelper;

    public AttachStorageDomainToPoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public AttachStorageDomainToPoolCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected List<DiskImage> getAllOVFDisks(Guid storageDomainId, Guid storagePoolId) {
        return super.getAllOVFDisks(storageDomainId, storagePoolId);
    }

    @Override
    protected List<OvfEntityData> getEntitiesFromStorageOvfDisk(Guid storageDomainId, Guid storagePoolId) {
        return super.getEntitiesFromStorageOvfDisk(storageDomainId, storagePoolId);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
    }

    @Override
    protected void executeCommand() {
        if (isManagedBlockStorageDomain() || !getStorageDomain().isManaged()) {
            handleExternallyManagedStorageDomain();
            return;
        }
        if (isCinderStorageDomain()) {
            handleCinderDomain();
            return;
        }
        if (getStoragePool().getStatus() == StoragePoolStatus.Uninitialized) {
            StoragePoolWithStoragesParameter parameters = new StoragePoolWithStoragesParameter(getStoragePool(),
                    Collections.singletonList(getStorageDomain().getId()),
                    getParameters().getSessionId());
            parameters.setIsInternal(true);
            parameters.setTransactionScopeOption(TransactionScopeOption.Suppress);

            ActionReturnValue returnValue = runInternalAction(
                    ActionType.AddStoragePoolWithStorages,
                    parameters,
                    getContext().clone().withoutCompensationContext());
            setSucceeded(returnValue.getSucceeded());
            if (!returnValue.getSucceeded()) {
                getReturnValue().setFault(returnValue.getFault());
            }
        } else {
            map = storagePoolIsoMapDao.get(new StoragePoolIsoMapId(getStorageDomain().getId(),
                    getParameters().getStoragePoolId()));
            if (map == null) {
                executeInNewTransaction(() -> {
                    map = new StoragePoolIsoMap(getStorageDomain().getId(), getParameters()
                            .getStoragePoolId(), StorageDomainStatus.Locked);
                    storagePoolIsoMapDao.save(map);
                    getCompensationContext().snapshotNewEntity(map);
                    getCompensationContext().stateChanged();
                    return null;
                });

                List<Pair<Guid, Boolean>> hostsConnectionResults = connectHostsInUpToDomainStorageServer();
                if (isAllHostConnectionFailed(hostsConnectionResults)) {
                    log.error("Cannot connect storage connection server, aborting attach storage domain operation.");
                    setSucceeded(false);
                    return;
                }

                // Forcibly detach only data storage domains.
                if (getStorageDomain().getStorageDomainType() == StorageDomainType.Data) {
                    @SuppressWarnings("unchecked")
                    Pair<StorageDomainStatic, Guid> domainFromIrs =
                            (Pair<StorageDomainStatic, Guid>) runVdsCommand(
                                    VDSCommandType.HSMGetStorageDomainInfo,
                                    new HSMGetStorageDomainInfoVDSCommandParameters(getVdsId(),
                                            getParameters().getStorageDomainId())
                            ).getReturnValue();
                    // If the storage domain is already related to another Storage Pool, detach it by force.
                    Guid storagePoolId = domainFromIrs.getSecond();
                    if (storagePoolId != null) {
                        // Master domain version is not relevant since force remove at
                        // DetachStorageDomainVdsCommand does not use it.
                        // Storage pool id can be empty
                        DetachStorageDomainVDSCommandParameters detachParams =
                                new DetachStorageDomainVDSCommandParameters(getStoragePoolIdFromVds(),
                                        getParameters().getStorageDomainId(),
                                        Guid.Empty,
                                        0);
                        detachParams.setForce(true);
                        detachParams.setDetachFromOldStoragePool(true);
                        try {
                            runVdsCommand(VDSCommandType.DetachStorageDomain, detachParams);
                        } catch (EngineException e) {
                            log.warn("Detaching Storage Domain '{}' from it's previous storage pool '{}'"
                                    + " has failed. The meta data of the Storage Domain might still"
                                    + " indicate that it is attached to a different Storage Pool.",
                                    getParameters().getStorageDomainId(),
                                    Guid.Empty);
                            throw e;
                        }
                    }
                    if (diskProfileDao.getAllForStorageDomain(getStorageDomain().getId()).isEmpty()) {
                        createDefaultDiskProfile();
                    }
                }

                runVdsCommand(VDSCommandType.AttachStorageDomain,
                        new AttachStorageDomainVDSCommandParameters(getParameters().getStoragePoolId(),
                                getParameters().getStorageDomainId()));
                final List<OvfEntityData> unregisteredEntitiesFromOvfDisk = new ArrayList<>();
                if (getStorageDomain().getStorageDomainType().isDataDomain()) {
                    List<OvfEntityData> returnValueFromStorageOvfDisk = getEntitiesFromStorageOvfDisk(
                            getParameters().getStorageDomainId(), getStoragePoolIdFromVds());
                    unregisteredEntitiesFromOvfDisk.addAll(returnValueFromStorageOvfDisk);
                }

                List<DiskImage> ovfStoreDiskImages =
                        getAllOVFDisks(getParameters().getStorageDomainId(), getStoragePoolIdFromVds());
                executeInNewTransaction(() -> {
                    final StorageDomainType sdType = getStorageDomain().getStorageDomainType();
                    map.setStatus(StorageDomainStatus.Maintenance);
                    storagePoolIsoMapDao.updateStatus(map.getId(), map.getStatus());

                    if (sdType == StorageDomainType.Master) {
                        calcStoragePoolStatusByDomainsStatus();
                    }
                    if (getStorageDomain().getStorageDomainType().isDataDomain()) {
                        registerAllOvfDisks(ovfStoreDiskImages, getParameters().getStorageDomainId());

                        // Update unregistered entities
                        for (OvfEntityData ovf : unregisteredEntitiesFromOvfDisk) {
                            unregisteredOVFDataDao.removeEntity(ovf.getEntityId(),
                                    getParameters().getStorageDomainId());
                            unregisteredOVFDataDao.saveOVFData(ovf);
                            log.info("Adding OVF data of entity id '{}' and entity name '{}'",
                                    ovf.getEntityId(),
                                    ovf.getEntityName());
                        }
                        initUnregisteredDisksToDB(getParameters().getStorageDomainId());
                    }

                    // upgrade the domain format to the storage pool format
                    updateStorageDomainFormatIfNeeded(getStorageDomain());
                    return null;
                });

                if (getParameters().getActivate()) {
                    attemptToActivateDomain();
                }
                checkFreeSpace(getStorageDomain());
                setSucceeded(true);
            }
        }
    }

    private void handleExternallyManagedStorageDomain() {
        StorageDomainStatus status = getParameters().getActivate() ? StorageDomainStatus.Active : StorageDomainStatus.Maintenance;
        StoragePoolIsoMap storagePoolIsoMap =
                new StoragePoolIsoMap(getStorageDomain().getId(),
                        getParameters().getStoragePoolId(),
                        status);
        storagePoolIsoMapDao.save(storagePoolIsoMap);

        setSucceeded(true);
    }

    private boolean isAllHostConnectionFailed(List<Pair<Guid, Boolean>> hostsConnectionResults) {
        return hostsConnectionResults.stream().map(Pair::getSecond).noneMatch(Boolean.TRUE::equals);
    }

    private void handleCinderDomain() {
        cinderStorageHelper.attachCinderDomainToPool(getStorageDomain().getId(),
                getParameters().getStoragePoolId());
        if (getParameters().getActivate()) {
            attemptToActivateCinderDomain();
        }
        setSucceeded(true);
    }

    private void checkFreeSpace(StorageDomain storageDomain) {
        AuditLogType type = AuditLogType.UNASSIGNED;
        Integer freeDiskInGB = storageDomain.getStorageDynamicData().getAvailableDiskSize();
        if (freeDiskInGB != null) {
            double freePercent = storageDomain.getStorageDynamicData().getfreeDiskPercent();
            if (freePercent < storageDomain.getWarningLowSpaceIndicator()) {
                type = AuditLogType.IRS_DISK_SPACE_LOW;
            }
            if (freeDiskInGB < storageDomain.getCriticalSpaceActionBlocker()) {
                // Note, if both conditions are met, only IRS_DISK_SPACE_LOW_ERROR will be shown
                type = AuditLogType.IRS_DISK_SPACE_LOW_ERROR;
            }
        }

        if (type != AuditLogType.UNASSIGNED) {
            AuditLogable loggable = new AuditLogableImpl();
            loggable.setStorageDomainId(storageDomain.getId());
            loggable.setStorageDomainName(storageDomain.getStorageName());
            loggable.addCustomValue("DiskSpace", storageDomain.getAvailableDiskSize().toString());
            auditLogDirector.log(loggable, type, "", true);
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getStorageDomainId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    protected void attemptToActivateDomain() {
        StorageDomainPoolParametersBase activateParameters = new StorageDomainPoolParametersBase(getStorageDomain().getId(),
                getStoragePool().getId());
        backend
                .runInternalAction(ActionType.ActivateStorageDomain,
                        activateParameters,
                        cloneContext().withoutCompensationContext().withoutExecutionContext());
    }

    protected void attemptToActivateCinderDomain() {
        try {
            cinderStorageHelper.activateCinderDomain(
                    getParameters().getStorageDomainId(), getParameters().getStoragePoolId());
        } catch (RuntimeException e) {
            auditLogDirector.log(this, AuditLogType.USER_ACTIVATE_STORAGE_DOMAIN_FAILED);
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ATTACH_STORAGE_DOMAIN_TO_POOL
                : AuditLogType.USER_ATTACH_STORAGE_DOMAIN_TO_POOL_FAILED;
    }

    @Override
    protected boolean validate() {
        StoragePoolValidator spValidator = createStoragePoolValidator();
        if (getStorageDomain() != null && !getStorageDomain().isManaged()) {
            return true;
        }

        if (!validate(spValidator.exists())
                        || !initializeVds()
                        || !checkStorageDomain()) {
            return false;
        }

        StorageDomainToPoolRelationValidator
                storageDomainToPoolRelationValidator = new StorageDomainToPoolRelationValidator(getStorageDomain().getStorageStaticData(), getStoragePool());
        if (!validate(storageDomainToPoolRelationValidator.validateDomainCanBeAttachedToPool())) {
            return false;
        }

        if (getStorageDomain().getStorageType().isManagedBlockStorage() &&
                !FeatureSupported.isManagedBlockDomainSupported(getStoragePool().getCompatibilityVersion())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_ACTIVATE_MANAGED_BLOCK_STORAGE_DOMAIN);
        }
        if (!spValidator.isNotInStatus(StoragePoolStatus.Uninitialized).isValid()
                && getStorageDomain().getStorageDomainType() != StorageDomainType.Data) {
            return failValidation(EngineMessage.ERROR_CANNOT_ADD_STORAGE_POOL_WITHOUT_DATA_DOMAIN);
        }
        if (spValidator.isNotInStatus(StoragePoolStatus.Uninitialized).isValid()) {
            return checkMasterDomainIsUp();
        }
        if (getStorageDomain().getBlockSize() == StorageBlockSize.BLOCK_4K &&
                !validate(storageDomainToPoolRelationValidator.isBlockSizeAutoDetectionSupported())) {
            return false;
        }
        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__STORAGE__DOMAIN);
        addValidationMessage(EngineMessage.VAR__ACTION__ATTACH);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();
        permissionList.add(new PermissionSubject(getStoragePoolId(),
                VdcObjectType.StoragePool,
                getActionType().getActionGroup()));
        return permissionList;
    }

    protected Guid getStoragePoolIdFromVds() {
        return getVds().getStoragePoolId();
    }
}
