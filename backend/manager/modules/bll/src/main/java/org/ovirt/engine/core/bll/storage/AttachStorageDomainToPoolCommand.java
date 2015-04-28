package org.ovirt.engine.core.bll.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainToPoolRelationValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolWithStoragesParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.AttachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.DetachStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.HSMGetStorageDomainInfoVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
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

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Execution);
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
            if (isCinderStorageDomain()) {
                handleCinderDomain();
                return;
            }
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
                    connectHostsInUpToDomainStorageServer();

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
                            if (FeatureSupported.importDataStorageDomain(getStoragePool().getCompatibilityVersion())) {
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
                                VDSReturnValue returnValue =
                                        runVdsCommand(VDSCommandType.DetachStorageDomain, detachParams);
                                if (!returnValue.getSucceeded()) {
                                    log.warn("Detaching Storage Domain '{}' from it's previous storage pool '{}'"
                                                    + " has failed. The meta data of the Storage Domain might still"
                                                    + " indicate that it is attached to a different Storage Pool.",
                                            getParameters().getStorageDomainId(),
                                            Guid.Empty,
                                            0);
                                    throw new VdcBLLException(
                                            returnValue.getVdsError() != null ? returnValue.getVdsError().getCode()
                                                    : VdcBllErrors.ENGINE,
                                            returnValue.getExceptionString());
                                }
                            }
                        }
                        createDefaultDiskProfile();
                    }

                    runVdsCommand(VDSCommandType.AttachStorageDomain,
                            new AttachStorageDomainVDSCommandParameters(getParameters().getStoragePoolId(),
                                    getParameters().getStorageDomainId()));
                    final List<OvfEntityData> unregisteredEntitiesFromOvfDisk =
                            getEntitiesFromStorageOvfDisk(getParameters().getStorageDomainId(),
                                    getStoragePoolIdFromVds());
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
                            updateStorageDomainFormatIfNeeded(getStorageDomain());
                            List<DiskImage> ovfStoreDiskImages =
                                    getAllOVFDisks(getParameters().getStorageDomainId(), getStoragePoolIdFromVds());
                            registerAllOvfDisks(ovfStoreDiskImages, getParameters().getStorageDomainId());

                            // Update unregistered entities
                            for (OvfEntityData ovf : unregisteredEntitiesFromOvfDisk) {
                                getUnregisteredOVFDataDao().removeEntity(ovf.getEntityId(),
                                        getParameters().getStorageDomainId());
                                getUnregisteredOVFDataDao().saveOVFData(ovf);
                                log.info("Adding OVF data of entity id '{}' and entity name '{}'",
                                        ovf.getEntityId(),
                                        ovf.getEntityName());
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

    private void handleCinderDomain() {
        CINDERStorageHelper CINDERStorageHelper = new CINDERStorageHelper();
        CINDERStorageHelper.attachCinderDomainToPool(getStorageDomain().getId(),
                getParameters().getStoragePoolId());
        if (getParameters().getActivate()) {
            attemptToActivateCinderDomain();
        }
        setSucceeded(true);
    }

    /**
     * Creating default disk profile for existing storage domain.
     */
    private void createDefaultDiskProfile() {
        if (FeatureSupported.storageQoS(getStoragePool().getCompatibilityVersion())
                && getDiskProfileDao().getAllForStorageDomain(getStorageDomain().getId()).isEmpty()) {
            final DiskProfile diskProfile =
                    DiskProfileHelper.createDiskProfile(getStorageDomain().getId(),
                            getStorageDomainName());
            executeInNewTransaction(new TransactionMethod<Object>() {
                @Override
                public Void runInTransaction() {
                    getDiskProfileDao().save(diskProfile);
                    getCompensationContext().snapshotNewEntity(diskProfile);
                    getCompensationContext().stateChanged();
                    return null;
                }
            });
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getStorageDomainId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.STORAGE, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    protected void attemptToActivateDomain() {
        StorageDomainPoolParametersBase activateParameters = new StorageDomainPoolParametersBase(getStorageDomain().getId(),
                getStoragePool().getId());
        getBackend()
                .runInternalAction(VdcActionType.ActivateStorageDomain,
                        activateParameters,
                        cloneContext().withoutCompensationContext().withoutExecutionContext());
    }

    protected void attemptToActivateCinderDomain() {
        try {
            CINDERStorageHelper CINDERStorageHelper = new CINDERStorageHelper();
            CINDERStorageHelper.activateCinderDomain(
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
    protected boolean canDoAction() {
        // We can share only ISO or Export domain , or a data domain
        // which is not attached.
        StorageDomainToPoolRelationValidator
                storageDomainToPoolRelationValidator = new StorageDomainToPoolRelationValidator(getStorageDomain().getStorageStaticData(), getStoragePool());
        StorageDomainValidator storageDomainValidator = new StorageDomainValidator(getStorageDomain());
        boolean returnValue =
                checkStoragePool()
                        && initializeVds() && checkStorageDomain() && validate(storageDomainValidator.checkStorageDomainSharedStatusNotLocked()) &&
                            validate(storageDomainToPoolRelationValidator.validateDomainCanBeAttachedToPool());

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

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.addAll(super.getPermissionCheckSubjects());
        permissionList.add(new PermissionSubject(getStoragePoolId(),
                VdcObjectType.StoragePool,
                getActionType().getActionGroup()));
        return permissionList;
    }

    protected Guid getStoragePoolIdFromVds() {
        return getVds().getStoragePoolId();
    }
}
