package org.ovirt.engine.core.bll.storage.disk;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessage;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskOperationsValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.ManagedBlockStorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageDbOperationScope;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.GetUnregisteredDiskQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@DisableInPrepareMode
@NonTransactiveCommandAttribute
public class RemoveDiskCommand<T extends RemoveDiskParameters> extends CommandBase<T>
        implements QuotaStorageDependent {

    @Inject
    private SnapshotsValidator snapshotsValidator;

    @Inject
    private ImagesHandler imagesHandler;

    private Disk disk;
    private List<PermissionSubject> permsList = null;
    private List<VM> listVms;
    private String cachedDiskIsBeingRemovedLockMessage;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private StoragePoolDao storagePoolDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private UnregisteredDisksDao unregisteredDisksDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    public RemoveDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setStorageDomainId(getParameters().getStorageDomainId());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    @Override
    protected boolean validate() {
        if (getDisk() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
        }


        return validateHostedEngineDisks() && canRemoveBasedOnLegacyHostedEngineDiskAlias() && validateAllVmsForDiskAreDown()
                && canRemoveDiskBasedOnContentTypeChecks()
                && canRemoveDiskBasedOnStorageTypeCheck();
    }

    /**
     * From version 4.3 the Hosted Engine disks are created with a specific content type but in earlier setups
     * the only way to distinguish whether a disk is a part of Hosted Engine is to check the disk alias
     */
    private boolean canRemoveBasedOnLegacyHostedEngineDiskAlias() {
        if (StorageConstants.HOSTED_ENGINE_DISKS_ALIASES.contains(getDiskAlias())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_A_HOSTED_ENGINE_DISK);
        }
        return true;
    }

    private boolean canRemoveDiskBasedOnContentTypeChecks() {
        Disk disk = getDisk();

        if (!getParameters().isSuppressContentTypeCheck() &&
                !validate(new DiskOperationsValidator(disk).isOperationAllowedOnDisk(getActionType()))) {
            return false;
        }

        // No need to validate for Hosted Engine disks as the remove operation is not allowed of them to begin with
        switch (disk.getContentType()) {
        case OVF_STORE:
            DiskImagesValidator diskImagesValidator = new DiskImagesValidator((DiskImage) disk);
            if (!validate(diskImagesValidator.disksInStatus(ImageStatus.ILLEGAL,
                    EngineMessage.ACTION_TYPE_FAILED_OVF_DISK_NOT_IN_APPLICABLE_STATUS))) {
                return false;
            }
            break;
        case MEMORY_DUMP_VOLUME:
        case MEMORY_METADATA_VOLUME:
            List<Snapshot> snapshots = snapshotDao.getSnapshotsByMemoryDiskId(disk.getId());
            // If there's more than one snapshot it means the snapshot is in preview
            if (snapshots.size() > 1) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_MEMORY_DISK_SNAPSHOT_IN_PREVIEW);
            }
            if (!snapshots.isEmpty() && snapshots.get(0).getType() == Snapshot.SnapshotType.ACTIVE) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_HIBERNATION_DISK);
            }
            break;
        case ISO:
            List<String> vmNames = vmStaticDao.getAllNamesWithSpecificIsoAttached(disk.getId());
            if (!vmNames.isEmpty()) {
                return failValidation(EngineMessage.ERROR_CANNOT_REMOVE_ISO_DISK_ATTACHED_TO_VMS,
                    String.format("$VmNames %1$s", String.join(",", vmNames)));
            }
        }

        return true;
    }

    private boolean validateHostedEngineDisks() {
        DiskValidator oldDiskValidator = new DiskValidator(getDisk());
        if (getDisk().getVmEntityType() != null && getDisk().getVmEntityType().isVmType()) {
            for (VM vm : getVmsForDiskId()) {
                if (!validate(oldDiskValidator.validRemovableHostedEngineDisks(vm))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Validate that all vms containing the disk are down, except hosted engine vm
     */
    private boolean validateAllVmsForDiskAreDown() {
        if (getDisk().getVmEntityType() != null && getDisk().getVmEntityType().isVmType()) {
            for (VM vm : getVmsForDiskId()) {
                if (vm.getStatus() != VMStatus.Down && !vm.isHostedEngine()) {
                    VmDevice vmDevice = vmDeviceDao.get(new VmDeviceId(getDisk().getId(), vm.getId()));
                    if (vmDevice.isPlugged()) {
                        addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean canRemoveDiskBasedOnStorageTypeCheck() {
        // currently, only images have specific checks.
        // In the future, if LUNs get specific checks,
        // or additional storage types are added, other else-if clauses should be added.
        if (getDisk().getDiskStorageType() == DiskStorageType.IMAGE ||
                getDisk().getDiskStorageType() == DiskStorageType.CINDER ||
                getDisk().getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE) {
            return canRemoveDiskBasedOnImageStorageCheck();
        }

        return true;
    }

    protected boolean canRemoveDiskBasedOnImageStorageCheck() {
        boolean retValue = true;
        DiskImage diskImage = getDiskImage();

        if (getDisk().getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE) {
            retValue = validate(ManagedBlockStorageDomainValidator.isOperationSupportedByManagedBlockStorage(getActionType()));
        }

        boolean isVmTemplateType = diskImage.getVmEntityType() != null &&
                diskImage.getVmEntityType().isTemplateType();

        if (Guid.isNullOrEmpty(getParameters().getStorageDomainId())) {
            if (isVmTemplateType) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANT_DELETE_TEMPLATE_DISK_WITHOUT_SPECIFYING_DOMAIN);
            }
            getParameters().setStorageDomainId(diskImage.getStorageIds().get(0));
            setStorageDomainId(diskImage.getStorageIds().get(0));
        }

        if (isVmTemplateType) {
            diskImage.setStorageIds(diskImageDao.get(diskImage.getImageId()).getStorageIds());
        }

        if (!diskImage.getStorageIds().contains(getParameters().getStorageDomainId())) {
            retValue = false;
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_IS_WRONG);
        }

        StorageDomainValidator validator = new StorageDomainValidator(getStorageDomain());
        retValue =
                retValue && validate(validator.isDomainExistAndActive())
                        && validate(validator.domainIsValidDestination());

        if (retValue && diskImage.getImageStatus() == ImageStatus.LOCKED) {
            retValue = false;
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED);
        }
        if (retValue && getDisk().getVmEntityType() != null) {
            if (getDisk().getVmEntityType().isVmType()) {
                retValue = canRemoveVmImageDisk();
            } else if (getDisk().getVmEntityType().isTemplateType()) {
                retValue = canRemoveTemplateDisk();
            }
        }

        return retValue;
    }

    /**
     * Set the parent parameter vmTemplateId, based on the disk image id.
     */
    private void setVmTemplateIdParameter() {
        Map<Boolean, VmTemplate> templateMap =
                // Disk image is the only disk type that can be part of the template disks.
                vmTemplateDao.getAllForImage(getDiskImage().getImageId());

        if (!templateMap.isEmpty()) {
            setVmTemplateId(templateMap.values().iterator().next().getId());
        }
    }

    /**
     * Cache method to retrieve all the VMs related to image
     * @return List of Vms.
     */
    private List<VM> getVmsForDiskId() {
        if (listVms == null) {
            listVms = vmDao.getVmsListForDisk(getParameters().getDiskId(), true);
        }
        return listVms;
    }

    private void addAttachVmNamesCustomValue() {
        addCustomValue("VmNames", listVms.stream().map(VM::getName).collect(Collectors.joining(", ")));
    }

    private boolean canRemoveTemplateDisk() {
        if (getVmTemplate().getStatus() == VmTemplateStatus.Locked) {
            return failValidation(EngineMessage.VM_TEMPLATE_IMAGE_IS_LOCKED);
        }

        DiskImage diskImage = getDiskImage();

        if (diskImage.getStorageIds().size() == 1) {
            return failValidation(EngineMessage.VM_TEMPLATE_IMAGE_LAST_DOMAIN);
        }

        if (!checkDerivedVmFromTemplateExists(diskImage) || !checkDerivedDisksFromDiskNotExist(diskImage)){
            return false;
        }

        return true;
    }

    private boolean checkDerivedVmFromTemplateExists(DiskImage diskImage) {
        List<String> vmNames = getNamesOfDerivedVmsFromTemplate(diskImage);
        if (!vmNames.isEmpty()) {
            addValidationMessage(EngineMessage.VMT_CANNOT_REMOVE_DETECTED_DERIVED_VM);
            addValidationMessageVariable("vmsList", StringUtils.join(vmNames, ","));
            return false;
        }
        return true;
    }

    private DiskImagesValidator createDiskImagesValidator(DiskImage disk) {
      return new DiskImagesValidator(disk);
    }

    protected boolean checkDerivedDisksFromDiskNotExist(DiskImage diskImage) {
        return validate(createDiskImagesValidator(diskImage).diskImagesHaveNoDerivedDisks(getParameters().getStorageDomainId()));
    }

    private List<String> getNamesOfDerivedVmsFromTemplate(DiskImage diskImage) {
        List<String> result = new ArrayList<>();
        for (VM vm : vmDao.getAllWithTemplate(getVmTemplateId())) {
            for (Disk vmDisk : diskDao.getAllForVm(vm.getId())) {
                if (vmDisk.getDiskStorageType() == DiskStorageType.IMAGE) {
                    DiskImage vmDiskImage = (DiskImage) vmDisk;
                    if (vmDiskImage.getImageTemplateId().equals(diskImage.getImageId())) {
                        if (vmDiskImage.getStorageIds().contains(getParameters().getStorageDomainId())) {
                            result.add(vm.getName());
                        }
                        break;
                    }
                }
            }
        }
        return result;
    }

    private boolean canRemoveVmImageDisk() {
        if (!listVms.isEmpty()) {
            Guid storagePoolId = listVms.get(0).getStoragePoolId();
            StoragePool sp = storagePoolDao.get(storagePoolId);
            if (!validate(new StoragePoolValidator(sp).existsAndUp())) {
                return false;
            }

            List<DiskImage> diskList = DisksFilter.filterImageDisks(Collections.singletonList(getDisk()),
                    ONLY_NOT_SHAREABLE,
                    ONLY_ACTIVE);
            DiskImagesValidator diskImagesValidator = new DiskImagesValidator(diskList);
            if (!validate(diskImagesValidator.diskImagesNotLocked())) {
                return false;
            }
        }

        for (VM vm : listVms) {
            if (!validate(snapshotsValidator.vmNotDuringSnapshot(vm.getId())) ||
                    !validate(snapshotsValidator.vmNotInPreview(vm.getId()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        switch (getDisk().getDiskStorageType()) {
            case IMAGE:
                if (getDisk().getContentType() == DiskContentType.MEMORY_DUMP_VOLUME ||
                        getDisk().getContentType() == DiskContentType.MEMORY_METADATA_VOLUME) {
                    ActionReturnValue removeOtherMemoryDiskReturnValue = removeOtherMemoryDisk();
                    if (removeOtherMemoryDiskReturnValue != null && removeOtherMemoryDiskReturnValue.getSucceeded()) {
                        getReturnValue().getVdsmTaskIdList().addAll(removeOtherMemoryDiskReturnValue.getInternalVdsmTaskIdList());
                    }
                }
                ActionReturnValue actionReturnValue =
                        runInternalActionWithTasksContext(ActionType.RemoveImage,
                                buildRemoveImageParameters(getDiskImage()));
                if (actionReturnValue.getSucceeded()) {
                    if (getDisk().getContentType() == DiskContentType.MEMORY_DUMP_VOLUME ||
                            getDisk().getContentType() == DiskContentType.MEMORY_METADATA_VOLUME) {
                        removeMemoryDiskFromSnapshotIfNeeded();
                    }

                    incrementVmsGeneration();
                    getReturnValue().getVdsmTaskIdList().addAll(actionReturnValue.getInternalVdsmTaskIdList());
                    setSucceeded(true);
                }
                break;
            case LUN:
                removeLunDisk();
                break;
            case CINDER:
                RemoveCinderDiskParameters params = new RemoveCinderDiskParameters(getParameters().getDiskId());
                params.setEndProcedure(EndProcedure.COMMAND_MANAGED);
                Future<ActionReturnValue> future = commandCoordinatorUtil.executeAsyncCommand(
                        ActionType.RemoveCinderDisk,
                        params,
                        cloneContextAndDetachFromParent());
                try {
                    setReturnValue(future.get());
                    setSucceeded(getReturnValue().getSucceeded());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error removing Cinder disk '{}': {}",
                            getDiskImage().getDiskAlias(),
                            e.getMessage());
                    log.debug("Exception", e);
                }
                break;
            case MANAGED_BLOCK_STORAGE:
                removeManagedBlockStorageDisk();
                break;
        }
        if (getParameters().isUnregisteredDisk()) {
            unregisteredDisksDao.removeUnregisteredDisk(getParameters().getDiskId(), getParameters().getStorageDomainId());
        }
    }

    private void removeManagedBlockStorageDisk() {
        RemoveDiskParameters params = getParameters();
        params.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        params.setCommandId(null);
        Future<ActionReturnValue> future = commandCoordinatorUtil.executeAsyncCommand(
                ActionType.RemoveManagedBlockStorageDisk,
                params,
                cloneContextAndDetachFromParent());
        try {
            setReturnValue(future.get());
            setSucceeded(getReturnValue().getSucceeded());
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error removing managed block storage disk '{}': {}",
                    getDiskImage().getDiskAlias(),
                    e.getMessage());
            log.debug("Exception", e);
        }
    }

    private void removeMemoryDiskFromSnapshotIfNeeded() {
        List<Snapshot> snapshots = snapshotDao.getSnapshotsByMemoryDiskId(getDisk().getId());
        if (!snapshots.isEmpty()) {
            snapshotDao.removeMemoryFromSnapshot(snapshots.get(0).getId());
        }
    }

    private DiskImage getOtherMemoryDisk() {
        List<Snapshot> snapshots = snapshotDao.getSnapshotsByMemoryDiskId(getDisk().getId());
        DiskImage otherMemoryDisk = null;

        if (!snapshots.isEmpty()) {
            if (getDiskImage().getContentType() == DiskContentType.MEMORY_DUMP_VOLUME) {
                Disk metadataDisk = diskDao.get(snapshots.get(0).getMetadataDiskId());
                if (metadataDisk != null) {
                    otherMemoryDisk = (DiskImage) metadataDisk;
                }
            } else {
                Disk memoryDisk = diskDao.get(snapshots.get(0).getMemoryDiskId());
                if (memoryDisk != null) {
                    otherMemoryDisk = (DiskImage) memoryDisk;
                }
            }
        }
        return otherMemoryDisk;
    }

    private ActionReturnValue removeOtherMemoryDisk() {
        DiskImage otherMemoryDisk = getOtherMemoryDisk();
        if (otherMemoryDisk != null) {
            log.info("Removing '{}' disk", otherMemoryDisk.getName());
            return runInternalActionWithTasksContext(ActionType.RemoveImage,
                    buildRemoveImageParameters(otherMemoryDisk));
        }
        return null;
    }

    private RemoveImageParameters buildRemoveImageParameters(DiskImage diskImage) {
        RemoveImageParameters result = new RemoveImageParameters(diskImage.getImageId());
        result.setTransactionScopeOption(TransactionScopeOption.Suppress);
        result.setDiskImage(diskImage);
        result.setParentCommand(ActionType.RemoveDisk);
        result.setEntityInfo(new EntityInfo(VdcObjectType.Disk, getParameters().getDiskId()));
        result.setParentParameters(getParameters());
        result.setRemoveFromSnapshots(true);
        result.setStorageDomainId(getParameters().getStorageDomainId());
        result.setForceDelete(getParameters().getForceDelete());
        if (diskImage.getStorageIds().size() > 1) {
            result.setDbOperationScope(ImageDbOperationScope.MAPPING);
        }
        return result;
    }

    private void removeLunDisk() {
        TransactionSupport.executeInNewTransaction(() -> {
            imagesHandler.removeLunDisk((LunDisk) getDisk());
            incrementVmsGeneration();
            return null;
        });
        setSucceeded(true);
    }

    @Override
    protected void endSuccessfully() {
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        setSucceeded(true);
    }

    private void incrementVmsGeneration() {
        List<VM> listVms = getVmsForDiskId();
        for (VM vm : listVms) {
            vmStaticDao.incrementDbGeneration(vm.getId());
        }
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            if (getDisk().getDiskStorageType() == DiskStorageType.LUN) {
                if (getSucceeded()) {
                    if (getVmsForDiskId().isEmpty()) {
                        return AuditLogType.USER_FINISHED_REMOVE_DISK_NO_DOMAIN;
                    } else {
                        addAttachVmNamesCustomValue();
                        return AuditLogType.USER_FINISHED_REMOVE_DISK_ATTACHED_TO_VMS_NO_DOMAIN;
                    }
                } else {
                    return AuditLogType.USER_FINISHED_FAILED_REMOVE_DISK_NO_DOMAIN;
                }
            } else if (getDisk().getDiskStorageType() == DiskStorageType.CINDER) {
                if (getSucceeded()) {
                    if (getVmsForDiskId().isEmpty()) {
                        return AuditLogType.USER_REMOVE_DISK_INITIATED;
                    } else {
                        addAttachVmNamesCustomValue();
                        return AuditLogType.USER_REMOVE_DISK_ATTACHED_TO_VMS_INITIATED;
                    }
                } else {
                    return AuditLogType.USER_FINISHED_FAILED_REMOVE_DISK;
                }
            }
            if (getSucceeded()) {
                if (getVmsForDiskId().isEmpty()) {
                    return AuditLogType.USER_FINISHED_REMOVE_DISK;
                } else {
                    addAttachVmNamesCustomValue();
                    return AuditLogType.USER_FINISHED_REMOVE_DISK_ATTACHED_TO_VMS;
                }
            } else {
                return AuditLogType.USER_FINISHED_FAILED_REMOVE_DISK;
            }
        default:
            return AuditLogType.UNASSIGNED;
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (permsList == null && getDisk() != null) {
            permsList = new ArrayList<>();
            permsList.add(new PermissionSubject(getDisk().getId(),
                    VdcObjectType.Disk,
                    ActionGroup.DELETE_DISK));
        }
        return permsList;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getDiskId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, getDiskIsBeingRemovedLockMessage()));
    }

    private String getDiskIsBeingRemovedLockMessage() {
        if (cachedDiskIsBeingRemovedLockMessage == null) {
            cachedDiskIsBeingRemovedLockMessage = new LockMessage(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_BEING_REMOVED)
                    .with("DiskName", getDiskAlias())
                    .toString();
        }
        return cachedDiskIsBeingRemovedLockMessage;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        if (getDisk() == null || getDisk().getVmEntityType() == null) {
            return null;
        }

        if (getDisk().getVmEntityType().isVmType()) {
            return createSharedLocksForVmDisk();
        }

        if (getDisk().getVmEntityType().isTemplateType()) {
            return createSharedLocksForTemplateDisk();
        }

        log.warn("No shared locks are taken while removing disk of entity: {}",
                getDisk().getVmEntityType());
        return null;
    }

    private Map<String, Pair<String, String>> createSharedLocksForVmDisk() {
        List<VM> listVms = getVmsForDiskId();
        if (listVms.isEmpty()) {
            return null;
        }

        Map<String, Pair<String, String>> result = new HashMap<>();
        for (VM vm : listVms) {
            result.put(vm.getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, getDiskIsBeingRemovedLockMessage()));
        }
        return result;
    }

    private Map<String, Pair<String, String>> createSharedLocksForTemplateDisk() {
        setVmTemplateIdParameter();
        return Collections.singletonMap(getVmTemplateId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, getDiskIsBeingRemovedLockMessage()));
    }

    protected Disk getDisk() {
        if (disk == null) {
            disk = getParameters().isUnregisteredDisk() ?
                    getUnregisteredDisk() :
                    diskDao.get(getParameters().getDiskId());
        }

        return disk;
    }

    private Disk getUnregisteredDisk() {
        QueryReturnValue queryReturnValue = backend.runInternalQuery(QueryType.GetUnregisteredDisk,
                new GetUnregisteredDiskQueryParameters(
                        getParameters().getDiskId(),
                        getParameters().getStorageDomainId(),
                        getStoragePoolId()));

        if (queryReturnValue.getSucceeded()) {
            return queryReturnValue.getReturnValue();
        }

        log.error("Failed to find unregistered disk with the following ID '{}' in storage domain '{}'",
                getParameters().getDiskId(),
                getStorageDomain().getName());
        return null;
    }

    protected DiskImage getDiskImage() {
        return (DiskImage) getDisk();
    }

    public String getDiskAlias() {
        if (getDisk() != null) {
            return getDisk().getDiskAlias();
        }
        return "";
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("diskalias", getDiskAlias());
        }
        return jobProperties;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();
        if (getDisk() != null
                && getDisk().getDiskStorageType().isInternal()
                && getQuotaId() != null
                && !Guid.Empty.equals(getQuotaId())) {
            list.add(new QuotaStorageConsumptionParameter(
                    getQuotaId(),
                    QuotaConsumptionParameter.QuotaAction.RELEASE,
                    getStorageDomainId(),
                    (double) getDiskImage().getSizeInGigabytes()));
        }
        return list;
    }

    private Guid getQuotaId() {
        if (getDisk() != null && getDisk().getDiskStorageType().isInternal()) {
            return getDiskImage().getQuotaId();
        }
        return null;
    }

    @Override
    public void addQuotaPermissionSubject(List<PermissionSubject> quotaPermissionList) {
    }
}
