package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.profiles.DiskProfileHelper;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDAO;

@DisableInPrepareMode
@NonTransactiveCommandAttribute
public class MoveOrCopyDiskCommand<T extends MoveOrCopyImageGroupParameters> extends CopyImageGroupCommand<T>
        implements QuotaStorageDependent {

    private List<PermissionSubject> cachedPermsList;
    private List<Pair<VM, VmDevice>> cachedVmsDeviceInfo;
    private String cachedDiskIsBeingMigratedMessage;

    public MoveOrCopyDiskCommand(T parameters) {
        this(parameters, null);
    }

    public MoveOrCopyDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        defineVmTemplate();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    protected void defineVmTemplate() {
        if (getParameters().getOperation() == ImageOperation.Copy) {
            setVmTemplate(getTemplateForImage());
        }
    }

    protected VmTemplate getTemplateForImage() {
        if (getImage() == null) {
            return null;
        }
        Collection<VmTemplate> templates = getVmTemplateDAO().getAllForImage(getImage().getImageId()).values();
        return !templates.isEmpty() ? templates.iterator().next() : null;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(getParameters().getOperation() == ImageOperation.Copy ?
                        VdcBllMessages.VAR__ACTION__COPY
                        : VdcBllMessages.VAR__ACTION__MOVE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    protected boolean canDoAction() {
        return isImageExist()
                && checkOperationIsCorrect()
                && canFindVmOrTemplate()
                && isDiskUsedAsOvfStore()
                && isImageNotLocked()
                && isSourceAndDestTheSame()
                && validateSourceStorageDomain()
                && validateDestStorage()
                && checkTemplateInDestStorageDomain()
                && validateSpaceRequirements()
                && checkCanBeMoveInVm()
                && checkIfNeedToBeOverride()
                && setAndValidateDiskProfiles();
    }

    protected boolean isSourceAndDestTheSame() {
        if (getParameters().getOperation() == ImageOperation.Move
                && getParameters().getSourceDomainId().equals(getParameters().getStorageDomainId())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_SOURCE_AND_TARGET_SAME);
        }
        return true;
    }

    protected boolean isImageExist() {
        if (getImage() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
        }
        return true;
    }

    protected boolean isImageNotLocked() {
        DiskImage diskImage = getImage();
        if (diskImage.getImageStatus() == ImageStatus.LOCKED) {
            if (getParameters().getOperation() == ImageOperation.Move) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED,
                        String.format("$%1$s %2$s", "diskAliases", diskImage.getDiskAlias()));
            } else {
                return failCanDoAction(VdcBllMessages.VM_TEMPLATE_IMAGE_IS_LOCKED);
            }
        }
        return true;
    }

    protected boolean isDiskUsedAsOvfStore() {
        return validate(new DiskValidator(getImage()).isDiskUsedAsOvfStore());
    }

    /**
     * The following method will perform a check for correctness of operation
     * It is allow to copy only if it is image that belongs to template and
     * it is allow to move only if it is image of disk
     * @return
     */
    protected boolean checkOperationIsCorrect() {
        if (getParameters().getOperation() == ImageOperation.Copy
                && (getImage().getVmEntityType() == null || !getImage().getVmEntityType().isTemplateType())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_IS_NOT_TEMPLATE_DISK);
        }

        if (getParameters().getOperation() == ImageOperation.Move
                && getImage().getVmEntityType() != null && getImage().getVmEntityType().isTemplateType()) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_DISK_IS_NOT_VM_DISK);
        }
        return true;
    }

    protected boolean validateDestStorage() {
        StorageDomainValidator validator = new StorageDomainValidator(getStorageDomain());
        if (!validate(validator.isDomainExistAndActive()) || !validate(validator.domainIsValidDestination())) {
            return false;
        }

        // Validate shareable disks moving
        if (getParameters().getOperation() == ImageOperation.Move && getImage().isShareable() && getStorageDomain().getStorageType() == StorageType.GLUSTERFS ) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CANT_MOVE_SHAREABLE_DISK_TO_GLUSTERFS,
                    String.format("$%1$s %2$s", "diskAlias", getImage().getDiskAlias()));
        }

        return true;
    }

    /**
     * Check if destination storage has enough space
     * @return
     */
    protected boolean validateSpaceRequirements() {
        StorageDomainValidator storageDomainValidator = createStorageDomainValidator();
        if (validate(storageDomainValidator.isDomainWithinThresholds())) {
            getImage().getSnapshots().addAll(getAllImageSnapshots());
            return validate(storageDomainValidator.hasSpaceForClonedDisk(getImage()));
        }
        return false;
    }

    protected List<DiskImage> getAllImageSnapshots() {
        return ImagesHandler.getAllImageSnapshots(getImage().getImageId());
    }

    protected boolean checkIfNeedToBeOverride() {
        if (getParameters().getOperation() == ImageOperation.Copy && !getParameters().getForceOverride()
                && getImage().getStorageIds().contains(getStorageDomain().getId())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_IMAGE_ALREADY_EXISTS);
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
        }
        StorageDomainValidator validator =
                new StorageDomainValidator(getStorageDomainDAO().getForStoragePool(sourceDomainId,
                        getImage().getStoragePoolId()));
        return validate(validator.isDomainExistAndActive());
    }

    /**
     * If a disk is attached to VM it can be moved when it is unplugged or at case that disk is plugged
     * vm should be down
     * @return
     */
    protected boolean checkCanBeMoveInVm() {
        return validate(createDiskValidator().isDiskPluggedToVmsThatAreNotDown(false, getVmsWithVmDeviceInfoForDiskId()));
    }

    /**
     * Cache method to retrieve all the VMs with the device info related to the image
     */
    protected List<Pair<VM, VmDevice>> getVmsWithVmDeviceInfoForDiskId() {
        if (cachedVmsDeviceInfo == null) {
            cachedVmsDeviceInfo = getVmDAO().getVmsWithPlugInfo(getImage().getId());
        }
        return cachedVmsDeviceInfo;
    }

    /**
     * The following method will check, if we can move disk to destination storage domain, when
     * it is based on template
     * @return
     */
    protected boolean checkTemplateInDestStorageDomain() {
        if (getParameters().getOperation() == ImageOperation.Move
                && !Guid.Empty.equals(getImage().getImageTemplateId())) {
            DiskImage templateImage = getDiskImageDao().get(getImage().getImageTemplateId());
            if (!templateImage.getStorageIds().contains(getParameters().getStorageDomainId())) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_NOT_FOUND_ON_DESTINATION_DOMAIN);
            }
        }
        return true;
    }

    protected VmDeviceDAO getVmDeviceDAO() {
        return getDbFacade().getVmDeviceDao();
    }

    protected VdcActionType getImagesActionType() {
        if (getParameters().getOperation() == ImageOperation.Move) {
            return VdcActionType.MoveImageGroup;
        }
        return VdcActionType.CopyImageGroup;
    }

    @Override
    protected void executeCommand() {
        overrideParameters();
        VdcReturnValueBase vdcRetValue = runInternalActionWithTasksContext(
                getImagesActionType(),
                getParameters());
        if (!vdcRetValue.getSucceeded()) {
            setSucceeded(false);
            getReturnValue().setFault(vdcRetValue.getFault());
        } else {
            setSucceeded(true);
            getReturnValue().getVdsmTaskIdList().addAll(vdcRetValue.getInternalVdsmTaskIdList());
        }
    }

    private void endCommandActions() {
        getBackend().endAction(getImagesActionType(),
                getParameters(),
                getContext().clone().withoutCompensationContext().withoutLock());
        setSucceeded(true);
    }

    @Override
    protected void endSuccessfully() {
        endCommandActions();
        incrementDbGenerationForRelatedEntities();
    }

    private void incrementDbGenerationForRelatedEntities() {
        if (getParameters().getOperation() == ImageOperation.Copy) {
            getVmStaticDAO().incrementDbGeneration(getVmTemplateId());
        } else {
            List<Pair<VM, VmDevice>> vmsForDisk = getVmsWithVmDeviceInfoForDiskId();
            for (Pair<VM, VmDevice> pair : vmsForDisk) {
                getVmStaticDAO().incrementDbGeneration(pair.getFirst().getId());
            }
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
            return getSucceeded() ? (getParameters().getOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_VM_DISK
                    : AuditLogType.USER_COPIED_TEMPLATE_DISK
                    : (getParameters().getOperation() == ImageOperation.Move) ? AuditLogType.USER_FAILED_MOVED_VM_DISK
                            : AuditLogType.USER_FAILED_COPY_TEMPLATE_DISK;

        case END_SUCCESS:
            return getSucceeded() ? (getParameters().getOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_VM_DISK_FINISHED_SUCCESS
                    : AuditLogType.USER_COPIED_TEMPLATE_DISK_FINISHED_SUCCESS
                    : (getParameters().getOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_VM_DISK_FINISHED_FAILURE
                            : AuditLogType.USER_COPIED_TEMPLATE_DISK_FINISHED_FAILURE;

        default:
            return (getParameters().getOperation() == ImageOperation.Move) ? AuditLogType.USER_MOVED_VM_DISK_FINISHED_FAILURE
                    : AuditLogType.USER_COPIED_TEMPLATE_DISK_FINISHED_FAILURE;
        }
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (cachedPermsList == null) {
            cachedPermsList = new ArrayList<PermissionSubject>();

            DiskImage image = getImage();
            Guid diskId = image == null ? Guid.Empty : image.getId();
            cachedPermsList.add(new PermissionSubject(diskId, VdcObjectType.Disk, ActionGroup.CONFIGURE_DISK_STORAGE));
            cachedPermsList.add(new PermissionSubject(getParameters().getStorageDomainId(),
                    VdcObjectType.Storage, ActionGroup.CREATE_DISK));
        }
        return cachedPermsList;
    }

    /**
     * The following method will override a parameters which are not relevant for the MoveOrCopyDiskCommand to the
     * correct values for these scenario in order to be used at parent class
     */
    private void overrideParameters() {
        if (getParameters().getOperation() == ImageOperation.Copy) {
            getParameters().setUseCopyCollapse(true);
            getParameters().setAddImageDomainMapping(true);
        } else {
            getParameters().setUseCopyCollapse(false);
        }
        getParameters().setDestinationImageId(getImageId());
        getParameters().setImageGroupID(getImageGroupId());
        getParameters().setDestImageGroupId(getImageGroupId());
        getParameters().setVolumeFormat(getDiskImage().getVolumeFormat());
        getParameters().setVolumeType(getDiskImage().getVolumeType());
        getParameters().setCopyVolumeType(CopyVolumeType.SharedVol);
        getParameters().setParentCommand(getActionType());
        getParameters().setParentParameters(getParameters());
        getParameters().setDiskProfileId(getImage().getDiskProfileId());
    }

    /**
     * The following method will determine if a provided vm/template exists
     * @return
     */
    private boolean canFindVmOrTemplate() {
        if (getParameters().getOperation() == ImageOperation.Copy) {
            if (getVmTemplate() == null) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
            }
        }
        return true;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        if (getParameters().getOperation() == ImageOperation.Copy) {
            if (!Guid.Empty.equals(getVmTemplateId())) {
                return Collections.singletonMap(getVmTemplateId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.TEMPLATE, getDiskIsBeingMigratedMessage()));
            }
        } else {
            List<Pair<VM, VmDevice>> vmsForDisk = getVmsWithVmDeviceInfoForDiskId();
            if (!vmsForDisk.isEmpty()) {
                Map<String, Pair<String, String>> lockMap = new HashMap<>();
                for (Pair<VM, VmDevice> pair : vmsForDisk) {
                    lockMap.put(pair.getFirst().getId().toString(),
                            LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, getDiskIsBeingMigratedMessage()));
                }
                return lockMap;
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
            StringBuilder builder = new StringBuilder(VdcBllMessages.ACTION_TYPE_FAILED_DISK_IS_BEING_MIGRATED.name());
            if (getImage() != null) {
                builder.append(String.format("$DiskName %1$s", getDiskAlias()));
            }
            cachedDiskIsBeingMigratedMessage = builder.toString();
        }
        return cachedDiskIsBeingMigratedMessage;
    }

    public String getDiskAlias() {
        return getImage().getDiskAlias();
    }

    protected boolean setAndValidateDiskProfiles() {
        getImage().setDiskProfileId(getParameters().getDiskProfileId());
        return validate(DiskProfileHelper.setAndValidateDiskProfiles(Collections.singletonMap(getImage(),
                getParameters().getStorageDomainId()), getStoragePool().getcompatibility_version()));
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<QuotaConsumptionParameter>();

        list.add(new QuotaStorageConsumptionParameter(
                getDestinationQuotaId(),
                null,
                QuotaConsumptionParameter.QuotaAction.CONSUME,
                getParameters().getStorageDomainId(),
                (double)getImage().getSizeInGigabytes()));

        if (ImageOperation.Move == getParameters().getOperation()) {
            if (getImage().getQuotaId() != null && !Guid.Empty.equals(getImage().getQuotaId())) {
                list.add(new QuotaStorageConsumptionParameter(
                        getImage().getQuotaId(),
                        null,
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
        List<StorageDomain> storageDomains = getStorageDomainDAO().getAllForStorageDomain(getParameters().getSourceDomainId());
        String sourceSDName = StringUtils.EMPTY;

        if (storageDomains.size() > 0) {
            sourceSDName = storageDomains.get(0).getStorageName();
        }
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("sourcesd", sourceSDName);
            jobProperties.put("targetsd", getStorageDomainName());
            jobProperties.put("diskalias", getDiskAlias());
            if (ImageOperation.Move == getParameters().getOperation()) {
                jobProperties.put("action", "Moving");
            } else {
                jobProperties.put("action", "Copying");
            }
        }
        return jobProperties;
    }

    protected StorageDomainValidator createStorageDomainValidator() {
        return new StorageDomainValidator(getStorageDomain());
    }

    protected DiskValidator createDiskValidator() {
        return new DiskValidator(getImage());
    }
}
