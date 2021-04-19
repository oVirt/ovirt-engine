package org.ovirt.engine.core.bll.storage.disk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessage;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.disk.image.CopyImageGroupCommand;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.QuotaValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskOperationsValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleDiskVmElementValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageDbOperationScope;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.UnregisteredDisk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

@DisableInPrepareMode
@NonTransactiveCommandAttribute
public class MoveOrCopyDiskCommand<T extends MoveOrCopyImageGroupParameters> extends CopyImageGroupCommand<T>
        implements QuotaStorageDependent {

    @Inject
    private DiskProfileHelper diskProfileHelper;
    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private SnapshotsValidator snapshotsValidator;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private UnregisteredDisksDao unregisteredDisksDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    private List<PermissionSubject> cachedPermsList;
    private List<Pair<VM, VmDevice>> cachedVmsDeviceInfo;
    private String cachedDiskIsBeingMigratedMessage;

    public MoveOrCopyDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    public MoveOrCopyDiskCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected DiskImage getImage() {
        return super.getImage();
    }

    @Override
    protected Guid getImageGroupId() {
        return super.getImageGroupId();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    public void init() {
        super.init();
        if (isCopyOperation()) {
            setVmTemplate(getTemplateForImage());
        }
    }

    protected VmTemplate getTemplateForImage() {
        if (getImage() == null) {
            return null;
        }
        return vmTemplateDao.getAllForImage(getImage().getImageId()).values().stream().findAny().orElse(null);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(isCopyOperation() ?
                        EngineMessage.VAR__ACTION__COPY
                        : EngineMessage.VAR__ACTION__MOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    @Override
    protected boolean validate() {
        return super.validate()
                && isImageExist()
                && checkOperationIsCorrect()
                && checkOperationAllowedOnDiskContentType()
                && isImageNotLocked()
                && isSourceAndDestTheSame()
                && validateSourceStorageDomain()
                && validateDestStorage()
                && checkTemplateDiskExistAndValidInDestStorageDomain()
                && validateSpaceRequirements()
                && validateVmSnapshotStatus()
                && checkCanBeMoveInVm()
                && checkIfNeedToBeOverride()
                && setAndValidateDiskProfiles()
                && setAndValidateQuota()
                && validatePassDiscardSupportedForDestinationStorageDomain();
    }

    protected boolean isSourceAndDestTheSame() {
        if (isMoveOperation()
                && getParameters().getSourceDomainId().equals(getParameters().getStorageDomainId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_SOURCE_AND_TARGET_SAME);
        }
        return true;
    }

    protected boolean isImageExist() {
        return validate(createDiskValidator(getImage()).isDiskExists());
    }

    protected boolean isImageNotLocked() {
        DiskImage diskImage = getImage();
        if (diskImage.getImageStatus() == ImageStatus.LOCKED) {
            if (isMoveOperation()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED,
                        String.format("$%1$s %2$s", "diskAliases", diskImage.getDiskAlias()));
            } else {
                return failValidation(EngineMessage.VM_TEMPLATE_IMAGE_IS_LOCKED);
            }
        }
        return true;
    }

    protected boolean checkOperationAllowedOnDiskContentType() {
        return validate(new DiskOperationsValidator(getImage()).isOperationAllowedOnDisk(getActionType()));
    }

    /**
     * The following method will perform a check for correctness of operation
     * It is allow to move only if it is image of template
     */
    protected boolean checkOperationIsCorrect() {
        if (isMoveOperation() && getImage().getVmEntityType() != null && getImage().getVmEntityType().isTemplateType()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_NOT_VM_DISK,
                    String.format("$%1$s %2$s", "diskAliases", getImage().getDiskAlias()));
        }
        return true;
    }

    protected boolean validateDestStorage() {
        StorageDomainValidator validator = new StorageDomainValidator(getStorageDomain());
        if (!validate(validator.isDomainExistAndActive()) || !validate(validator.domainIsValidDestination())) {
            return false;
        }

        if (!isSupportedByManagedBlockStorageDomain(getStorageDomain())) {
            return false;
        }

        // Validate shareable disks moving/copying
        boolean moveOrCopy = isMoveOperation() || isCopyOperation();

        if (moveOrCopy && getImage().isShareable()) {
            if (getStorageDomain().getStorageType() == StorageType.GLUSTERFS) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANT_MOVE_SHAREABLE_DISK_TO_GLUSTERFS,
                        String.format("$%1$s %2$s", "diskAlias", getImage().getDiskAlias()));
            }

            if (getStorageDomain().getStorageType().isBlockDomain() && getImage().getVolumeType() == VolumeType.Sparse) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANT_MOVE_OR_COPY_SHAREABLE_THIN_DISK_TO_BLOCK_DOMAIN,
                        String.format("$%1$s %2$s", "diskAlias", getImage().getDiskAlias()));
            }
        }

        if (isMoveOperation()) {
            if (getImage().getStorageIds().contains(getStorageDomainId())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_DESTINATION_STORAGE_DOMAIN_ALREADY_CONTAINS_THE_DISK);
            }
        }

        return true;
    }

    /**
     * Check if destination storage has enough space
     */
    protected boolean validateSpaceRequirements() {
        if (isUnregisteredDiskExistsForCopyTemplate()) {
            return true;
        }

        List<Guid> sdsToValidate = new ArrayList<>();
        sdsToValidate.add(getStorageDomainId());

        if (getActionType() == ActionType.LiveMigrateDisk) {
            sdsToValidate.add(getParameters().getSourceDomainId());
        }

        MultipleStorageDomainsValidator storageDomainsValidator =
                createMultipleStorageDomainsValidator(sdsToValidate);

        if (validate(storageDomainsValidator.allDomainsWithinThresholds())) {
            // If we are copying a template's disk we do not want all its copies
            if (getImage().getVmEntityType() == VmEntityType.TEMPLATE) {
                getImage().getSnapshots().add(getImage());
            } else {
                getImage().getSnapshots().addAll(diskImageDao.getAllSnapshotsForLeaf(getImage().getImageId()));
            }
            return validate(storageDomainsValidator.allDomainsHaveSpaceForDisksWithSnapshots(Collections.singletonList(getImage())));
        }
        return false;
    }

    private boolean validateVmSnapshotStatus() {
        for (Pair<VM, VmDevice> pair : getVmsWithVmDeviceInfoForDiskId()) {
            VmDevice vmDevice = pair.getSecond();
            if (vmDevice.getSnapshotId() == null) { // Skip check for VMs with connected snapshot
                VM vm = pair.getFirst();
                if (!validate(snapshotsValidator.vmNotInPreview(vm.getId()))) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean checkIfNeedToBeOverride() {
        if (isTemplate() && isCopyOperation() && !getParameters().getForceOverride() &&
                getImage().getStorageIds().contains(getStorageDomain().getId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_IMAGE_ALREADY_EXISTS);
        }
        return true;
    }

    /**
     * Validate a source storage domain of image, when a source storage domain is not provided
     * any of the domains image will be used
     */
    protected boolean validateSourceStorageDomain() {
        Guid sourceDomainId = getParameters().getSourceDomainId();
        if (sourceDomainId == null || Guid.Empty.equals(sourceDomainId)) {
            sourceDomainId = getImage().getStorageIds().get(0);
            getParameters().setSourceDomainId(sourceDomainId);
        } else {
            if (!getImage().getStorageIds().contains(sourceDomainId)) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_SOURCE_STORAGE_DOMAIN_DOES_CONTAINS_THE_DISK);
            }
        }
        StorageDomain storageDomain = storageDomainDao.getForStoragePool(sourceDomainId, getImage().getStoragePoolId());
        StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
        return validate(validator.isDomainExistAndActive()) && isSupportedByManagedBlockStorageDomain(storageDomain);
    }

    /**
     * If a disk is attached to VM it can be moved when it is unplugged or at case that disk is plugged
     * vm should be down
     */
    protected boolean checkCanBeMoveInVm() {
        DiskValidator diskValidator = createDiskValidator(getImage());
        if (getImage().getContentType() == DiskContentType.ISO) {
            return validate(diskValidator.isIsoDiskAttachedToAnyNonDownVm());
        }
        return validate(diskValidator.isDiskPluggedToAnyNonDownVm(false));
    }

    /**
     * Cache method to retrieve all the VMs with the device info related to the image
     */
    protected List<Pair<VM, VmDevice>> getVmsWithVmDeviceInfoForDiskId() {
        if (cachedVmsDeviceInfo == null) {
            cachedVmsDeviceInfo = vmDao.getVmsWithPlugInfo(getImage().getId());
        }
        return cachedVmsDeviceInfo;
    }

    /**
     * The following method will check, if we can move disk to destination storage domain, when
     * it is based on template
     */
    protected boolean checkTemplateDiskExistAndValidInDestStorageDomain() {
        if (isMoveOperation()
                && !Guid.Empty.equals(getImage().getImageTemplateId())) {
            DiskImage templateImage = diskImageDao.get(getImage().getImageTemplateId());
            if (!templateImage.getStorageIds().contains(getParameters().getStorageDomainId())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN);
            }
            if (templateImage.getImageStatus() != ImageStatus.OK) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DISK_STATUS_IS_NOT_VALID);
            }
        }
        return true;
    }

    protected ActionType getImagesActionType() {
        if (isMoveOperation()) {
            return ActionType.MoveImageGroup;
        }
        return ActionType.CopyImageGroup;
    }

    @Override
    protected void executeCommand() {
        if (isUnregisteredDiskExistsForCopyTemplate()) {
            addDiskMapping();
            return;
        }
        MoveOrCopyImageGroupParameters p = prepareChildParameters();
        ActionReturnValue vdcRetValue = runInternalActionWithTasksContext(
                getImagesActionType(),
                p);
        if (!vdcRetValue.getSucceeded()) {
            setSucceeded(false);
            getReturnValue().setFault(vdcRetValue.getFault());
        } else {
            setSucceeded(true);
            if (isCopyOperation() && !isTemplate() && !isManagedBlockCopy()) {
                imagesHandler.addDiskImageWithNoVmDevice(getImage());
            }
        }
    }

    private void addDiskMapping() {
        executeInNewTransaction(() -> {
            addStorageDomainMapForCopiedTemplateDisk();
            unregisteredDisksDao.removeUnregisteredDisk(getImage().getId(), getParameters().getStorageDomainId());
            incrementDbGenerationForRelatedEntities();
            return null;
        });
        setSucceeded(true);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    protected boolean isUnregisteredDiskExistsForCopyTemplate() {
        if (isTemplate() && isCopyOperation()) {
            List<UnregisteredDisk> unregisteredDisks =
                    unregisteredDisksDao.getByDiskIdAndStorageDomainId(getImage().getId(),
                            getParameters().getStorageDomainId());
            if (!unregisteredDisks.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void addStorageDomainMapForCopiedTemplateDisk() {
        imageStorageDomainMapDao.save
                (new ImageStorageDomainMap(getParameters().getImageId(),
                        getParameters().getStorageDomainId(),
                        getParameters().getQuotaId(),
                        getImage().getDiskProfileId()));
    }

    private void endCommandActions() {
        if (!getParameters().getImagesParameters().isEmpty()) {
            ActionParametersBase params = getParameters().getImagesParameters().get(0);

            // If this command failed the children should be marked as failed too
            if (!getParameters().getTaskGroupSuccess()) {
                params.setTaskGroupSuccess(false);
            }

            backend.endAction(getImagesActionType(), params,
                    ExecutionHandler.createDefaultContextForTasks(getContext()));
        }
        setSucceeded(true);
    }

    @Override
    protected void endSuccessfully() {
        endCommandActions();
        incrementDbGenerationForRelatedEntities();
    }

    private void incrementDbGenerationForRelatedEntities() {
        if (isCopyOperation()) {
            // When copying a non template disk the copy is to a new
            // floating disk, so no need to increment any generations.
            if (!isTemplate()) {
                return;
            }
            vmStaticDao.incrementDbGeneration(getVmTemplateId());
        } else {
            getVmsWithVmDeviceInfoForDiskId().forEach(p -> vmStaticDao.incrementDbGeneration(p.getFirst().getId()));
        }
    }

    @Override
    protected void endWithFailure() {
        endCommandActions();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? (isMoveOperation()) ? AuditLogType.USER_MOVED_DISK
                    : AuditLogType.USER_COPIED_DISK
                    : (isMoveOperation()) ? AuditLogType.USER_FAILED_MOVED_VM_DISK
                            : AuditLogType.USER_FAILED_COPY_DISK;

        case END_SUCCESS:
            return getSucceeded() ? (isMoveOperation()) ? AuditLogType.USER_MOVED_DISK_FINISHED_SUCCESS
                    : AuditLogType.USER_COPIED_DISK_FINISHED_SUCCESS
                    : (isMoveOperation()) ? AuditLogType.USER_MOVED_DISK_FINISHED_FAILURE
                            : AuditLogType.USER_COPIED_DISK_FINISHED_FAILURE;

        default:
            return (isMoveOperation()) ? AuditLogType.USER_MOVED_DISK_FINISHED_FAILURE
                    : AuditLogType.USER_COPIED_DISK_FINISHED_FAILURE;
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (cachedPermsList == null) {
            cachedPermsList = new ArrayList<>();

            DiskImage image = getImage();
            Guid diskId = image == null ? Guid.Empty : image.getId();
            cachedPermsList.add(new PermissionSubject(diskId, VdcObjectType.Disk, ActionGroup.CONFIGURE_DISK_STORAGE));
            cachedPermsList.add(new PermissionSubject(getParameters().getStorageDomainId(),
                    VdcObjectType.Storage, ActionGroup.CREATE_DISK));
        }
        return cachedPermsList;
    }

    private MoveOrCopyImageGroupParameters prepareChildParameters() {
        MoveOrCopyImageGroupParameters parameters = new MoveOrCopyImageGroupParameters(getParameters());
        if (parameters.getOperation() == ImageOperation.Copy) {
            // If we didn't get here from a parent command, assume we want to collapse the chain
            parameters.setUseCopyCollapse(getParentParameters() == null ? true : getParameters().getUseCopyCollapse());
            parameters.setAddImageDomainMapping(isTemplate() ? true : getParameters().getAddImageDomainMapping());
            parameters.setShouldLockImageOnRevert(false);
            parameters.setDestImages(getParameters().getDestImages());
            parameters.setDestImageGroupId(getParameters().getDestImageGroupId());

            if (!isTemplate()) {
                prepareCopyNotTemplate(parameters);
                parameters.setShouldLockImageOnRevert(true);
                parameters.setRevertDbOperationScope(ImageDbOperationScope.IMAGE);
            }
        } else {
            parameters.setUseCopyCollapse(false);
        }

        if (parameters.getOperation() == ImageOperation.Move || isTemplate()) {
            parameters.setDestinationImageId(getImageId());
            parameters.setImageGroupID(getImageGroupId());
            parameters.setDestImageGroupId(getImageGroupId());
        }

        parameters.setVolumeFormat(getDiskImage().getVolumeFormat());
        parameters.setVolumeType(getDiskImage().getVolumeType());
        parameters.setDiskAlias(getDiskAlias());
        if (isTemplate()) {
            parameters.setCopyVolumeType(CopyVolumeType.SharedVol);
        } else {
            parameters.setCopyVolumeType(CopyVolumeType.LeafVol);
        }
        parameters.setStoragePoolId(getStoragePoolId());
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setDiskProfileId(getImage().getDiskProfileId());
        parameters.setJobWeight(Job.MAX_WEIGHT);
        return parameters;
    }

    /**
     * Prepares the copy of the VM disks and floating disks
     */
    private void prepareCopyNotTemplate(MoveOrCopyImageGroupParameters parameters) {
        parameters.setAddImageDomainMapping(false);

        Guid newImageId = Guid.newGuid();
        Guid newId = Guid.newGuid();

        DiskImage image = getImage();
        image.setId(newId);
        image.setImageId(newImageId);

        image.setDiskAlias(getDiskAlias());
        image.setStorageIds(new ArrayList<>());
        image.getStorageIds().add(getParameters().getStorageDomainId());
        image.setQuotaId(getParameters().getQuotaId());
        image.setDiskProfileId(getParameters().getDiskProfileId());
        image.setImageStatus(ImageStatus.LOCKED);
        image.setVmSnapshotId(null);
        image.setParentId(Guid.Empty);
        image.setImageTemplateId(Guid.Empty);
        image.setDiskDescription(getDiskImage().getDiskDescription());
        parameters.setDestinationImageId(newImageId);
        parameters.setDestImageGroupId(newId);

        // we call copy directly via UI/REST
        if (getParameters().getParentCommand() == ActionType.Unknown) {
            parameters.setDestImages(Arrays.asList(image));
        }
    }

    private boolean isTemplate() {
        return !(getImage().getVmEntityType() == null || !getImage().getVmEntityType().isTemplateType());
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        if (isCopyOperation()) {
            if (!Guid.Empty.equals(getVmTemplateId())) {
                return Collections.singletonMap(getVmTemplateId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, getDiskIsBeingMigratedMessage()));
            }
        } else {
            if (getImage().getContentType() == DiskContentType.ISO) {
                List<Guid> vmIds = vmDynamicDao.getAllIdsWithSpecificIsoAttached(getImage().getId());
                if (!vmIds.isEmpty()) {
                    return vmIds.stream()
                            .collect(Collectors.toMap(p -> p.toString(),
                                    p -> LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, getDiskIsBeingMigratedMessage())));
                }
            } else {
                List<Pair<VM, VmDevice>> vmsForDisk = getVmsWithVmDeviceInfoForDiskId();
                if (!vmsForDisk.isEmpty()) {
                    return vmsForDisk.stream()
                            .collect(Collectors.toMap(p -> p.getFirst().getId().toString(),
                                    p -> LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, getDiskIsBeingMigratedMessage())));
                }
            }
        }
        return null;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(
                (getImage() != null ? getImage().getId() : Guid.Empty).toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, getDiskIsBeingMigratedMessage()));
    }

    private String getDiskIsBeingMigratedMessage() {
        if (cachedDiskIsBeingMigratedMessage == null) {
            cachedDiskIsBeingMigratedMessage = new LockMessage(EngineMessage.ACTION_TYPE_FAILED_DISK_IS_BEING_MIGRATED)
                    .withOptional("DiskName", getImage() != null ? getDiskAlias() : null)
                    .toString();
        }
        return cachedDiskIsBeingMigratedMessage;
    }

    public String getDiskAlias() {
        return StringUtils.isEmpty(getParameters().getNewAlias()) ? getImage().getDiskAlias() : getParameters().getNewAlias();
    }

    protected boolean setAndValidateDiskProfiles() {
        if (isManagedBlockCopy()) {
            return true;
        }

        getImage().setDiskProfileId(getParameters().getDiskProfileId());
        return validate(diskProfileHelper.setAndValidateDiskProfiles(Collections.singletonMap(getImage(),
                getParameters().getStorageDomainId()), getCurrentUser()));
    }

    public boolean setAndValidateQuota() {
        if (Guid.isNullOrEmpty(getDestinationQuotaId())) {
            // Use old quota, if no new quota is set
            // If both quotas are null, use the default for the destination storage pool
            Guid quotaId = getQuotaManager().getFirstQuotaForUser(
                    getImage().getQuotaId(),
                    getStoragePoolId(),
                    getCurrentUser()
            );

            getParameters().setQuotaId(quotaId);
        }

        QuotaValidator validator = createQuotaValidator(getDestinationQuotaId());
        return validate(validator.isValid()) &&
                validate(validator.isDefinedForStorageDomain(getParameters().getStorageDomainId()));
    }

    protected boolean validatePassDiscardSupportedForDestinationStorageDomain() {
        if (isMoveOperation() ||
                (isCopyOperation() && isTemplate())) {
            MultipleDiskVmElementValidator multipleDiskVmElementValidator = createMultipleDiskVmElementValidator();
            return validate(multipleDiskVmElementValidator.isPassDiscardSupportedForDestSd(
                    getParameters().getStorageDomainId()));
        }
        return true;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();

        list.add(new QuotaStorageConsumptionParameter(
                getDestinationQuotaId(),
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                getParameters().getStorageDomainId(),
                (double)getImage().getSizeInGigabytes()));

        if (isMoveOperation()) {
            if (getImage().getQuotaId() != null && !Guid.Empty.equals(getImage().getQuotaId())) {
                list.add(new QuotaStorageConsumptionParameter(
                        getImage().getQuotaId(),
                        QuotaConsumptionParameter.QuotaAction.RELEASE,
                        getParameters().getSourceDomainId(),
                        (double)getImage().getSizeInGigabytes()));
            }
        }
        return list;
    }

    private Guid getDestinationQuotaId() {
        return getParameters().getQuotaId();
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        List<StorageDomain> storageDomains = storageDomainDao.getAllForStorageDomain(getParameters().getSourceDomainId());
        String sourceSDName = StringUtils.EMPTY;

        if (storageDomains.size() > 0) {
            sourceSDName = storageDomains.get(0).getStorageName();
        }
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("sourcesd", sourceSDName);
            jobProperties.put("targetsd", getStorageDomainName());
            jobProperties.put("diskalias", getDiskAlias());
            if (isMoveOperation()) {
                jobProperties.put("action", "Moving");
            } else {
                jobProperties.put("action", "Copying");
            }
        }
        return jobProperties;
    }

    protected MultipleDiskVmElementValidator createMultipleDiskVmElementValidator() {
        return new MultipleDiskVmElementValidator(getImage(),
                diskVmElementDao.getAllDiskVmElementsByDiskId(getParameters().getImageGroupID()));
    }

    public MultipleStorageDomainsValidator createMultipleStorageDomainsValidator(List<Guid> sdsToValidate) {
        return new MultipleStorageDomainsValidator(getStoragePoolId(), sdsToValidate);
    }

    private boolean isMoveOperation() {
        return ImageOperation.Move == getParameters().getOperation();
    }

    private boolean isCopyOperation() {
        return ImageOperation.Copy == getParameters().getOperation();
    }

    private boolean isManagedBlockCopy() {
        return storageDomainDao.get(getParameters().getStorageDomainId()).getStorageType() == StorageType.MANAGED_BLOCK_STORAGE;
    }

    protected QuotaValidator createQuotaValidator(Guid quotaId) {
        return QuotaValidator.createInstance(quotaId, false);
    }
}
